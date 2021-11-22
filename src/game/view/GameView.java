package game.view;

import game.controller.ShipController;
import game.model.Game;
import game.model.entities.ShipEntity;
import game.view.boilerplate.BufferedCanvasView;
import game.view.entities.BackgroundView;
import game.view.entities.EntityViewRegistry;
import game.view.entities.ShipView;
import javafx.scene.Cursor;

/**
renderer for the {@link Game} as a whole.
this only requires rendering the game's entities.

@author Michael Johnston (tky886)
*/
public class GameView extends BufferedCanvasView {

	public final Game game;

	public GameView(Game game) {
		this.game = game;
		//required for {@link KeyboardShipController}
		//to be able to receive keyboard events.
		this.canvas.canvas.setFocusTraversable(true);
		this.canvas.canvas.setCursor(Cursor.CROSSHAIR);
	}

	@Override
	protected void doRender() {
		BackgroundView.INSTANCE.render(this.game.background, this);
		this.game.entities.forEach(entity -> EntityViewRegistry.get(entity).render(entity, this));
		ShipView.INSTANCE.render(this.game.ship, this);
	}

	public void setShipController(ShipController controller) {
		ShipEntity ship = this.game.ship;
		if (ship.controller != null) {
			ship.controller.uninstall(this);
		}
		controller.install(this);
		ship.controller = controller;
	}
}