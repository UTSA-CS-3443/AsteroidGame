package game.model.spawners;

import game.model.Game;
import game.model.entities.PlasmaPulseEntity;

/**
@see PlasmaPulseEntity

@author Michael Johnston (tky886)
*/
public class PlasmaPulseSpawner implements EntitySpawner {

	private double nextTime;

	/**
	pure randomness can sometimes (often in fact)
	produce pulses which travel in similar directions.
	this makes the distribution feel oddly uneven.
	a bayer pattern produces much more even results,
	and looks a lot better in my opinion.
	tl;dr: I made it less random to make it feel more random.
	*/
	private short randomAngle;

	@Override
	public void spawn(Game game) {
		if (game.totalTime >= this.nextTime) {
			double angle = (Integer.reverse(++this.randomAngle) >> 16) * 0x1.0p-20F;
			this.spawn(game, angle);
			if (game.ship.wideSpreadTime > 0.0D) {
				this.spawn(game, angle - 0.5D);
				this.spawn(game, angle + 0.5D);
			}
			this.nextTime += game.ship.fireRate.getStatValue();
		}
	}

	public void spawn(Game game, double angle) {
		PlasmaPulseEntity pulse = new PlasmaPulseEntity(
			game.ship.x,
			game.ship.y,
			Math.sin(angle) *  256.0D,
			Math.cos(angle) * -256.0D,
			game.ship.firePower.getCount(),
			game.ship.firePower.getStatValue()
		);
		game.entities.addEntity(pulse);
	}

	@Override
	public void reset() {
		this.nextTime = 0.0D;
		this.randomAngle = 0;
	}
}