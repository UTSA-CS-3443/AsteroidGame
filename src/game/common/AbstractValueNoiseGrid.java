package game.common;

/**
common superinterface of {@link ValueGrid} and {@link NoiseGrid}
which defines all the overloads they both use.

@author Michael Johnston (tky886)
*/
public interface AbstractValueNoiseGrid {

	/** returns the value at this position. */
	public abstract float getValue(double x, double y);

	/**
	partial derivative with respect to x is added to out.partialDerivativeX.
	partial derivative with respect to y is added to out.partialDerivativeY.
	value is added to out.value.
	adding allows multiple grids to be stacked together.
	*/
	public abstract void getValueAndDerivative(double x, double y, ValueAndDerivativeHolder out);

	/**
	overload for {@link #getValuesX(double, double, double, float[], int, int)}
	which provides a default offset of 0.
	*/
	public default void getValuesX(double x, double y, double stepSize, float[] out, int length) {
		this.getValuesX(x, y, stepSize, out, 0, length);
	}

	/**
	overload for {@link #getValuesX(double, double, double, float[], int, int)}
	which provides a default stepSize of 1.
	*/
	public default void getValuesX(double x, double y, float[] out, int offset, int length) {
		this.getValuesX(x, y, 1.0D, out, offset, length);
	}

	/**
	overload for {@link #getValuesX(double, double, double, float[], int, int)}
	which provides a default stepSize of 1, and a default offset of 0.
	*/
	public default void getValuesX(double x, double y, float[] out, int length) {
		this.getValuesX(x, y, 1.0D, out, 0, length);
	}

	/**
	gets multiple values in bulk, starting at x and y,
	continuing in a line in the +x direction,
	and stopping after (length) steps.
	the distance between each step is stepSize.
	the values obtained are *added* to the "out" array,
	starting at the index held by offset.
	adding allows multiple grids to be stacked together.

	in general, this method will be faster than iterating over the
	required line manually and calling {@link #getValue} at every step.
	*/
	public abstract void getValuesX(double x, double y, double stepSize, float[] out, int offset, int length);

	/**
	overload for {@link #getValuesY(double, double, double, float[], int, int)}
	which provides a default offset of 0.
	*/
	public default void getValuesY(double x, double y, double stepSize, float[] out, int length) {
		this.getValuesY(x, y, stepSize, out, 0, length);
	}

	/**
	overload for {@link #getValuesY(double, double, double, float[], int, int)}
	which provides a default stepSize of 1.
	*/
	public default void getValuesY(double x, double y, float[] out, int offset, int length) {
		this.getValuesY(x, y, 1.0D, out, offset, length);
	}

	/**
	overload for {@link #getValuesY(double, double, double, float[], int, int)}
	which provides a default stepSize of 1, and a default offset of 0.
	*/
	public default void getValuesY(double x, double y, float[] out, int length) {
		this.getValuesY(x, y, 1.0D, out, 0, length);
	}

	/**
	gets multiple values in bulk, starting at x and y,
	continuing in a line in the +y direction,
	and stopping after (length) steps.
	the distance between each step is stepSize.
	the values obtained are *added* to the "out" array,
	starting at the index held by offset.
	adding allows multiple grids to be stacked together.

	in general, this method will be faster than iterating over the
	required line manually and calling {@link #getValue} at every step.
	*/
	public abstract void getValuesY(double x, double y, double stepSize, float[] out, int offset, int length);

	public static class ValueAndDerivativeHolder {

		public float value, partialDerivativeX, partialDerivativeY;
	}
}