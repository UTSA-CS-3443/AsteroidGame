package game.view.screen;

import game.controller.MenuHandler;
import game.view.Images;
import game.view.screen.ImageButton.SingleImageButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/** @author Michael Johnston (tky886) */
public class GameOverMenu extends Menu {

	private final VBox rootComponent;

	public GameOverMenu(MenuHandler menuHandler) {
		super(menuHandler);
		this.rootComponent = new VBox(
			new ImageView(Images.GUI_GAME_OVER),
			new SingleImageButton(
				Images.GUI_PLAY_AGAIN,
				Images.GUI_PLAY_AGAIN_HOVERED,
				Images.GUI_PLAY_AGAIN_PRESSED,
				event -> this.menuHandler.startClosing()
			)
			.getRootComponent()
		);
		this.rootComponent.setAlignment(Pos.CENTER);
	}

	@Override
	public Node getRootComponent() {
		return this.rootComponent;
	}

	@Override
	public void startClose() {
		this.menuHandler.gameView.game.reset();
	}

	@Override
	public void finishClose() {
		this.menuHandler.setMenu(new PauseMenu(this.menuHandler));
	}
}