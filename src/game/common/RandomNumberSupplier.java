package game.common;

import java.util.Random;

/**
holder for a {@link Random} which provides numbers
with a specific range and, optionally, distribution.
the range and distribution are determined by the provided {@link Interpolator}.

@author Michael Johnston (tky886)
*/
public class RandomNumberSupplier {

	public Random random;
	public Interpolator interpolator;

	public RandomNumberSupplier(Interpolator interpolator) {
		this(new Random(), interpolator);
	}

	public RandomNumberSupplier(Random random, Interpolator interpolator) {
		this.random = random;
		this.interpolator = interpolator;
	}

	public double interpolate(double frac) {
		return this.interpolator.interpolate(frac);
	}

	public double next() {
		return this.interpolate(this.random.nextDouble());
	}
}