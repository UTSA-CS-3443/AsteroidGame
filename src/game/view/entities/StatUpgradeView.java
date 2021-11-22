package game.view.entities;

import game.model.upgrades.StatUpgradeEntity;
import game.view.Images;
import javafx.scene.image.Image;

/** @author Michael Johnston (tky886) */
public class StatUpgradeView extends SimpleImageEntityView<StatUpgradeEntity> {

	public static final StatUpgradeView INSTANCE = new StatUpgradeView();

	@Override
	public Image getImage(StatUpgradeEntity upgrade) {
		switch (upgrade.mode) {
			case SPEED_UPGRADE:        return Images.SPEED_UPGRADE;
			case SPEED_DOWNGRADE:      return Images.SPEED_DOWNGRADE;
			case FIRE_RATE_UPGRADE:    return Images.FIRE_RATE_UPGRADE;
			case FIRE_RATE_DOWNGRADE:  return Images.FIRE_RATE_DOWNGRADE;
			case FIRE_POWER_UPGRADE:   return Images.FIRE_POWER_UPGRADE;
			case FIRE_POWER_DOWNGRADE: return Images.FIRE_POWER_DOWNGRADE;
			default: throw new AssertionError("Must add " + upgrade.mode + " to switch statement in StatUpgradeView.");
		}
	}
}