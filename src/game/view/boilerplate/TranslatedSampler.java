package game.view.boilerplate;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

/**
simple helper for {@link Shader}'s to query an {@link Image} with an offset.

@author Michael Johnston (tky886)
*/
public class TranslatedSampler {

	private final PixelReader reader;
	private final int translationX, translationY;

	public TranslatedSampler(Image image, int sourceX, int sourceY, int destinationX, int destinationY) {
		this.reader = image.getPixelReader();
		this.translationX = sourceX - destinationX;
		this.translationY = sourceY - destinationY;
	}

	public TranslatedSampler(Image image, int destinationX, int destinationY) {
		this(image, 0, 0, destinationX, destinationY);
	}

	public int getArgb(int x, int y) {
		return this.reader.getArgb(x + this.translationX, y + this.translationY);
	}
}