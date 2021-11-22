package game.model.upgrades;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public class GhostUpgradeEntity extends UpgradeEntity {

	public GhostUpgradeEntity(double x, double y) {
		super(x, y);
	}

	@Override
	public void applyUpgrade(Game game) {
		game.ship.ghostTime += 15.0D;
	}
}