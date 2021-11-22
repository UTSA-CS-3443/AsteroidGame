package game.controller;

import game.model.entities.ShipEntity;
import game.view.GameView;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

/** @author Michael Johnston (tky886) */
public class KeyboardShipController implements EventHandler<KeyEvent>, ShipController {

	private static final byte
		FORWARD  = 1 << 0,
		BACKWARD = 1 << 1,
		LEFT     = 1 << 2,
		RIGHT    = 1 << 3;

	/** packed to alternate between x and y. */
	private static final double[] VELOCITY_LOOKUP = new double[16 * 2];
	static {
		final double diagonal = 1.0D / Math.sqrt(2.0D);
		for (int i = 0; i < 16; i++) {
			double x = 0.0D;
			double y = 0.0D;
			if ((i & FORWARD ) != 0) y -= 1.0D;
			if ((i & BACKWARD) != 0) y += 1.0D;
			if ((i & LEFT    ) != 0) x -= 1.0D;
			if ((i & RIGHT   ) != 0) x += 1.0D;
			if (x != 0.0D && y != 0.0D) {
				x *= diagonal;
				y *= diagonal;
			}
			VELOCITY_LOOKUP[i << 1] = x;
			VELOCITY_LOOKUP[(i << 1) + 1] = y;
		}
	}

	private byte state;

	@Override
	public void updateTargetVelocity(ShipEntity ship) {
		double speed = ship.speed.getStatValue();
		int index = this.state << 1;
		ship.targetVelocityX = VELOCITY_LOOKUP[index] * speed;
		ship.targetVelocityY = VELOCITY_LOOKUP[index + 1] * speed;
	}

	@Override
	public void handle(KeyEvent event) {
		boolean isPressing;
		if (event.getEventType() == KeyEvent.KEY_PRESSED) isPressing = true;
		else if (event.getEventType() == KeyEvent.KEY_RELEASED) isPressing = false;
		else return;

		byte bit;
		switch (event.getCode()) {
			case W: bit = FORWARD;  break;
			case A: bit = LEFT;     break;
			case S: bit = BACKWARD; break;
			case D: bit = RIGHT;    break;
			default: return;
		}

		this.state = (byte)(isPressing ? this.state | bit : this.state & ~bit);
	}

	@Override
	public void install(GameView gameView) {
		gameView.canvas.canvas.addEventHandler(KeyEvent.KEY_PRESSED,  this);
		gameView.canvas.canvas.addEventHandler(KeyEvent.KEY_RELEASED, this);
	}

	@Override
	public void uninstall(GameView gameView) {
		gameView.canvas.canvas.removeEventHandler(KeyEvent.KEY_PRESSED,  this);
		gameView.canvas.canvas.removeEventHandler(KeyEvent.KEY_RELEASED, this);
	}
}