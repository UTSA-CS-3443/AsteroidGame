package game.view.boilerplate;

import java.nio.ByteBuffer;

import game.common.Util;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;

/**
the internal pixel storage for {@link BufferedCanvas}

@author Michael Johnston (tky886)
*/
public class PixelBuffer {

	public static final byte[] EMPTY_BYTE_ARRAY = {};

	public static final int BYTES_PER_PIXEL = 4;
	/** 1 << BYTES_PER_PIXEL_SHIFT == BYTES_PER_PIXEL */
	public static final int BYTES_PER_PIXEL_SHIFT = 2;
	public static final int   RED_OFFSET = 2;
	public static final int GREEN_OFFSET = 1;
	public static final int  BLUE_OFFSET = 0;
	public static final int ALPHA_OFFSET = 3;
	/** matches GraphicsContext.getPixelWriter().getPixelFormat(). */
	public static final WritablePixelFormat<ByteBuffer> FORMAT = PixelFormat.getByteBgraPreInstance();

	public final DoubleProperty width, height;
	private byte[] pixels = EMPTY_BYTE_ARRAY;
	/** smallest power of two which is greater than or equal to width and height. */
	private int roundedWidth, roundedHeight;
	/**
	1 << widthShift == roundedWidth
	heightShift is not necessary for anything.
	*/
	private int widthShift;

	public PixelBuffer() {
		this(0.0D, 0.0D);
	}

	public PixelBuffer(double width, double height) {
		this.width  = new SimpleDoubleProperty(this, "width",  width );
		this.height = new SimpleDoubleProperty(this, "height", height);
		this.width.addListener((observable, oldValue, newValue) -> this.updateSize(newValue.intValue(), this.height.intValue()));
		this.height.addListener((observable, oldValue, newValue) -> this.updateSize(this.width.intValue(), newValue.intValue()));
		this.updateSize((int)(width), (int)(height));
	}

	private void updateSize(int width, int height) {
		//can sometimes happen while the window is still setting itself up.
		if (width <= 0 || height <= 0) return;

		int roundedWidth  = Util.nextPowerOfTwo(width);
		int roundedHeight = Util.nextPowerOfTwo(height);
		int widthShift    = Integer.numberOfTrailingZeros(roundedWidth);
		int heightShift   = Integer.numberOfTrailingZeros(roundedHeight);
		boolean arrayChanged = roundedWidth != this.roundedWidth || roundedHeight != this.roundedHeight;
		if (arrayChanged) {
			int totalShift = widthShift + heightShift + BYTES_PER_PIXEL_SHIFT;
			if (totalShift >= 31) throw new OutOfMemoryError("Cannot allocate a backing array with dimensions " + roundedWidth + 'x' + roundedHeight + " (requested: " + width + 'x' + height + ')');
			int byteLength = 1 << totalShift;
			byte[] pixels = this.pixels = new byte[byteLength];
			//set alpha immediately.
			for (int index = ALPHA_OFFSET; index < byteLength; index += BYTES_PER_PIXEL) {
				pixels[index] = -1;
			}
		}
		this.roundedWidth  = roundedWidth;
		this.roundedHeight = roundedHeight;
		this.widthShift    = widthShift;
	}

	public byte[] getBackingArray() {
		return this.pixels;
	}

	public int getRoundedWidth() {
		return this.roundedWidth;
	}

	public int getRoundedHeight() {
		return this.roundedHeight;
	}

	public int getScanlineStride() {
		return this.roundedWidth << BYTES_PER_PIXEL_SHIFT;
	}

	public static float i2f(int brightness) {
		return ((float)(brightness)) / 255.0F;
	}

	public static double i2d(int brightness) {
		return ((double)(brightness)) / 255.0D;
	}

	public static int f2i(float brightness) {
		return (int)(brightness * 255.0F);
	}

	public static int d2i(double brightness) {
		return (int)(brightness * 255.0D);
	}

	public static byte clamp(int component) {
		return (byte)(Util.clamp(component, 0, 255));
	}

	/** returns the index in our backing array where this pixel starts. */
	public int baseOffset(int x, int y) {
		return ((y << this.widthShift) | x) << BYTES_PER_PIXEL_SHIFT;
	}

	/** returns the index in our backing array where this row starts. */
	public int rowOffset(int y) {
		return y << (this.widthShift + BYTES_PER_PIXEL_SHIFT);
	}

	public int getRed(int baseOffset) {
		return this.pixels[baseOffset | RED_OFFSET] & 255;
	}

	public int getGreen(int baseOffset) {
		return this.pixels[baseOffset | GREEN_OFFSET] & 255;
	}

