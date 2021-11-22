package game.common;

import java.util.Random;
import java.util.function.DoubleSupplier;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** @author Michael Johnston (tky886) */
public class Util {

	//////////////////////////////// some nice/useful curves ////////////////////////////////

	public static int square(int value) {
		return value * value;
	}

	public static float square(float value) {
		return value * value;
	}

	public static double square(double value) {
		return value * value;
	}

	public static int square(int value1, int value2) {
		return (value1 * value1) + (value2 * value2);
	}

	public static float square(float value1, float value2) {
		return (value1 * value1) + (value2 * value2);
	}

	public static double square(double value1, double value2) {
		return (value1 * value1) + (value2 * value2);
	}

	public static int square(int value1, int value2, int value3) {
		return (value1 * value1) + (value2 * value2) + (value3 * value3);
	}

	public static float square(float value1, float value2, float value3) {
		return (value1 * value1) + (value2 * value2) + (value3 * value3);
	}

	public static double square(double value1, double value2, double value3) {
		return (value1 * value1) + (value2 * value2) + (value3 * value3);
	}

	public static float smooth(float value) {
		return value * value * (value * -2.0F + 3.0F);
	}

	public static double smooth(double value) {
		return value * value * (value * -2.0D + 3.0D);
	}

	//////////////////////////////// interpolation ////////////////////////////////

	/** linear interpolation. */
	public static float mix(float low, float high, float value) {
		return (high - low) * value + low;
	}

	/** linear interpolation. */
	public static double mix(double low, double high, double value) {
		return (high - low) * value + low;
	}

	/** linear de-interpolation. */
	public static float unmix(float low, float high, float value) {
		return (value - low) / (high - low);
	}

	/** linear de-interpolation. */
	public static double unmix(double low, double high, double value) {
		return (value - low) / (high - low);
	}

	//////////////////////////////// rounding ////////////////////////////////

	/** faster than {@link Math#floor}, and returns an int instead of a double. */
	public static int floor(float value) {
		int floor = (int)(value); //will round towards 0
		return floor > value ? floor - 1 : floor;
	}

	/** faster than {@link Math#floor}, and returns an int instead of a double. */
	public static int floor(double value) {
		int floor = (int)(value); //will round towards 0
		return floor > value ? floor - 1 : floor;
	}

	/** faster than {@link Math#ceil}, and returns an int instead of a double. */
	public static int ceil(float value) {
		int ceil = (int)(value); //will round towards 0
		return ceil < value ? ceil + 1 : ceil;
	}

	/** faster than {@link Math#ceil}, and returns an int instead of a double. */
	public static int ceil(double value) {
		int ceil = (int)(value);
		return ceil < value ? ceil + 1 : ceil;
	}

	/** rounds towards the nearest int, with ties rounding up towards positive infinity. */
	public static int round(float value) {
		return floor(value + 0.5F);
	}

	/** rounds towards the nearest int, with ties rounding up towards positive infinity. */
	public static int round(double value) {
		return floor(value + 0.5D);
	}

	/**
	returns the floor or the ceil based on how close the value is to each.
	for example, a value of 3.6 has a 60% chance of returning 4, and a 40% chance of returning 3.
	*/
	public static int roundRandomly(float value, Random random) {
		return floor(value + random.nextFloat());
	}

	/**
	returns the floor or the ceil based on how close the value is to each.
	for example, a value of 3.6 has a 60% chance of returning 4, and a 40% chance of returning 3.
	*/
	public static int roundRandomly(double value, Random random) {
		return floor(value + random.nextDouble());
	}

	//////////////////////////////// RNG ////////////////////////////////
	//these static methods allow generation of pseudorandom numbers
	//without having to construct a new {@link Random} object.
	/////////////////////////////////////////////////////////////////////

	/** copy-paste of {@link Random#multiplier}. */
	public static final long MULTIPLIER = 0x5DEECE66DL;
	/** copy-paste of {@link Random#addend}. */
	public static final long ADDEND = 0xBL;

	public static long nextSeed(long seed) {
		return seed * MULTIPLIER + ADDEND;
	}

	public static long permute(long seed, int salt) {
		return seed + Long.rotateLeft((seed + salt) * MULTIPLIER, salt);
	}

	/** all int values are equally likely. this includes negative ints. */
	public static int nextInt(long seed) {
		return (int)(nextSeed(seed) >>> 16);
	}

