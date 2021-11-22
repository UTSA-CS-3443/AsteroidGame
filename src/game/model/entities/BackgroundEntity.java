package game.model.entities;

import java.util.Arrays;
import java.util.Random;

import game.common.NoiseGrid;
import game.model.Game;

/**
yes, I know it doesn't make much sense to think of the background as an entity,
but it does have a position and it does need to be ticked,
so... it's an entity now. deal with it.

@author Michael Johnston (tky886)
*/
public class BackgroundEntity extends Entity {

	private static final Random RANDOM = new Random();

	public final NoiseGrid noise;

	public BackgroundEntity(double x, double y) {
		super(x, y);
		this.noise = new NoiseGrid(RANDOM.nextLong(), 1024.0D, 0.5D, 0.1875F, 0.625F, 10);
	}

	public float getNoiseValue(double x, double y) {
		return this.noise.getValue(x, y) + 0.5F;
	}

	public void getNoiseValuesX(double x, double y, int length, float[] out) {
		Arrays.fill(out, 0, length, 0.5F);
		this.noise.getValuesX(x, y, out, length);
	}

	@Override
	public void tickMovement(Game game) {
		this.y += game.getScaledDeltaTime() * 32.0D;
	}

	@Override
	public boolean tickInteraction(Game game) {
		return true;
	}
}