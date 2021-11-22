package game.view.boilerplate;

import java.util.stream.IntStream;

import game.common.Util;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;

/**
basically, when you call Canvas.getGraphicsContext2D().getPixelWriter().setArgb(),
it doesn't actually set the color.
instead, it adds this request to a queue to process later.
processing this queue is very slow for single-pixel requests.
it's faster for bulk operations.
so, this class acts as an intermediate buffer to store pixels directly,
and flush them once per frame.

@author Michael Johnston (tky886)
*/
public class BufferedCanvas {

	/** the number of threads used for shaders. */
	public static final int THREAD_COUNT;
	/**
	in {@link #runShaderCircle}, we want to ensure that
	every thread is doing the same amount of work.
	this ensures that they all finish at (roughly) the same time,
	and no threads are left idly waiting on other threads.
	(assuming the shader itself runs in constant time, anyway.)
	therefore, our goal here is to cut a circle into
	horizontal slices such that each slice has the same area.

	this array holds the positions of the slice bounds.
	since there are {@link #THREAD_COUNT} slices,
	this array has a length of {@link #THREAD_COUNT} + 1
	to mark the end of the last slice (which is always 1.0).
	*/
	private static final double[] CIRCLE_SLICE_POSITIONS;
	static {
		THREAD_COUNT = Runtime.getRuntime().availableProcessors();
		double[] slices = new double[THREAD_COUNT + 1];

		//the total area for a slice is the integral of
		//	sqrt(x * (1 - x)) * 8/pi
		//on the interval from the slice's starting position to its ending position.
		//I multiply by 8/pi to ensure that the semicircle
		//described by the above function has a total area of 1.
		//this integral evaluates to
		//	0.5 - (asin(1 - 2x) + (2 - 4x) * sqrt(x * (1 - x))) / pi
		//
		//so, if we want to know where to position the slices
		//so that this function equals a specific value,
		//we'd need to find an inverse of this function.
		//unfortunately, such an inverse cannot be
		//represented with standard mathematical operators.
		//but we can get a pretty darn close approximation for it using newton iteration.
		//
		//6 iterations has been tested and verified to give results
		//within 2 ^ -32 of the correct answer for up to 1024 threads.
		//see BufferedCanvasNewtonIterationTest.
		for (int sliceIndex = 1; sliceIndex < THREAD_COUNT; sliceIndex++) {
			double target = ((double)(sliceIndex)) / ((double)(THREAD_COUNT));
			double result = target;
			for (int iteration = 0; iteration < 6; iteration++) {
				double commonFactor = Math.sqrt(result * (1.0D - result)); //used by both value and derivative.
				double value = 0.5D - (Math.asin(1.0D - 2.0D * result) + (2.0D - 4.0D * result) * commonFactor) / Math.PI;
				double derivative = commonFactor * 8.0D / Math.PI;
				result -= (value - target) / derivative;
			}
			slices[sliceIndex] = result;
		}
		slices[THREAD_COUNT] = 1.0D;
		CIRCLE_SLICE_POSITIONS = slices;
	}

	public final DoubleProperty width, height;
	public final Canvas canvas;
	public final PixelBuffer pixels;

	public BufferedCanvas() {
		this(0.0D, 0.0D);
	}

	public BufferedCanvas(double width, double height) {
		this.width  = new SimpleDoubleProperty(this, "width", width);
		this.height = new SimpleDoubleProperty(this, "height", height);

		this.canvas = new Canvas(width, height);
		this.pixels = new PixelBuffer(width, height);

		this.canvas.widthProperty().bind(this.width);
		this.canvas.heightProperty().bind(this.height);
		this.pixels.width.bind(this.width);
		this.pixels.height.bind(this.height);

		PixelFormat<?> format = this.canvas.getGraphicsContext2D().getPixelWriter().getPixelFormat();
		if (format != PixelBuffer.FORMAT) {
			System.err.println("Canvas has an unexpected pixel format: " + format + ". Framerate may decrease as a result.");
		}
	}

	/** copies the contents of our pixel buffer to our wrapped canvas. */
	public void flush() {
		this.canvas.getGraphicsContext2D().getPixelWriter().setPixels(
			0,
			0,
			this.width.intValue(),
			this.height.intValue(),
			PixelBuffer.FORMAT,
			this.pixels.getBackingArray(),
			0,
			this.pixels.getScanlineStride()
		);
	}

