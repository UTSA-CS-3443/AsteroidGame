package game.common;

/**
represents a grid where each grid point has a random number between -{@link #amplitude} and {@link #amplitude}.
attempting to query the value between 2 or 4 grid points will
interpolate the values of the surrounding grid points smoothly.

@author Michael Johnston (tky886)
*/
public class ValueGrid implements AbstractValueNoiseGrid {

	public final long seed;
	/** distance between grid points. */
	public final double size;
	public final double reciprocalSize;
	public final float amplitude;

	public ValueGrid(long seed, double size, float amplitude) {
		assert size > 0.0D && amplitude > 0.0F;
		this.seed = seed;
		this.size = size;
		this.reciprocalSize = 1.0D / size;
		this.amplitude = amplitude;
	}

	public float getRawValue(int gridX, int gridY) {
		long bits = this.seed;
		bits = Util.permute(bits, gridX);
		bits = Util.permute(bits, gridY);
		return Util.nextUniformFloat(bits);
	}

	@Override
	public float getValue(double x, double y) {
		//grid points
		int x0 = Util.floor(x * this.reciprocalSize);
		int y0 = Util.floor(y * this.reciprocalSize);
		int x1 = x0 + 1;
		int y1 = y0 + 1;
		//fractional position within the cell
		float fracX = (float)((x - x0 * this.size) * this.reciprocalSize);
		float fracY = (float)((y - y0 * this.size) * this.reciprocalSize);
		float smoothFracX = Util.smooth(fracX);
		float smoothFracY = Util.smooth(fracY);
		//values
		float v00 = this.getRawValue(x0, y0);
		float v01 = this.getRawValue(x0, y1);
		float v10 = this.getRawValue(x1, y0);
		float v11 = this.getRawValue(x1, y1);
		//interpolate!
		return Util.mix(
			Util.mix(v00, v01, smoothFracY),
			Util.mix(v10, v11, smoothFracY),
			smoothFracX
		) * this.amplitude;
	}

	private float interpolateYOnly(int x, int y0, int y1, float smoothFracY) {
		return Util.mix(
			this.getRawValue(x, y0),
			this.getRawValue(x, y1),
			smoothFracY
		) * this.amplitude;
	}

	@Override
	public void getValuesX(double x, double y, double stepSize, float[] out, int offset, int length) {
		if (stepSize > this.size) {
			throw new IllegalArgumentException("stepSize ("+ stepSize + ") must be less than or equal to grid size (" + this.size + ')');
		}
		if (offset < 0) throw new ArrayIndexOutOfBoundsException(offset);
		int endIndex = offset + length;
		if (endIndex > out.length) throw new ArrayIndexOutOfBoundsException(endIndex - 1);

		int gridX = Util.floor(x * this.reciprocalSize);
		int y0    = Util.floor(y * this.reciprocalSize);
		int y1    = y0 + 1;
		float fracX = (float)((x - gridX * this.size) * this.reciprocalSize);
		float fracY = (float)((y - y0    * this.size) * this.reciprocalSize);
		float fracIncrement = (float)(this.reciprocalSize * stepSize);
		float smoothFracY = Util.smooth(fracY);

		float value0 = this.interpolateYOnly(  gridX, y0, y1, smoothFracY);
		float value1 = this.interpolateYOnly(++gridX, y0, y1, smoothFracY);
		for (int index = offset; true /* break in the middle of the loop */;) {
			out[index] += Util.mix(value0, value1, Util.smooth(fracX));
			if (++index >= endIndex) break;
			if ((fracX += fracIncrement) >= 1.0F) {
				fracX -= 1.0F;
				value0 = value1;
				value1 = this.interpolateYOnly(++gridX, y0, y1, smoothFracY);
			}
		}
	}

	private float interpolateXOnly(int x0, int x1, int y, float smoothFracX) {
		return Util.mix(
			this.getRawValue(x0, y),
			this.getRawValue(x1, y),
			smoothFracX
		) * this.amplitude;
	}

	@Override
	public void getValuesY(double x, double y, double stepSize, float[] out, int offset, int length) {
		if (stepSize > this.size) {
			throw new IllegalArgumentException("stepSize ("+ stepSize + ") must be less than or equal to grid size (" + this.size + ')');
		}
		if (offset < 0) throw new ArrayIndexOutOfBoundsException(offset);
		int endIndex = offset + length;
		if (endIndex > out.length) throw new ArrayIndexOutOfBoundsException(endIndex - 1);

		int gridY = Util.floor(y * this.reciprocalSize);
		int x0    = Util.floor(x * this.reciprocalSize);
		int x1    = x0 + 1;
		float fracX = (float)((x - x0    * this.size) * this.reciprocalSize);
		float fracY = (float)((y - gridY * this.size) * this.reciprocalSize);
		float fracIncrement = (float)(this.reciprocalSize * stepSize);
		float smoothFracX = Util.smooth(fracX);

		float value0 = this.interpolateXOnly(x0, x1,   gridY, smoothFracX);
		float value1 = this.interpolateXOnly(x0, x1, ++gridY, smoothFracX);
		for (int index = offset; true /* break in the middle of the loop */;) {
			out[index] += Util.mix(value0, value1, Util.smooth(fracY));
			if (++index >= endIndex) break;
			if ((fracY += fracIncrement) >= 1.0F) {
				fracY -= 1.0F;
				value0 = value1;
				value1 = this.interpolateXOnly(x0, x1, ++gridY, smoothFracX);
			}
		}
	}

	@Override
	public void getValueAndDerivative(double x, double y, ValueAndDerivativeHolder out) {
		//grid points
		int x0 = Util.floor(x * this.reciprocalSize);
		int y0 = Util.floor(y * this.reciprocalSize);
		int x1 = x0 + 1;
		int y1 = y0 + 1;
		//fractional position within the cell
		float fracX = (float)((x - x0 * this.size) * this.reciprocalSize);
		float fracY = (float)((y - y0 * this.size) * this.reciprocalSize);
		float smoothFracX = Util.smooth(fracX);
		float smoothFracY = Util.smooth(fracY);
		float smoothFracX2 = Util.smooth(1.0F - Math.abs(fracX * 2.0F - 1.0F));
		float smoothFracY2 = Util.smooth(1.0F - Math.abs(fracY * 2.0F - 1.0F));
		//values
		float v00 = this.getRawValue(x0, y0);
		float v01 = this.getRawValue(x0, y1);
		float v10 = this.getRawValue(x1, y0);
		float v11 = this.getRawValue(x1, y1);
		//partial interpolation
		float px0 = Util.mix(v00, v01, smoothFracY);
		float px1 = Util.mix(v10, v11, smoothFracY);
		float py0 = Util.mix(v00, v10, smoothFracX);
		float py1 = Util.mix(v01, v11, smoothFracX);
		//full interpolation
		//note: this is not the true derivative.
		//I am cheating here to make it a bit more smooth near cell borders.
		float derivativeScale = this.amplitude * ((float)(this.reciprocalSize));
		out.partialDerivativeX += (px1 - px0) * smoothFracX2 * derivativeScale;
		out.partialDerivativeY += (py1 - py0) * smoothFracY2 * derivativeScale;
		out.value += Util.mix(px0, px1, smoothFracX) * this.amplitude;
	}
}