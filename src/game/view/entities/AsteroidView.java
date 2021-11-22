package game.view.entities;

import game.common.AbstractValueNoiseGrid.ValueAndDerivativeHolder;
import game.common.Util;
import game.common.VoronoiGrid;
import game.common.VoronoiGrid.SeedPoint;
import game.model.entities.AsteroidEntity;
import game.view.GameView;
import game.view.boilerplate.PixelBuffer;

/** @author Michael Johnston (tky886) */
public class AsteroidView implements EntityView<AsteroidEntity> {

	private static final float NEGATIVE_RECIPROCAL_SQRT_2 = (float)(-1.0D / Math.sqrt(2.0D));

	public static final AsteroidView INSTANCE = new AsteroidView();

	@Override
	public void render(AsteroidEntity asteroid, GameView gameView) {
		gameView.canvas.runShaderCircle(asteroid.x, asteroid.y, asteroid.size, context -> {
			ShaderThreadVariables variables = context.getPerThreadStorage();
			if (variables == null) variables = context.setPerThreadStorage(new ShaderThreadVariables(asteroid.shatterNoise));

			if (asteroid.getSurfaceNormal(context.x - asteroid.x, context.y - asteroid.y, variables.seedPoint, variables.surfaceVec)) {
				float brightness = (variables.surfaceVec.partialDerivativeX + variables.surfaceVec.partialDerivativeY) * NEGATIVE_RECIPROCAL_SQRT_2;
				brightness = brightness * 0.5F + 0.5F;
				brightness *= Util.mix(0.5F, 2.0F - brightness, asteroid.brightness);
				context.buffer.setGrayscale(context.baseOffset, PixelBuffer.f2i(brightness));
			}
		});
	}

	private static class ShaderThreadVariables {

		final ValueAndDerivativeHolder surfaceVec;
		final SeedPoint seedPoint;

		public ShaderThreadVariables(VoronoiGrid grid) {
			this.surfaceVec = new ValueAndDerivativeHolder();
			this.seedPoint = grid.new SeedPoint();
		}
	}
}