package game.model.upgrades;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public class WideSpreadUpgradeEntity extends UpgradeEntity {

	public WideSpreadUpgradeEntity(double x, double y) {
		super(x, y);
	}

	@Override
	public void applyUpgrade(Game game) {
		game.ship.wideSpreadTime += 15.0D;
	}
}