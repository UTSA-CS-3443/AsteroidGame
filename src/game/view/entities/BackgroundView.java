package game.view.entities;

import java.util.stream.IntStream;

import game.model.entities.BackgroundEntity;
import game.view.GameView;
import game.view.boilerplate.BufferedCanvas;
import game.view.boilerplate.Dithering;
import game.view.boilerplate.PixelBuffer;

/** @author Michael Johnston (tky886) */
public class BackgroundView implements EntityView<BackgroundEntity> {

	public static final BackgroundView INSTANCE = new BackgroundView();

	@Override
	public void render(BackgroundEntity background, GameView gameView) {
		//normally when using a Shader, the Context's perThreadStorage
		//is intended to be mutated for every pixel drawn.
		//but in this case the data we would normally want
		//to store there encodes a whole row of noise values.
		//this is not *quite* what Shader was designed for.
		//I could check if context.x == 0 and populate the values then,
		//but this just feels... kind of hacky to me.
		//so instead I just inlined most of the logic used by {@link BufferedCanvas#runShaderSquare}.
		//
		//plus, the background is *the* most time-consuming entity to render due
		//to the number of pixels it draws and the number of noise layers it has.
		//inlining this logic will avoid allocating Context objects,
		//getting fields from that context, and checking if x == 0 for every pixel.
		//this probably won't make it *much* faster,
		//but I kind of want to save every nanosecond I can.
		PixelBuffer pixels = gameView.canvas.pixels;
		int width   = pixels.width.intValue();
		int height  = pixels.height.intValue();
		int threads = Math.min(BufferedCanvas.THREAD_COUNT, height);
		IntStream.range(0, BufferedCanvas.THREAD_COUNT).parallel().forEach(thread -> {
			int minY = height * thread / threads;
			int maxY = height * (thread + 1) / threads;
			float[] values = new float[width];
			for (int y = minY; y < maxY; y++) {
				int baseOffset = pixels.rowOffset(y);
				background.getNoiseValuesX(-background.x, y - background.y, width, values);
				for (int x = 0; x < width; x++) {
					float value = values[x];
					assert value >= 0.0F && value <= 1.0F;
					float commonFactor = value * (2.0F - value) * 255.0F; //used by the red and blue channels.
					float dithering = Dithering.getFloat(x, y);
					pixels.setRGB(
						baseOffset,
						(int)(value * commonFactor + dithering),
						(int)(value * value * 255.0F + dithering),
						(int)(commonFactor + dithering)
					);
					baseOffset += PixelBuffer.BYTES_PER_PIXEL;
				}
			}
		});
	}
}