package game.view.entities;

import game.common.Util;
import game.model.entities.ShipEntity;
import game.view.GameView;
import game.view.Images;
import game.view.boilerplate.PixelBuffer;
import game.view.boilerplate.TranslatedSampler;

/** @author Michael Johnston (tky886) */
public class ShipView implements EntityView<ShipEntity> {

	/**
	number of frames in {@link Images#EXPLOSION}.
	the frames are arranged horizontally left to right.
	*/
	private static final int EXPLOSION_FRAMES = 9;
	private static final double EXPLOSION_FRAMES_PER_SECOND = EXPLOSION_FRAMES / 0.75D;
	public static final ShipView INSTANCE = new ShipView();

	@Override
	public void render(ShipEntity ship, GameView gameView) {
		int startX = (int)(ship.x - Images.SHIP.getWidth()  * 0.5D);
		int startY = (int)(ship.y - Images.SHIP.getHeight() * 0.5D);
		if (ship.lives.getCount() < 0) {
			int frame = (int)(ship.destroyedTime * EXPLOSION_FRAMES_PER_SECOND);
			if (frame < EXPLOSION_FRAMES) {
				gameView.canvas.drawImage(startX, startY, frame * 64, 0, 64, 64, Images.EXPLOSION);
			}
		}
		else if (ship.ghostTime <= 0.0D) {
			gameView.canvas.drawImage(startX, startY, Images.SHIP);
		}
		else if (ship.ghostTime >= 5.0D) {
			gameView.canvas.drawImage(startX, startY, Images.SHIP_GHOST);
		}
		else {
			double scaledTime = ship.ghostTime * 0.2D;
			double opacityD = Util.mix(
				1.0D - Util.square(scaledTime),
				Util.square(1.0D - scaledTime),
				Math.cos(10.5D * 2.0D * Math.PI * scaledTime) * -0.5D + 0.5D
			);
			int opacityI = (int)(opacityD * 255.0D);
			TranslatedSampler      opaqueSampler = new TranslatedSampler(Images.SHIP,       startX, startY);
			TranslatedSampler translucentSampler = new TranslatedSampler(Images.SHIP_GHOST, startX, startY);
			gameView.canvas.runShaderSquare(
				startX,
				startY,
				startX + ((int)(Images.SHIP.getWidth())),
				startY + ((int)(Images.SHIP.getHeight())),
				context -> {
					int      opaqueColor =      opaqueSampler.getArgb(context.x, context.y);
					int translucentColor = translucentSampler.getArgb(context.x, context.y);
					if (opaqueColor >>> 24 == 0 && translucentColor >>> 24 == 0) return; //from the shader lambda.
					context.buffer.blendRGB(
						context.baseOffset,
						PixelBuffer.blend((translucentColor >>> 16) & 255, (opaqueColor >>> 16) & 255, opacityI),
						PixelBuffer.blend((translucentColor >>>  8) & 255, (opaqueColor >>>  8) & 255, opacityI),
						PixelBuffer.blend((translucentColor       ) & 255, (opaqueColor       ) & 255, opacityI),
						PixelBuffer.blend((translucentColor >>> 24),       (opaqueColor >>> 24),       opacityI)
					);
				}
			);
		}
	}
}