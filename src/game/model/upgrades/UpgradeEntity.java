package game.model.upgrades;

import game.common.Util;
import game.model.entities.Entity;
import game.model.Game;

/**
@see game.model.spawners.UpgradeSpawner

@author Michael Johnston (tky886)
*/
public abstract class UpgradeEntity extends Entity {

	public static final double SIZE = 32.0D;
	public static final double COLLISION_RADIUS = 48.0D;

	public UpgradeEntity(double x, double y) {
		super(x, y);
	}

	public abstract void applyUpgrade(Game game);

	@Override
	public void tickMovement(Game game) {
		this.y += game.gameSpeed * game.deltaTime * 96.0D;
	}

	@Override
	public boolean tickInteraction(Game game) {
		if (Util.square(game.ship.x - this.x, game.ship.y - this.y) < COLLISION_RADIUS * COLLISION_RADIUS) {
			this.applyUpgrade(game);
			return false;
		}
		else {
			return true;
		}
	}
}