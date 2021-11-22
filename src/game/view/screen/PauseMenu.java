package game.view.screen;

import game.controller.KeyboardShipController;
import game.controller.MenuHandler;
import game.controller.MouseShipController;
import game.view.GameView;
import game.view.Images;
import game.view.screen.ImageButton.ChoiceImageButton;
import game.view.screen.ImageButton.SingleImageButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** @author Michael Johnston (tky886) */
public class PauseMenu extends Menu {

	private final VBox rootComponent;

	public PauseMenu(MenuHandler menuHandler) {
		super(menuHandler);

		GameView gameView = menuHandler.gameView;
		ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>(
			gameView.game.ship.controller instanceof MouseShipController
			? InputMode.MOUSE
			: InputMode.KEYBOARD
		);
		inputMode.addListener((observable, oldValue, newValue) -> {
			switch (newValue) {
				case MOUSE:    gameView.setShipController(new    MouseShipController()); break;
				case KEYBOARD: gameView.setShipController(new KeyboardShipController()); break;
				default: throw new AssertionError(newValue);
			}
		});

		VBox guiElements = new VBox(
			64.0D, //spacing
			new SingleImageButton(
				Images.GUI_PLAY,
				Images.GUI_PLAY_HOVERED,
				Images.GUI_PLAY_PRESSED,
				event -> menuHandler.startClosing()
			)
			.getRootComponent(),
			/*
			//todo: implement upgrades screen.
			ImageButton.create(
				Util.getImage("gui/upgrades", 320, 64),
				Util.getImage("gui/upgrades_hovered", 320, 64),
				Util.getImage("gui/upgrades_pressed", 320, 64),
				event -> {}
			),
			*/
			/*
			//todo: implement settings screen too.
			ImageButton.create(
				Util.getImage("gui/settings", 320, 64),
				Util.getImage("gui/settings_hovered", 320, 64),
				Util.getImage("gui/settings_pressed", 320, 64),
				event -> {}
			)
			*/
			new HBox(
				new ImageView(Images.GUI_CONTROLS),
				new ChoiceImageButton<>(
					Images.GUI_MOUSE_CONTROL,
					Images.GUI_MOUSE_CONTROL_HOVERED,
					Images.GUI_MOUSE_CONTROL_PRESSED,
					inputMode,
					InputMode.MOUSE
				)
				.getRootComponent(),
				new ChoiceImageButton<>(
					Images.GUI_KEYBOARD_CONTROL,
					Images.GUI_KEYBOARD_CONTROL_HOVERED,
					Images.GUI_KEYBOARD_CONTROL_PRESSED,
					inputMode,
					InputMode.KEYBOARD
				)
				.getRootComponent()
			)
		);
		guiElements.setAlignment(Pos.CENTER);
		this.rootComponent = guiElements;
	}

	@Override
	public Node getRootComponent() {
		return this.rootComponent;
	}

	private static enum InputMode {
		MOUSE,
		KEYBOARD;
	}
}