package game.model.entities;

import java.util.Random;

import game.common.*;
import game.common.AbstractValueNoiseGrid.ValueAndDerivativeHolder;
import game.common.VoronoiGrid.SeedPoint;
import game.model.Game;

/**
you all probably know what an asteroid is,
but there is a concept which is used for a lot of asteroid
logic that is important to know about: coordinate spaces.
asteroids have 3 relevant coordinate spaces,
which I will call absolute space, relative space, and rotated space.

absolute space is the one that all entities can agree on.
the asteroid's {@link #x} and {@link #y} positions are in absolute space.

relative space is aligned like absolute space,
but translated such that the asteroid itself is always at (0, 0).

rotated space has the same offset as relative space,
but as its name would imply, it is also rotated based on the asteroid's {@link #rotation}.

rotated space is the space that asteroids use when querying their {@link #heightNoise}
and {@link #shatterNoise}, and these are then used for rendering and collision testing.
however, most of the methods here actually take coordinate inputs in relative space,
and will convert to rotated space automatically.
this is an important thing to know from a developer perspective because you will have
a lot of pain and sadness if you provide coordinates to an asteroid in the wrong space.

@see game.model.spawners.AsteroidSpawner

@author Michael Johnston (tky886)
*/
public class AsteroidEntity extends Entity {

	private static final Random RANDOM = new Random();
	private static final RandomNumberSupplier SIZE = new RandomNumberSupplier(RANDOM, Interpolator.exponential(40.0D, 160.0D));
	private static final RandomNumberSupplier POINT_MULTIPLIER = new RandomNumberSupplier(RANDOM, Interpolator.exponential(0.5D, 2.0D));
	/** time (in seconds) it takes for an asteroid to disappear after its {@link #integrity} reaches 0. */
	public static final double SHATTER_TIME = 0.5D;

	public double velocityX, velocityY;
	private double rotation;
	/**
	the sine and cosine of the {@link #rotation} are used to convert
	coordinates from relative space to rotated space and vise versa.
	*/
	private double cosRotation, sinRotation;
	public double rotationSpeed;

	public final NoiseGrid heightNoise;
	public final VoronoiGrid shatterNoise;
	public final float brightness;
	public final double size, reciprocalSize;
	/**
	every time a {@link PlasmaPulseEntity} collides with an asteroid,
	the asteroid loses some integrity.
	when the integrity reaches 0, the asteroid is considered destroyed,
	and its shatter animation starts.
	*/
	public double integrity;
	/**
	where the asteroid is in its shatter animation.
	if the asteroid is not destroyed yet,
	this value will be 0.
	*/
	public double shatterTime;
	/**
	how many points are awarded to the ship when this asteroid is destroyed.
	@see ShipEntity#points
	@see ShipEntity#finalScore
	@see ShipEntity#addPoints
	*/
	public int points;

	public AsteroidEntity(double x, double y) {
		this(x, y, 1.0D);
	}

	/**
	@param sizeLimit used to restrict the maximum size of the asteroid.
	used when the game is first starting and the ship doesn't have good weapons yet.
	we don't want the player to be overwhelmed with massive asteroids
	until they acquire enough upgrades to deal with them.
	the range for sizeLimit is 0 to 1.
	*/
	public AsteroidEntity(double x, double y, double sizeLimit) {
		super(x, y);

		this.size = SIZE.interpolate(RANDOM.nextDouble() * sizeLimit);
		this.reciprocalSize = 1.0D / this.size;
		this.integrity = this.size * this.size * 0.015625D;
		this.points = Util.roundRandomly(this.size * POINT_MULTIPLIER.next(), RANDOM);

		this.heightNoise = new NoiseGrid(RANDOM.nextLong(), 64.0D, 0.5D, 0.5F, 0.5F, 5);
		this.shatterNoise = new VoronoiGrid(RANDOM.nextLong(), this.size * 0.25D, 0.75D);
		this.brightness = RANDOM.nextFloat();

		double velocityAngle = RANDOM.nextDouble() * Math.PI;
		double velocityMagnitude = Math.random() * 64.0D;
		this.velocityX = Math.cos(velocityAngle) * velocityMagnitude;
		this.velocityY = Math.sin(velocityAngle) * velocityMagnitude + 64.0D;
		this.setRotation(RANDOM.nextDouble() * (Math.PI * 2.0D));
		this.rotationSpeed = (RANDOM.nextDouble() * 128.0D - 64.0D) * this.reciprocalSize;
	}

	public void setRotation(double angle) {
		this.rotation = angle;
		this.sinRotation = Math.sin(angle);
		this.cosRotation = Math.cos(angle);
	}

	public void addRotation(double angle) {
		this.setRotation(this.rotation + angle);
	}

	@Override
	public void tickMovement(Game game) {
		double adjustedTime = game.getScaledDeltaTime();
		this.x += this.velocityX * adjustedTime;
		this.y += this.velocityY * adjustedTime;
		this.addRotation(this.rotationSpeed * adjustedTime);
		if (this.integrity <= 0.0D) {
			this.shatterTime += game.deltaTime * (1.0D / SHATTER_TIME);
		}
	}

	@Override
	public boolean tickInteraction(Game game) {
		return this.shatterTime < 1.0D && this.isInsideGame(game, this.size);
	}

	/**
	returns true if the position intersects with this asteroid; false otherwise.
	the position should be in relative space.
	*/
	public boolean checkCollisionAt(double x, double y) {
		if (this.integrity <= 0.0D) return false;
		double rotatedX = x * this.cosRotation - y * this.sinRotation;
		double rotatedY = x * this.sinRotation + y * this.cosRotation;
		return this.heightNoise.getValue(rotatedX, rotatedY) + this.getHeightBias(rotatedX, rotatedY) > 0.0F;
	}