	/**
	draws the provided image at the provided coordinates, using standard alpha blending.
	if a different blend mode is desired, consider using a {@link Shader} instead.
	see the documentation on {@link Shader} for more information on how shaders work.
	*/
	public void drawImage(int x, int y, Image image) {
		this.drawImage(x, y, 0, 0, (int)(image.getWidth()), (int)(image.getHeight()), image);
	}

	/**
	draws the provided region of the image at the provided coordinates, using standard alpha blending.
	if a different blend mode is desired, consider using a {@link Shader} instead.
	see the documentation on {@link Shader} for more information on how shaders work.
	*/
	public void drawImage(int x, int y, int imageStartX, int imageStartY, int imageWidth, int imageHeight, Image image) {
		TranslatedSampler sampler = new TranslatedSampler(image, imageStartX, imageStartY, x, y);
		this.runShaderSquare(x, y, x + imageWidth, y + imageHeight, context -> {
			int color = sampler.getArgb(context.x, context.y);
				context.buffer.blendRGB(
				context.baseOffset,
				(color >>> 16) & 255,
				(color >>>  8) & 255,
				(color       ) & 255,
				(color >>> 24)
			);
		});
	}

	/**
	runs the shader in a square area.
	see the documentation on {@link Shader} for more information on how shaders work.
	*/
	public void runShaderSquare(int minX, int minY, int maxX, int maxY, Shader shader) {
		minX = Math.max(minX, 0);
		minY = Math.max(minY, 0);
		maxX = Math.min(maxX, this.width.intValue());
		maxY = Math.min(maxY, this.height.intValue());
		if (maxX > minX && maxY > minY) {
			this.runShaderSquareUnchecked(minX, minY, maxX, maxY, shader);
		}
	}

	/** skips bounds checks. */
	private void runShaderSquareUnchecked(int minX, int minY, int maxX, int maxY, Shader shader) {
		int threads = Math.min(Runtime.getRuntime().availableProcessors(), maxY - minY);
		IntStream.range(0, threads).parallel().forEach(thread -> {
			int threadMinY = minY + (maxY - minY) * thread / threads;
			int threadMaxY = minY + (maxY - minY) * (thread + 1) / threads;
			Shader.Context context = new Shader.Context(this.pixels);
			for (int y = threadMinY; y < threadMaxY; y++) {
				for (context.startRow(minX, y); context.x < maxX; context.moveRight()) {
					shader.run(context);
				}
			}
		});
	}

	/**
	runs the shader in a circular area.
	see the documentation on {@link Shader} for more information on how shaders work.
	*/
	public void runShaderCircle(double centerX, double centerY, double radius, Shader shader) {
		this.runShaderEllipse(centerX, centerY, radius, radius, shader);
	}

	/**
	runs the shader in an elliptical area.
	see the documentation on {@link Shader} for more information on how shaders work.
	*/
	public void runShaderEllipse(double centerX, double centerY, double radiusX, double radiusY, Shader shader) {
		int minY = Math.max(Util.ceil(centerY - radiusY), 0);
		int maxY = Math.min(Util.floor(centerY + radiusY) + 1, this.height.intValue());
		if (maxY > minY) {
			IntStream.range(0, THREAD_COUNT).parallel().forEach(thread -> {
				int threadMinY = Util.round(minY + (maxY - minY) * CIRCLE_SLICE_POSITIONS[thread]);
				int threadMaxY = Util.round(minY + (maxY - minY) * CIRCLE_SLICE_POSITIONS[thread + 1]);
				Shader.Context context = new Shader.Context(this.pixels);
				for (int y = threadMinY; y < threadMaxY; y++) {
					double rowRadius = Math.sqrt(1.0D - Util.square((y - centerY) / radiusY)) * radiusX;
					int rowMinX = Math.max(Util.ceil(centerX - rowRadius), 0);
					int rowMaxX = Math.min(Util.floor(centerX + rowRadius) + 1, this.width.intValue());
					for (context.startRow(rowMinX, y); context.x < rowMaxX; context.moveRight()) {
						shader.run(context);
					}
				}
			});
		}
	}
}