	public int getBlue(int baseOffset) {
		return this.pixels[baseOffset | BLUE_OFFSET] & 255;
	}

	public void setRed(int baseOffset, int red) {
		this.pixels[baseOffset | RED_OFFSET] = clamp(red);
	}

	public void setGreen(int baseOffset, int green) {
		this.pixels[baseOffset | GREEN_OFFSET] = clamp(green);
	}

	public void setBlue(int baseOffset, int blue) {
		this.pixels[baseOffset | BLUE_OFFSET] = clamp(blue);
	}

	public int getRed(int x, int y) {
		return this.getRed(this.baseOffset(x, y));
	}

	public int getGreen(int x, int y) {
		return this.getGreen(this.baseOffset(x, y));
	}

	public int getBlue(int x, int y) {
		return this.getBlue(this.baseOffset(x, y));
	}

	public int getARGB(int x, int y) {
		int baseOffset = this.baseOffset(x, y);
		return (
			(255                       << 24) |
			(this.getRed  (baseOffset) << 16) |
			(this.getGreen(baseOffset) <<  8) |
			(this.getBlue (baseOffset)      )
		);
	}

	public void setRed(int x, int y, int red) {
		this.setRed(this.baseOffset(x, y), red);
	}

	public void setGreen(int x, int y, int green) {
		this.setGreen(this.baseOffset(x, y), green);
	}

	public void setBlue(int x, int y, int blue) {
		this.setBlue(this.baseOffset(x, y), blue);
	}

	public void setRGB(int baseOffset, int red, int green, int blue) {
		this.setRed  (baseOffset, red);
		this.setGreen(baseOffset, green);
		this.setBlue (baseOffset, blue);
	}

	public void setRGB(int x, int y, int red, int green, int blue) {
		this.setRGB(this.baseOffset(x, y), red, green, blue);
	}

	public void setGrayscale(int baseOffset, int brightness) {
		this.pixels[baseOffset |   RED_OFFSET] =
		this.pixels[baseOffset | GREEN_OFFSET] =
		this.pixels[baseOffset |  BLUE_OFFSET] =
		clamp(brightness);
	}

	public void setGrayscale(int x, int y, int brightness) {
		this.setGrayscale(this.baseOffset(x, y), brightness);
	}

	public void addRGB(int baseOffset, int red, int green, int blue) {
		this.setRed  (baseOffset, this.getRed  (baseOffset) + red);
		this.setGreen(baseOffset, this.getGreen(baseOffset) + green);
		this.setBlue (baseOffset, this.getBlue (baseOffset) + blue);
	}

	public void addRGB(int x, int y, int red, int green, int blue) {
		this.addRGB(this.baseOffset(x, y), red, green, blue);
	}

	public void addGrayscale(int baseOffset, int brightness) {
		this.addRGB(baseOffset, brightness, brightness, brightness);
	}

	public void addGrayscale(int x, int y, int brightness) {
		this.addGrayscale(this.baseOffset(x, y), brightness);
	}

	public static int multiply(int a, int b) {
		return (a * b + 127) / 255;
	}

	public void multiplyRGB(int baseOffset, int red, int green, int blue) {
		this.setRed  (baseOffset, multiply(this.getRed  (baseOffset), red));
		this.setGreen(baseOffset, multiply(this.getGreen(baseOffset), green));
		this.setBlue (baseOffset, multiply(this.getBlue (baseOffset), blue));
	}

	public void multiplyRGB(int x, int y, int red, int green, int blue) {
		this.multiplyRGB(this.baseOffset(x, y), red, green, blue);
	}

	public void multiplyGrayscale(int baseOffset, int brightness) {
		this.multiplyRGB(baseOffset, brightness, brightness, brightness);
	}

	public void multiplyGrayscale(int x, int y, int brightness) {
		this.multiplyGrayscale(this.baseOffset(x, y), brightness);
	}

	public static int blend(int oldValue, int newValue, int alpha) {
		return oldValue + ((newValue - oldValue) * alpha + 127) / 255;
	}

	public void blendRGB(int baseOffset, int red, int green, int blue, int alpha) {
		if (alpha <= 0) return;
		if (alpha >= 255) {
			this.setRGB(baseOffset, red, green, blue);
			return;
		}
		this.setRed  (baseOffset, blend(this.getRed  (baseOffset), red,   alpha));
		this.setGreen(baseOffset, blend(this.getGreen(baseOffset), green, alpha));
		this.setBlue (baseOffset, blend(this.getBlue (baseOffset), blue,  alpha));
	}

	public void blendRGB(int x, int y, int red, int green, int blue, int alpha) {
		this.blendRGB(this.baseOffset(x, y), red, green, blue, alpha);
	}
}