	/**
	if the asteroid intersects this location,
	its surface normal will be stored in out,
	and the method returns true.
	otherwise, returns false,
	and the contents of out are undefined.
	the position should be in relative space.
	*/
	public boolean getSurfaceNormal(double x, double y, SeedPoint seedPoint, ValueAndDerivativeHolder out) {
		out.value = out.partialDerivativeX = out.partialDerivativeY = 0.0F;

		double rotatedX = x * this.cosRotation - y * this.sinRotation;
		double rotatedY = x * this.sinRotation + y * this.cosRotation;
		this.heightNoise.getValueAndDerivative(rotatedX, rotatedY, out);
		out.value += this.getHeightBias(rotatedX, rotatedY);
		if (out.value > 0.0F && !this.isInCrack(rotatedX, rotatedY, seedPoint)) {
			double unrotatedDx = out.partialDerivativeY * this.sinRotation + out.partialDerivativeX * this.cosRotation;
			double unrotatedDy = out.partialDerivativeY * this.cosRotation - out.partialDerivativeX * this.sinRotation;
			out.partialDerivativeX = (float)(unrotatedDx);
			out.partialDerivativeY = (float)(unrotatedDy);
			//d/dx (1 - 2 * ((x * reciprocalSize) ^ 2 + (y * reciprocalSize) ^ 2)) = 4 * reciprocalSize ^ 2 * x.
			//similarly, d/dy (1 - 2 * ((x * reciprocalSize) ^ 2 + (y * reciprocalSize) ^ 2)) = 4 * reciprocalSize ^ 2 * y.
			//these derivatives share a common factor.
			double distanceMultiplier = 4.0D * Util.square(this.reciprocalSize);
			out.partialDerivativeX -= (float)(x * distanceMultiplier);
			out.partialDerivativeY -= (float)(y * distanceMultiplier);
			//we originally had the height in the range [-1, 1],
			//but the noise scale starts at 64.
			//this will make for a very flat surface.
			//making the surface taller will scale the x and y
			//components of its gradient, but z is unaffected.
			//oh and also we want the normal vector to face in the decreasing direction,
			//not the increasing direction, so negate the x and y components.
			out.partialDerivativeX *= -8.0F;
			out.partialDerivativeY *= -8.0F;
			//ok so normalizing this value directly isn't actually the normal vector of the surface.
			//the true normal vector requires taking the derivative with respect to z too,
			//(which is always just 1), and therefore we should set out[2] to 1 here.
			//I'm leaving it as-is as a stylistic choice; having the z component
			//decrease near the edges of the asteroid means the x and y
			//components are amplified a bit there, which is what I want.
			float rcpMagnitude = 1.0F / ((float)(Math.sqrt(Util.square(out.value, out.partialDerivativeX, out.partialDerivativeY))));
			out.value *= rcpMagnitude;
			out.partialDerivativeX *= rcpMagnitude;
			out.partialDerivativeY *= rcpMagnitude;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	bias used to shape our {@link #heightNoise}.
	this bias forms the shape of a paraboloid with a
	maximum value of 1 at (0, 0) in relative space,
	and a value of -1 at all points whose distance
	to (0, 0) in relative space is this.{@link #size}.
	the coordinates x and y can be in either relative space or rotated space
	due to the fact that the paraboloid has rotational symmetry about the origin.
	*/
	private float getHeightBias(double x, double y) {
		return 1.0F - 2.0F * ((float)(Util.square(x * this.reciprocalSize, y * this.reciprocalSize)));
	}

	/**
	if the shatter animation is currently playing,
	returns true if the provided point is on one of the cracks.
	returns false if either of these conditions are not met.
	the position should be in rotated space.
	*/
	private boolean isInCrack(double x, double y, SeedPoint seedPoint) {
		if (this.integrity <= 0.0D) {
			seedPoint.setPositionToClosest(x, y);

			//just like the loop in VoronoiGrid,
			//extra edges could be outside this area,
			//but these false negatives are not noticeable in practice.
			int minGridX = seedPoint.gridX - 1;
			int maxGridX = seedPoint.gridX + 1;
			int minGridY = seedPoint.gridY - 1;
			int maxGridY = seedPoint.gridY + 1;
			for (int gridX = minGridX; gridX <= maxGridX; gridX++) {
				for (int gridY = minGridY; gridY <= maxGridY; gridY++) {
					if (gridX == seedPoint.gridX && gridY == seedPoint.gridY) continue;

					double otherX = this.shatterNoise.getSeedPointX(gridX, gridY);
					double otherY = this.shatterNoise.getSeedPointY(gridX, gridY);
					//how close to the edge x and y are.
					//0.0 means x and y are equal to seedPoint.center.
					//1.0 means x and y are on the edge between seedPoint and other.
					double frac = (
						//dot product
						(x - seedPoint.centerX) * (otherX - seedPoint.centerX) +
						(y - seedPoint.centerY) * (otherY - seedPoint.centerY)
					)
					/ (
						//distance squared from seedPoint.center to other.
						Util.square(otherX - seedPoint.centerX, otherY - seedPoint.centerY)
					)
					//if this were left as-is,
					//frac 1 would be at other, not the edge.
					//we multiply by 2 here because the edge is exactly
					//halfway between seedPoint.center and other.
					* 2.0D;

					if (frac > 1.0D - this.shatterTime) {
						return true;
					}
				}
			}
		}
		return false;
	}
}