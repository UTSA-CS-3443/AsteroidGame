package game.controller;

import game.model.entities.ShipEntity;
import game.view.GameView;

/**
common superinterface for {@link MouseShipController} and {@link KeyboardShipController}.
a ShipController is responsible for listening
to user input and using this information to
determine where the user wants the ship to move.

note that the ship controller does not actually move the ship.
instead, it tells the ship where it *should* move,
and the ship will perform this movement when it {@link ShipEntity#tickMovement ticks}.

@author Michael Johnston (tky886)
*/
public interface ShipController {

	/**
	called when the ship is ready to process input.
	the controller is expected to update the ship's
	{@link ShipEntity#targetVelocityX} and {@link ShipEntity#targetVelocityY}
	fields depending on where the user wants the ship to move.
	implementing classes define how the user indicates this desire.
	*/
	public abstract void updateTargetVelocity(ShipEntity ship);

	/**
	adds any event handlers required to know where the user wants
	the ship to move to the gameView's {@link GameView#canvas}.
	*/
	public abstract void install(GameView gameView);

	/**
	removes any event handlers that were previously {@link #install installed}
	in preparation for this controller being replaced with a different one.
	*/
	public abstract void uninstall(GameView gameView);
}