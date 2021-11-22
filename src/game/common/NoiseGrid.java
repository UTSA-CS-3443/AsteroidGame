package game.common;

/**
represents the sum of several {@link ValueGrid}'s.

@author Michael Johnston (tky886)
*/
public class NoiseGrid implements AbstractValueNoiseGrid {

	private final ValueGrid[] layers;

	public NoiseGrid(ValueGrid... layers) {
		this.layers = layers;
	}

	/** utility constructor which generates the layers dynamically. */
	public NoiseGrid(
		long seed,
		double startSize,
		double sizeScaleFactor,
		float startAmplitude,
		float amplitudeScaleFactor,
		int layerCount
	) {
		this.layers = new ValueGrid[layerCount];
		for (int i = 0; i < layerCount; i++) {
			this.layers[i] = new ValueGrid(Util.permute(seed, i), startSize, startAmplitude);
			startSize *= sizeScaleFactor;
			startAmplitude *= amplitudeScaleFactor;
		}
	}

	@Override
	public float getValue(double x, double y) {
		float sum = 0.0F;
		for (ValueGrid layer : this.layers) {
			sum += layer.getValue(x, y);
		}
		return sum;
	}

	@Override
	public void getValuesX(double x, double y, double stepSize, float[] out, int offset, int length) {
		for (ValueGrid layer : this.layers) {
			layer.getValuesX(x, y, stepSize, out, offset, length);
		}
	}

	@Override
	public void getValuesY(double x, double y, double stepSize, float[] out, int offset, int length) {
		for (ValueGrid layer : this.layers) {
			layer.getValuesX(x, y, stepSize, out, offset, length);
		}
	}

	@Override
	public void getValueAndDerivative(double x, double y, ValueAndDerivativeHolder out) {
		for (ValueGrid layer : this.layers) {
			layer.getValueAndDerivative(x, y, out);
		}
	}

	public int getLayerCount() {
		return this.layers.length;
	}

	public ValueGrid getLayer(int index) {
		return this.layers[index];
	}
}