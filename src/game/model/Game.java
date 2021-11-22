package game.model;

import java.util.Iterator;

import game.Main;
import game.model.entities.BackgroundEntity;
import game.model.entities.Entity;
import game.model.entities.ShipEntity;
import game.model.spawners.AsteroidSpawner;
import game.model.spawners.EntitySpawner;
import game.model.spawners.PlasmaPulseSpawner;
import game.model.spawners.UpgradeSpawner;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/** @author Michael Johnston (tky886) */
public class Game {

	/** minimum {@link #gameSpeed}. */
	public static final double MIN_GAME_SPEED = 1.0D;
	/** maximum {@link #gameSpeed}. */
	public static final double MAX_GAME_SPEED = 5.0D;

	public final EntityList entities = new EntityList();
	public final ShipEntity ship = new ShipEntity(Main.DEFAULT_WINDOW_WIDTH * 0.5D, Main.DEFAULT_WINDOW_HEIGHT - 32.0D);
	public BackgroundEntity background = new BackgroundEntity(0.0D, 0.0D);
	public final EntitySpawner[] spawners = {
		new AsteroidSpawner(),
		new UpgradeSpawner(),
		new PlasmaPulseSpawner(),
	};

	public final DoubleProperty width, height;
	/** time since the game started. */
	public double totalTime;
	/** time since the previous frame. */
	public double deltaTime;
	/**
	starts at {@link #MIN_GAME_SPEED}, and increases over time to make
	the game progressively harder. capped at {@link #MAX_GAME_SPEED}.
	*/
	public double gameSpeed = Main.DEBUG_MODE ? MAX_GAME_SPEED : MIN_GAME_SPEED;
	/**
	amount of time "stolen" by time warp upgrades.
	when time is stolen, it will be regained more quickly than normal.
	*/
	public double lostTime;

	public Game() {
		this.width  = new SimpleDoubleProperty(this, "width");
		this.height = new SimpleDoubleProperty(this, "height");
	}

	public void reset() {
		this.ship.reset(this.width.doubleValue() * 0.5D, this.height.doubleValue() - 32.0D);
		this.background = new BackgroundEntity(0.0D, 0.0D); //generate new seed, cause why not.
		this.entities.clear();
		for (EntitySpawner spawner : this.spawners) spawner.reset();
		this.totalTime = 0.0D;
		this.deltaTime = 0.0D;
		this.gameSpeed = Main.DEBUG_MODE ? MAX_GAME_SPEED : MIN_GAME_SPEED;
		this.lostTime = 0.0D;
	}

	public boolean tick(double deltaTime) {
		this.deltaTime = deltaTime;
		this.totalTime += deltaTime;
		if (this.ship.lives.getCount() >= 0) {
			double normalTime = deltaTime / 120.0D;
			double lostTime = Math.min(deltaTime / 20.0D, this.lostTime);
			this.gameSpeed = Math.min(this.gameSpeed + normalTime + lostTime, MAX_GAME_SPEED);
			this.lostTime -= lostTime;

			for (EntitySpawner spawner : this.spawners) {
				spawner.spawn(this);
			}

			this.background.tickMovement(this);
			this.ship.tickMovement(this);
			for (Entity entity : this.entities) {
				entity.tickMovement(this);
			}

			this.ship.tickInteraction(this);
			for (Iterator<Entity> iterator = this.entities.iterator(); iterator.hasNext(); ) {
				if (!iterator.next().tickInteraction(this)) {
					iterator.remove();
				}
			}
			return true;
		}
		else {
			//"pause" the game while displaying the ship's explosion animation.
			//we can't *actually* pause the game because that would require pausing the {@link GameTimer timer},
			//and pausing the timer means that no animations can play anymore, including the explosion.
			//so, instead we just don't tick other entities while the ship is exploding.
			return this.ship.tickExplosion(this);
		}
	}

	public double getScaledDeltaTime() {
		return this.deltaTime * this.gameSpeed;
	}
}