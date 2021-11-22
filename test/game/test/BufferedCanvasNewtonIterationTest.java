package game.test;

import org.junit.Test;

import static org.junit.Assert.fail;

public class BufferedCanvasNewtonIterationTest {

	@Test
	public void test() {
		for (int threads = 1; threads <= 1024; threads++) {
			for (int slice = 1; slice < threads; slice++) {
				newtonIterate(slice, threads, false);
			}
		}
	}

	public static void newtonIterate(int sliceIndex, int threadCount, boolean print) {
		final int iterations = 6;
		final double requiredPrecision = 0x1.0p-32;

		double target = ((double)(sliceIndex)) / ((double)(threadCount));
		double result = target;
		if (print) System.out.println("slice " + sliceIndex + '/' + threadCount + ": " + target);
		for (int iteration = 0; iteration < iterations; iteration++) {
			double commonFactor = Math.sqrt(result * (1.0D - result)); //used by value and derivative.
			double value = 0.5D - (Math.asin(1.0D - 2.0D * result) + (2.0D - 4.0D * result) * commonFactor) / Math.PI;
			double derivative = commonFactor * 8.0D / Math.PI;
			double correction = (value - target) / derivative;
			double nextResult = result - correction;
			if (print) {
				System.out.println("Iteration " + iteration + ": " + result + " - " + correction + " = " + nextResult);
				result = nextResult;
			}
			else {
				if (iteration == iterations - 1 && !(Math.abs(correction) <= requiredPrecision)) {
					newtonIterate(sliceIndex, threadCount, true);
					fail("More than " + iterations + " iterations required for result within " + requiredPrecision);
				}
				result = nextResult;
				if (!(result >= 0.0D && result <= 1.0D)) {
					newtonIterate(sliceIndex, threadCount, true);
					fail("Result not in expected interval: " + result);
				}
			}
		}
	}
}
