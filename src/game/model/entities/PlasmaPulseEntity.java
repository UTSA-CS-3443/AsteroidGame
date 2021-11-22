package game.model.entities;

import game.common.Util;
import game.model.EntityFilter;
import game.model.Game;

/**
fired by the ship, destroys asteroids.
@see game.model.spawners.PlasmaPulseSpawner
@see AsteroidEntity

@author Michael Johnston (tky886)
*/
public class PlasmaPulseEntity extends Entity {

	public static final double TAIL_LENGTH = 4.0;

	public double directionX, directionY, speed;
	public int powerLevel;
	public double healthToRemove;

	public PlasmaPulseEntity(
		double x,
		double y,
		double velocityX,
		double velocityY,
		int powerLevel,
		double healthToRemove
	) {
		super(x, y);
		this.speed = Math.sqrt(Util.square(velocityX, velocityY));
		if (this.speed != 0.0D) { //shouldn't ever be 0, but I don't want the direction to NaN out if it is.
			this.directionX = velocityX / this.speed;
			this.directionY = velocityY / this.speed;
		}
		//else leave as 0 by default.
		this.powerLevel = powerLevel;
		this.healthToRemove = healthToRemove;
	}

	@Override
	public void tickMovement(Game game) {
		this.x += this.directionX * this.speed * game.deltaTime;
		this.y += this.directionY * this.speed * game.deltaTime;
	}

	@Override
	public boolean tickInteraction(Game game) {
		if (!this.isInsideGame(game, this.getSize() * TAIL_LENGTH)) return false;
		for (AsteroidEntity asteroid : game.entities.<AsteroidEntity>getEntities(EntityFilter.ASTEROID)) {
			if (asteroid.checkCollisionAt(this.x - asteroid.x, this.y - asteroid.y)) {
				asteroid.integrity -= this.healthToRemove;
				if (asteroid.integrity <= 0.0D) {
					game.ship.addPoints(asteroid.points, true);
				}
				asteroid.rotationSpeed += (this.x - asteroid.x) * this.healthToRemove * asteroid.reciprocalSize * asteroid.reciprocalSize;
				return false;
			}
		}
		return true;
	}

	public double getSize() {
		return this.powerLevel * 4 + 8;
	}
}