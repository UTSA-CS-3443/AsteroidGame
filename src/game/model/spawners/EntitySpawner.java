package game.model.spawners;

import game.model.Game;

/** @author Michael Johnston (tky886) */
public interface EntitySpawner {

	/**
	called once per {@link Game#tick} to add new entities to the game.
	however, implementors do not need to spawn things every tick,
	and may keep a timer which tracks how long it should wait before spawning something.
	*/
	public abstract void spawn(Game game);

	/**
	called when the game ends and is restarted to
	reset any internal state this spawner may have.
	*/
	public abstract void reset();
}