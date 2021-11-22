package game.model.spawners;

import java.util.Random;

import game.common.Interpolator;
import game.common.RandomNumberSupplier;
import game.common.Util;
import game.model.Game;
import game.model.StatSlot;
import game.model.upgrades.*;

/**
@see UpgradeEntity

@author Michael Johnston (tky886)
*/
public class UpgradeSpawner implements EntitySpawner {

	private static final Random RANDOM = new Random();
	private static final RandomNumberSupplier UPGRADE_DELAY = new RandomNumberSupplier(RANDOM, Interpolator.linear(5.0D, 15.0D));

	private double nextTime = UPGRADE_DELAY.next();

	@Override
	public void spawn(Game game) {
		if (game.totalTime >= this.nextTime) {
			double x = Util.mix(UpgradeEntity.SIZE, game.width.doubleValue() - UpgradeEntity.SIZE, RANDOM.nextDouble());
			double y = -UpgradeEntity.SIZE;
			UpgradeEntity entity;
			//*
			//1 in 3 chance to spawn a special upgrade.
			if (RANDOM.nextInt(3) == 0) {
				switch (RANDOM.nextInt(4)) {
					case 0: entity = new WideSpreadUpgradeEntity(x, y); break;
					case 1: entity = new   TimeWarpUpgradeEntity(x, y); break;
					case 2: entity = new  ExtraLifeUpgradeEntity(x, y); break;
					case 3: entity = new      GhostUpgradeEntity(x, y); break;
					default: throw new AssertionError();
				}
			}
			else {
				//2 in 3 chance to spawn either a stat upgrade or a stat downgrade.
				StatUpgradeMode.Type upgradeType = StatUpgradeMode.Type.random(RANDOM);
				StatSlot slot = game.ship.stats.get(upgradeType);
				//higher chance of spawning upgrades when the ship does not have very many yet.
				//higher chance of spawning downgrades when the ship already has a lot of upgrades.
				//this makes it easier to acquire upgrades when the game is first getting started,
				//but harder to max all your stats out after you've been playing for a while.
				boolean isUpgrade = RANDOM.nextInt(slot.maxCount) >= slot.getCount();
				StatUpgradeMode mode = StatUpgradeMode.get(upgradeType, isUpgrade);
				entity = new StatUpgradeEntity(x, y, mode);
			}
			//*/
			game.entities.addEntity(entity);
			this.nextTime += UPGRADE_DELAY.next();
		}
	}

	@Override
	public void reset() {
		this.nextTime = UPGRADE_DELAY.next();
	}
}