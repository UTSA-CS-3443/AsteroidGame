package game.model;

import game.common.Util;
import game.model.entities.ShipEntity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
basically a holder for an {@link IntegerProperty}
and a maximum value for that property.
the minimum value is always assumed to be 0.
attempting to change the count with {@link #increment},
{@link #decrement}, {@link #add}, or {@link #setCount}
will respect these bounds.

@author Michael Johnston (tky886)
*/
public class StatSlot {

	private final IntegerProperty countProperty;
	/** inclusive. */
	public final int maxCount;

	public StatSlot(ShipEntity ship, String name, int maxCount) {
		this.countProperty = new SimpleIntegerProperty(ship, name + "_count");
		this.maxCount = maxCount;
	}

	public ReadOnlyIntegerProperty countProperty() {
		return this.countProperty;
	}

	public int getCount() {
		return this.countProperty.get();
	}

	public void increment() {
		this.setCount(this.getCount() + 1);
	}

	public void decrement() {
		this.setCount(this.getCount() - 1);
	}

	public void add(int amount) {
		this.setCount(this.getCount() + amount);
	}

	public void setCount(int count) {
		this.setCountDirect(Util.clamp(count, 0, this.maxCount));
	}

	/**
	bypasses bounds checks.
	outside this class, only used by {@link ShipEntity} when a life is lost.
	negative life is how the ship keeps track of the fact that it's been destroyed.
	*/
	public void setCountDirect(int count) {
		this.countProperty.set(count);
	}
}