package game.controller;

import game.common.Util;
import game.model.entities.ShipEntity;
import game.view.GameView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/** @author Michael Johnston (tky886) */
public class MouseShipController implements ShipController, EventHandler<MouseEvent> {

	private double targetX, targetY;

	@Override
	public void updateTargetVelocity(ShipEntity ship) {
		double relativeX = this.targetX - ship.x;
		double relativeY = this.targetY - ship.y;
		double distanceSquaredToTarget = Util.square(relativeX, relativeY);
		double distanceToTarget = Math.sqrt(distanceSquaredToTarget);
		double scaleFactor = ship.speed.getStatValue() * distanceToTarget / (distanceSquaredToTarget + 1024.0D);
		ship.targetVelocityX = relativeX * scaleFactor;
		ship.targetVelocityY = relativeY * scaleFactor;
	}

	@Override
	public void handle(MouseEvent event) {
		this.targetX = event.getX();
		this.targetY = event.getY();
	}

	@Override
	public void install(GameView gameView) {
		gameView.canvas.canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this);
	}

	@Override
	public void uninstall(GameView gameView) {
		gameView.canvas.canvas.removeEventHandler(MouseEvent.MOUSE_MOVED, this);
	}
}