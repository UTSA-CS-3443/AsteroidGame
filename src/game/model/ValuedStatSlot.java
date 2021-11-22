package game.model;

import game.common.Interpolator;
import game.common.Util.AbstractDoubleBinding;
import game.model.entities.ShipEntity;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
a {@link StatSlot} which has an additional tracked value of some kind.
this can be used to make values which depend on the number of upgrades,
but are not directly equal to that number of upgrades.
the value is determined by an {@link Interpolator}.

@author Michael Johnston (tky886)
*/
public class ValuedStatSlot extends StatSlot {

	private final DoubleProperty valueProperty;

	public ValuedStatSlot(ShipEntity ship, String name, int maxCount, Interpolator interpolator) {
		super(ship, name, maxCount);
		this.valueProperty = new SimpleDoubleProperty(ship, name + "_value", interpolator.interpolate(0.0D));
		ReadOnlyIntegerProperty count = this.countProperty();
		double reciprocalMaxCount = 1.0D / maxCount;
		this.valueProperty.bind(
			AbstractDoubleBinding.create(
				() -> interpolator.interpolate(count.doubleValue() * reciprocalMaxCount),
				count
			)
		);
	}

	public ReadOnlyDoubleProperty valueProperty() {
		return this.valueProperty;
	}

	public double getStatValue() {
		return this.valueProperty.getValue();
	}
}