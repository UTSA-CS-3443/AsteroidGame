package game.model.spawners;

import java.util.Random;

import game.Main;
import game.common.Interpolator;
import game.common.RandomNumberSupplier;
import game.model.Game;
import game.model.entities.AsteroidEntity;

/**
@see AsteroidEntity

@author Michael Johnston (tky886)
*/
public class AsteroidSpawner implements EntitySpawner {

	private static final Random RANDOM = new Random();
	private static final RandomNumberSupplier DELAY = new RandomNumberSupplier(RANDOM, Interpolator.linear(1.0D, 4.0D));

	private double nextTime = DELAY.next();

	@Override
	public void spawn(Game game) {
		if (game.totalTime >= this.nextTime) {
			AsteroidEntity asteroid = new AsteroidEntity(
				RANDOM.nextDouble() * game.width.doubleValue(),
				0.0D,
				Main.DEBUG_MODE ? 1.0D : Math.min(game.totalTime / 120.0D + 0.25D, 1.0D)
			);
			asteroid.y -= asteroid.size;
			game.entities.addEntity(asteroid);
			this.nextTime += DELAY.next() / game.gameSpeed;
		}
	}

	@Override
	public void reset() {
		this.nextTime = DELAY.next();
	}
}