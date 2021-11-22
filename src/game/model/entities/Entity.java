package game.model.entities;

import game.model.Game;

/**
base class for all the things the user
might interact with while playing the game.

@author Michael Johnston (tky886)
*/
public abstract class Entity {

	public double x, y;

	public Entity(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** called once per frame for this Entity to perform "movement" tasks. */
	public abstract void tickMovement(Game game);

	/**
	convenience method to check if this Entity's
	bounds still overlap with the Game's bounds.
	used by some implementations of {@link #tickInteraction}.
	*/
	public boolean isInsideGame(Game game, double radius) {
		return (
			this.x + radius > 0.0D &&
			this.x - radius < game.width.doubleValue() &&
			this.y + radius > 0.0D &&
			this.y - radius < game.height.doubleValue()
		);
	}

	/**
	called after all of the entities have ticked their movement.
	allows this entity to "interact" with other entities in the game.
	returns true if this entity is still "valid".
	invalid entities are removed from the game's {@link Game#entities entity list}.
	*/
	public abstract boolean tickInteraction(Game game);

	@Override
	public String toString() {
		return String.format("%s at %.3f, %.3f", this.getClass().getSimpleName(), this.x, this.y);
	}
}