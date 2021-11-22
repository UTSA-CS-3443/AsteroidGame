package game.view.entities;

import game.common.Util;
import game.model.entities.PlasmaPulseEntity;
import game.model.entities.ShipEntity;
import game.view.GameView;
import game.view.boilerplate.PixelBuffer;

/** @author Michael Johnston (tky886) */
public class PlasmaPulseView implements EntityView<PlasmaPulseEntity> {

	/**
	current {@link Colors} to use for plasma pulses.
	in the future, I want to allow the user to
	customize the color of their plasma pulses,
	but for right now the colors are hard-coded.
	*/
	public static Colors COLORS = Colors.RED_YELLOW;
	public static final PlasmaPulseView INSTANCE = new PlasmaPulseView();

	@Override
	public void render(PlasmaPulseEntity pulse, GameView gameView) {
		double headRadius = pulse.getSize();
		//center = average of (position + direction * headRadius) and (position - direction * headRadius * TAIL_LENGTH)
		//= ((position + direction * headRadius) + (position - direction * headRadius * TAIL_LENGTH)) / 2
		//= (position + direction * headRadius + position - direction * headRadius * TAIL_LENGTH) / 2
		//= (position * 2 + direction * headRadius - direction * headRadius * TAIL_LENGTH) / 2
		//= position + (direction * headRadius - direction * headRadius * TAIL_LENGTH) / 2
		//= position + direction * headRadius * (1 - TAIL_LENGTH) / 2
		//
		//combined radius = -headRadius * (1 - TAIL_LENGTH) / 2 + headRadius
		//= headRadius * (TAIL_LENGTH - 1) / 2 + headRadius
		//= headRadius * ((TAIL_LENGTH - 1) / 2 + 1)
		gameView.canvas.runShaderCircle(
			pulse.x + pulse.directionX * headRadius * ((1.0D - PlasmaPulseEntity.TAIL_LENGTH) * 0.5D),
			pulse.y + pulse.directionY * headRadius * ((1.0D - PlasmaPulseEntity.TAIL_LENGTH) * 0.5D),
			headRadius * ((PlasmaPulseEntity.TAIL_LENGTH - 1.0D) * 0.5D + 1.0D),
			context -> {
				double forwardComponent  = ((context.x - pulse.x) * pulse.directionX + (context.y - pulse.y) * pulse.directionY) / headRadius;
				double sidewaysComponent = ((context.x - pulse.x) * pulse.directionY - (context.y - pulse.y) * pulse.directionX) / headRadius;
				float intensity;
				if (forwardComponent >= 0.0D) {
					intensity = 1.0F - ((float)(Math.sqrt(Util.square(forwardComponent, sidewaysComponent))));
					if (intensity <= 0.0F) return; //from shader lambda
					intensity = Util.square(intensity);
				}
				else {
					double thickness = 1.0D + forwardComponent * (1.0D / PlasmaPulseEntity.TAIL_LENGTH);
					if (thickness <= 0.0D) return; //from shader lambda
					thickness = Util.smooth(thickness);
					intensity = 1.0F - ((float)(Math.abs(sidewaysComponent) / thickness));
					if (intensity <= 0.0F) return; //from shader lambda
					intensity = Util.square(intensity) * (float)(thickness);
				}
				float power = pulse.powerLevel * (1.0F / ShipEntity.MAX_FIRE_POWER_UPGRADES);
				context.buffer.blendRGB(
					context.baseOffset,
					PixelBuffer.f2i(COLORS.get(intensity, power, 0)),
					PixelBuffer.f2i(COLORS.get(intensity, power, 1)),
					PixelBuffer.f2i(COLORS.get(intensity, power, 2)),
					PixelBuffer.f2i(intensity)
				);
			}
		);
	}

	/**
	holds colors to be used for drawing plasma pulses.
	the color is computed as a function of power and intensity,
	where power is the number of {@link ShipEntity#firePower} upgrades the ship has,
	and intensity is how close the current pixel is to the center of the pulse.
	*/
	public static class Colors {

		public static final Colors RED_YELLOW = new Colors(
			new float[] { 1.0F, 0.0F, 0.0F },
			new float[] { 1.0F, 0.0F, 0.0F },
			new float[] { 1.0F, 0.25F, 0.25F },
			new float[] { 1.0F, 0.875F, 0.5625F }
		);
		/**
		legacy colors. these were the first I picked,
		but I changed them to {@link #RED_YELLOW} at some point later.
		*/
		public static final Colors GREEN_YELLOW_CYAN_WHITE = new Colors(
			new float[] { 0.25F, 1.0F, 0.25F },
			new float[] { 0.25F, 1.0F, 1.0F },
			new float[] { 1.0F, 1.0F, 0.25F },
			new float[] { 1.0F, 1.0F, 1.0F }
		);

		public float[] edgeLowPower;
		public float[] edgeHighPower;
		public float[] centerLowPower;
		public float[] centerHighPower;

		public Colors(
			float[] edgeLowPower,
			float[] edgeHighPower,
			float[] centerLowPower,
			float[] centerHighPower
		) {
			assert edgeLowPower.length == 3;
			assert edgeHighPower.length == 3;
			assert centerLowPower.length == 3;
			assert centerHighPower.length == 3;
			this.edgeLowPower = edgeLowPower;
			this.edgeHighPower = edgeHighPower;
			this.centerLowPower = centerLowPower;
			this.centerHighPower = centerHighPower;
		}

		public float get(float intensity, float power, int component) {
			return Util.mix(
				Util.mix(
					this.edgeLowPower[component],
					this.edgeHighPower[component],
					power
				),
				Util.mix(
					this.centerLowPower[component],
					this.centerHighPower[component],
					power
				),
				intensity
			);
		}
	}
}