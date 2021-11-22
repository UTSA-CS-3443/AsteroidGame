package game.view.menu;

import game.Main;
import game.common.Util;
import game.model.Game;
import game.model.entities.ShipEntity;
import game.view.Images;
import game.view.boilerplate.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;

/**
display's the ship's stats at the top of the screen.
when the user pauses the game, this overlay expands to cover the whole screen,
and forms the background for other {@link Menu}'s.
@see game.controller.MenuHandler

@author Michael Johnston (tky886)
*/
public class IngameOverlayView extends BufferedCanvasView {

	public static final int MIN_HEIGHT = 56;

	public final Game game;
	public int iconOpacity;

	public IngameOverlayView(Game game) {
		super(Main.DEFAULT_WINDOW_WIDTH, MIN_HEIGHT);
		this.game = game;
		this.iconOpacity = 0;

		ChangeListener<Number> onStatChange = (observable, oldValue, newValue) -> this.render();
		game.ship.speed    .countProperty().addListener(onStatChange);
		game.ship.fireRate .countProperty().addListener(onStatChange);
		game.ship.firePower.countProperty().addListener(onStatChange);
		game.ship.lives    .countProperty().addListener(onStatChange);
	}

	@Override
	protected void doRender() {
		BufferedCanvas canvas = this.canvas;
		PixelBuffer pixels = canvas.pixels;

		int width  = this.width .intValue();
		int height = this.height.intValue();
		float reciprocalMaxY = 1.0F / ((float)(height - 1));

		for (int y = 0; y < height; y++) {
			float frac = Util.square(y * reciprocalMaxY);
			float brightness = Util.mix(0.125F, 0.25F, frac) * 255.0F;
			int baseOffset = pixels.rowOffset(y);
			for (int x = 0; x < width; x++) {
				pixels.setGrayscale(baseOffset, (int)(brightness + Dithering.getFloat(x, y)));
				baseOffset += PixelBuffer.BYTES_PER_PIXEL;
			}
		}
		if (this.iconOpacity > 0) {
			ShipEntity ship = this.game.ship;
			for (int i = 0, count = ship.firePower.getCount(); i <= count; i++) {
				this.drawStatIcon(i * 16 + 8, height - 48, Images.FIRE_POWER_STAT);
			}
			for (int i = 0, count = ship.fireRate.getCount(); i <= count; i++) {
				this.drawStatIcon(i * 16 + 8, height - 24, Images.FIRE_RATE_STAT);
			}
			for (int i = 0, count = ship.speed.getCount(); i <= count; i++) {
				this.drawStatIcon(width - i * 16 - 24, height - 48, Images.SPEED_STAT);
			}
			for (int i = 0, count = ship.lives.getCount(); i <= count; i++) {
				this.drawStatIcon(width - i * 32 - 24, height - 24, Images.EXTRA_LIFE_STAT);
			}
		}
	}

	private void drawStatIcon(int x, int y, Image image) {
		int opacity = this.iconOpacity;
		if (opacity <= 0) return;
		if (opacity >= 255) {
			this.canvas.drawImage(x, y, image);
			return;
		}
		TranslatedSampler sampler = new TranslatedSampler(image, 0, 0, x, y);
		this.canvas.runShaderSquare(x, y, x + 16, y + 16, context -> {
			int color = sampler.getArgb(context.x, context.y);
			context.buffer.blendRGB(
				context.baseOffset,
				(color >>> 16) & 255,
				(color >>>  8) & 255,
				(color       ) & 255,
				((color >>> 24) * opacity + 127) / 255
			);
		});
	}
}