package game.model.upgrades;

import java.util.Random;

import game.model.StatSlot;
import game.model.entities.ShipEntity;

/**
used to specify a common framework for
{@link StatUpgradeEntity} to use for all stat upgrades.
this way I don't need dedicated classes for
SpeedUpgradeEntity, SpeedDowngradeEntity, FireRateUpgradeEntity, etc...

@see StatUpgradeEntity
@see ShipEntity#stats

@author Michael Johnston (tky886)
*/
public enum StatUpgradeMode {
	SPEED_UPGRADE       (Type.SPEED,      true ),
	SPEED_DOWNGRADE     (Type.SPEED,      false),
	FIRE_RATE_UPGRADE   (Type.FIRE_RATE,  true ),
	FIRE_RATE_DOWNGRADE (Type.FIRE_RATE,  false),
	FIRE_POWER_UPGRADE  (Type.FIRE_POWER, true ),
	FIRE_POWER_DOWNGRADE(Type.FIRE_POWER, false);

	public static final StatUpgradeMode[] VALUES = values();

	public final Type type;
	public final boolean isUpgrade;

	StatUpgradeMode(Type type, boolean isUpgrade) {
		this.type = type;
		this.isUpgrade = isUpgrade;
	}

	public static StatUpgradeMode get(Type type, boolean isUpgrade) {
		int index = type.ordinal() << 1;
		if (!isUpgrade) index++;
		StatUpgradeMode result = VALUES[index];
		assert result.type == type && result.isUpgrade == isUpgrade;
		return result;
	}

	public static StatUpgradeMode random(Random random) {
		return VALUES[random.nextInt(VALUES.length)];
	}

	public void applyTo(StatSlot slot) {
		slot.add(this.isUpgrade ? 1 : -1);
	}

	public void applyTo(ShipEntity ship) {
		this.applyTo(ship.stats.get(this.type));
	}

	public static enum Type {
		SPEED,
		FIRE_RATE,
		FIRE_POWER;

		public static final Type[] VALUES = values();

		public static Type random(Random random) {
			return VALUES[random.nextInt(VALUES.length)];
		}
	}
}