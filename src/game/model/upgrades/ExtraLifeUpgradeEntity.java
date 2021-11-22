package game.model.upgrades;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public class ExtraLifeUpgradeEntity extends UpgradeEntity {

	public ExtraLifeUpgradeEntity(double x, double y) {
		super(x, y);
	}

	@Override
	public void applyUpgrade(Game game) {
		game.ship.lives.increment();
	}
}