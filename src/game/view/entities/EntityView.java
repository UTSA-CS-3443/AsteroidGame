package game.view.entities;

import game.model.entities.Entity;
import game.view.GameView;

/**
if I were doing this in my own project,
I would just make the entities responsible for rendering themselves via polymorphism.
but unfortunately I am required to use the MVC design pattern,
which means I can't make the view a property or task of the model.
so, I have a separate interface for that instead.
every type of Entity should have an associated EntityView,
and these views are registered in {@link EntityViewRegistry}.

@author Michael Johnston (tky886)
*/
public interface EntityView<E extends Entity> {

	/**
	draws the entity on the gameView's {@link GameView#canvas}.
	the GameView itself is provided for cases where rendering the entity
	requires knowing about other properties of the {@link GameView#game}.
	*/
	public abstract void render(E entity, GameView gameView);
}