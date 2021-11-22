package game.model.upgrades;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public class TimeWarpUpgradeEntity extends UpgradeEntity {

	public TimeWarpUpgradeEntity(double x, double y) {
		super(x, y);
	}

	@Override
	public void applyUpgrade(Game game) {
		double lostTime = Math.min(1.5D, game.gameSpeed - Game.MIN_GAME_SPEED);
		game.gameSpeed -= lostTime;
		game.lostTime  += lostTime;
	}
}