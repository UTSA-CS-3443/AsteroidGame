package game.common;

/**
not to be confused with {@link javafx.animation.Interpolator}:

the primary method in {@link javafx.animation.Interpolator},
curve(), is expected to output in the range 0 to 1,
and curve() is also a protected method.
the interpolate() methods in javafx.animation.Interpolator
expect the caller to provide the desired output range.

{@link game.common.Interpolator} specifies the output range at creation time,
and the primary method, interpolate(), is public.

@author Michael Johnston (tky886)
*/
public interface Interpolator {

	public abstract double interpolate(double frac);

	public static Interpolator linear(double min, double max) {
		double diff = max - min;
		return frac -> frac * diff + min;
	}

	public static Interpolator exponential(double min, double max) {
		double logMin = Math.log(min);
		double logMax = Math.log(max);
		double logDiff = logMax - logMin;
		return frac -> Math.exp(frac * logDiff + logMin);
	}
}