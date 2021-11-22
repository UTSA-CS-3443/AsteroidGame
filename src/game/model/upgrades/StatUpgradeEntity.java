package game.model.upgrades;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public class StatUpgradeEntity extends UpgradeEntity {

	public final StatUpgradeMode mode;

	public StatUpgradeEntity(double x, double y, StatUpgradeMode mode) {
		super(x, y);
		this.mode = mode;
	}

	@Override
	public void applyUpgrade(Game game) {
		this.mode.applyTo(game.ship);
	}
}