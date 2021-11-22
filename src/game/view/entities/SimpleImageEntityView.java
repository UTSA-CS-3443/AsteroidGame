package game.view.entities;

import game.model.entities.Entity;
import game.view.GameView;
import javafx.scene.image.Image;

/**
view for all entities that are just drawn from an image.

@author Michael Johnston (tky886)
*/
public abstract class SimpleImageEntityView<E extends Entity> implements EntityView<E> {

	public static <E extends Entity> SimpleImageEntityView<E> of(Image image) {
		return new SimpleImageEntityView<E>() {

			@Override
			public Image getImage(E entity) {
				return image;
			}
		};
	}

	public abstract Image getImage(E entity);

	@Override
	public void render(E entity, GameView gameView) {
		Image image = this.getImage(entity);
		gameView.canvas.drawImage(
			(int)(entity.x - image.getWidth() * 0.5D),
			(int)(entity.y - image.getHeight() * 0.5D),
			image
		);
	}
}