	/** all long values are equally likely. this includes negative longs. */
	public static long nextLong(long seed) {
		long highBits = nextSeed(seed);
		long lowBits = nextSeed(highBits);
		return ((highBits << 16) & 0xFFFF_FFFF_0000_0000L) | ((lowBits >>> 16) & 0x0000_0000_FFFF_FFFFL);
	}

	/** returns in the range [0, 1). */
	public static float nextPositiveFloat(long seed) {
		//usage of 24 bits explained in java.util.Random.nextFloat().
		return (nextInt(seed) >>> (32 - 24)) * 0x1.0p-24F;
	}

	/** returns in the range (-1, 1). */
	public static float nextUniformFloat(long seed) {
		//usage of 24 bits explained in java.util.Random.nextFloat().
		//the 25'th bit is used as a sign bit.
		return (nextInt(seed) >> (32 - 25)) * 0x1.0p-24F;
	}

	/** returns in the range [0, 1). */
	public static double nextPositiveDouble(long seed) {
		//usage of 53 bits explained in java.util.Random.nextDouble().
		return (nextLong(seed) >>> (64 - 53)) * 0x1.0p-53D;
	}

	/** returns in the range (-1, 1). */
	public static double nextUniformDouble(long seed) {
		//usage of 53 bits explained in java.util.Random.nextDouble().
		//the 54'th bit is used as a sign bit.
		return (nextLong(seed) >> (64 - 54)) * 0x1.0p-53D;
	}

	//////////////////////////////// clamping ////////////////////////////////

	public static int clamp(int value, int min, int max) {
		if (value <= min) return min;
		if (value >= max) return max;
		return value;
	}

	public static float clamp(float value, float min, float max) {
		if (value <= min) return min;
		if (value >= max) return max;
		return value;
	}

	public static double clamp(double value, double min, double max) {
		if (value <= min) return min;
		if (value >= max) return max;
		return value;
	}

	//////////////////////////////// bits ////////////////////////////////
	public static int nextPowerOfTwo(int number) {
		number--;
		number |= number >>> 1;
		number |= number >>> 2;
		number |= number >>> 4;
		number |= number >>> 8;
		number |= number >>> 16;
		return number + 1;
	}

	//////////////////////////////// misc ////////////////////////////////

	/**
	throws a checked exception without declaring so in the method signature.
	this method declares that it returns a RuntimeException so
	that it can be used in combination with other throw statements,
	but it will never actually return anything.

	example use cases:

	throwing a checked exception from a lambda which itself is surrounded by
	a try block or a method which declares that it throws checked exceptions:

		void nullCheck(List<InputStream> inputs) throws IOException {
			inputs.forEach(input -> {
				if (input == null) {
					throw Util.throwExceptionUnsafely(new IOException("Found null stream in inputs list!"));
				}
			});
		}

	handling all caught throwables while preserving finally {} semantics.

		try {
			unsafeAction();
		}
		catch (Throwable throwable) {
			handleException(throwable);
			throw Util.throwExceptionUnsafely(throwable);
		}
	*/
	@SuppressWarnings("unchecked")
	public static <X extends Throwable> RuntimeException throwExceptionUnsafely(Throwable throwable) throws X {
		//type erasure will not perform any casting at runtime.
		throw (X)(throwable);
	}

	/**
	convenience {@link DoubleBinding} which allows
	its dependencies to be specified at creation time.
	they will be {@link #bind bound} during construction,
	and {@link #unbind unbound} on {@link #dispose disposal}.
	*/
	public static abstract class AbstractDoubleBinding extends DoubleBinding {

		private final Observable[] dependencies;
		private ObservableList<Observable> dependenciesView;

		public AbstractDoubleBinding(Observable... dependencies) {
			this.dependencies = dependencies;
			this.bind(dependencies);
		}

		public static AbstractDoubleBinding create(DoubleSupplier supplier, Observable... dependencies) {
			return new AbstractDoubleBinding(dependencies) {

				@Override
				protected double computeValue() {
					return supplier.getAsDouble();
				}
			};
		}

		@Override
		public ObservableList<?> getDependencies() {
			if (this.dependenciesView == null) {
				this.dependenciesView = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.dependencies));
			}
			return this.dependenciesView;
		}

		@Override
		public void dispose() {
			this.unbind(this.dependencies);
		}
	}
}