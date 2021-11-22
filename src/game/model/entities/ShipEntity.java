package game.model.entities;

import java.util.EnumMap;
import java.util.Objects;

import game.Main;
import game.common.Interpolator;
import game.common.Util;
import game.controller.ShipController;
import game.model.EntityFilter;
import game.model.Game;
import game.model.StatSlot;
import game.model.upgrades.StatUpgradeMode;
import game.model.ValuedStatSlot;

/**
the ship is what the user controls,
by moving it around to dodge and destroy
asteroids, and to collect upgrades.

@see ShipController

@author Michael Johnston (tky886)
*/
public class ShipEntity extends Entity {

	public static final int MAX_SPEED_UPGRADES      = 4;
	public static final int MAX_FIRE_RATE_UPGRADES  = 4;
	public static final int MAX_FIRE_POWER_UPGRADES = 4;
	public static final int MAX_EXTRA_LIVES         = 2;
	public static final double SIZE                 = 32.0D;

	public double velocityX, velocityY;
	public double targetVelocityX, targetVelocityY;
	public ShipController controller;

	public final EnumMap<StatUpgradeMode.Type, StatSlot> stats = new EnumMap<>(StatUpgradeMode.Type.class);
	public final ValuedStatSlot speed     = new ValuedStatSlot(this, "speed",     MAX_SPEED_UPGRADES,      Interpolator.exponential(2.0D, 7.5D));
	public final ValuedStatSlot fireRate  = new ValuedStatSlot(this, "fireRate",  MAX_FIRE_RATE_UPGRADES,  Interpolator.exponential(1.0D, 1.0D / 5.0D));
	public final ValuedStatSlot firePower = new ValuedStatSlot(this, "firePower", MAX_FIRE_POWER_UPGRADES, Interpolator.linear(16.0D, 80.0D));
	public final StatSlot       lives     = new       StatSlot(this, "lives",     MAX_EXTRA_LIVES);

	private int points;
	private int finalScore;

	/** time remaining with the "wide spread" upgrade. */
	public double wideSpreadTime;
	/**
	time remaining during which the ship will ignore collisions with asteroids.
	this can be increased with ghost mode upgrades or,
	if the ship has extra lives remaining,
	by colliding with an asteroid.
	*/
	public double ghostTime;
	/**
	time that the ship has been destroyed for.
	the ship will be destroyed if it collides with an
	asteroid and does not have any extra {@link #lives} left.
	this timer is used to display the explosion animation.
	*/
	public double destroyedTime;

	public ShipEntity(double x, double y) {
		super(x, y);
		if (Main.DEBUG_MODE) {
			this.speed    .setCount(MAX_SPEED_UPGRADES);
			this.fireRate .setCount(MAX_FIRE_RATE_UPGRADES);
			this.firePower.setCount(MAX_FIRE_POWER_UPGRADES);
			this.lives    .setCount(MAX_EXTRA_LIVES);
		}
		this.stats.put(StatUpgradeMode.Type.SPEED,      this.speed);
		this.stats.put(StatUpgradeMode.Type.FIRE_RATE,  this.fireRate);
		this.stats.put(StatUpgradeMode.Type.FIRE_POWER, this.firePower);
	}

	/**
	resets this ship's position, stats, and other properties.
	called when the game ends and is restarted.
	*/
	public void reset(double x, double y) {
		this.setPosition(x, y);
		this.velocityX = 0.0D;
		this.velocityY = 0.0D;
		this.targetVelocityX = 0.0D;
		this.targetVelocityY = 0.0D;
		this.speed    .setCount(Main.DEBUG_MODE ? MAX_SPEED_UPGRADES      : 0);
		this.fireRate .setCount(Main.DEBUG_MODE ? MAX_FIRE_RATE_UPGRADES  : 0);
		this.firePower.setCount(Main.DEBUG_MODE ? MAX_FIRE_POWER_UPGRADES : 0);
		this.lives    .setCount(Main.DEBUG_MODE ? MAX_EXTRA_LIVES         : 0);
		this.points = 0;
		this.finalScore = 0;
		this.wideSpreadTime = 0.0D;
		this.ghostTime = 0.0D;
		this.destroyedTime = 0.0D;
	}

	@Override
	public void tickMovement(Game game) {
		Objects.requireNonNull(this.controller, "ShipController not installed").updateTargetVelocity(this);
		double interpolateAmount = -Math.expm1(-2.0D * this.speed.getStatValue() * game.deltaTime);
		this.velocityX = Util.mix(this.velocityX, this.targetVelocityX, interpolateAmount);
		this.velocityY = Util.mix(this.velocityY, this.targetVelocityY, interpolateAmount);
		double newX = this.x + this.velocityX;
		double newY = this.y + this.velocityY;
		double clampedX = Util.clamp(newX, SIZE, game.width.doubleValue() - SIZE);
		double clampedY = Util.clamp(newY, SIZE, game.height.doubleValue() - SIZE);
		this.x = clampedX;
		this.y = clampedY;
		//stop moving immediately when hitting the edge of the game area.
		//just makes the ship slightly easier to control.
		if (newX != clampedX) this.velocityX = 0.0D;
		if (newY != clampedY) this.velocityY = 0.0D;

		if (this.wideSpreadTime > 0.0D) this.wideSpreadTime -= game.deltaTime;
		if (this.ghostTime > 0.0D) this.ghostTime -= game.deltaTime;
	}

	@Override
	public boolean tickInteraction(Game game) {
		if (this.ghostTime <= 0.0D) {
			for (AsteroidEntity asteroid : game.entities.<AsteroidEntity>getEntities(EntityFilter.ASTEROID)) {
				if (asteroid.checkCollisionAt(this.x - asteroid.x, this.y - asteroid.y)) {
					this.lives.setCountDirect(this.lives.getCount() - 1);
					this.ghostTime = 5.0D;
				}
			}
		}
		return true;
	}

	public boolean tickExplosion(Game game) {
		this.destroyedTime += game.deltaTime;
		return this.destroyedTime < 1.5D;
	}

	public void addPoints(int points, boolean affectFinalScore) {
		this.points += points;
		if (affectFinalScore) this.finalScore += points;
	}

	public int getPoints() {
		return this.points;
	}

	public int getFinalScore() {
		return this.finalScore;
	}
}