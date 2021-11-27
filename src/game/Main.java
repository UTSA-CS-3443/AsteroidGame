package game;

import game.controller.MenuHandler;
import game.controller.MouseShipController;
import game.model.Game;
import game.view.GameView;
import game.view.Images;
import game.view.entities.EntityViewRegistry;
import game.view.menu.IngameOverlayView;
import javafx.application.Application;
import javafx.beans.binding.DoubleExpression;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** @author Michael Johnston (tky886) */
public class Main extends Application {

	public static final double
		DEFAULT_WINDOW_WIDTH  = 400.0D,
		DEFAULT_WINDOW_HEIGHT = 800.0D;

	/**
	when enabled, the {@link Game} starts at max {@link Game#gameSpeed speed},
	and the {@link game.model.entities.ShipEntity ship}
	starts with max {@link game.model.entities.ShipEntity#stats stats}.
	*/
	public static final boolean DEBUG_MODE = false;

	@Override
	public void start(Stage primaryStage) {
		Game game = new Game();

		GameView gameView = new GameView(game);
		gameView.setShipController(new MouseShipController());
		gameView.canvas.canvas.relocate(0.0D, IngameOverlayView.MIN_HEIGHT);

		IngameOverlayView overlay = new IngameOverlayView(game);
		overlay.canvas.canvas.relocate(0.0D, 0.0D);

		Pane rootPane = new Pane(gameView.canvas.canvas, overlay.canvas.canvas);
		Scene scene   = new Scene(rootPane, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

		DoubleExpression width      = scene.widthProperty();
		DoubleExpression fullHeight = scene.heightProperty();
		DoubleExpression gameHeight = fullHeight.subtract((double)(IngameOverlayView.MIN_HEIGHT));

		game    .width .bind(width);
		game    .height.bind(gameHeight);
		gameView.width .bind(width);
		gameView.height.bind(gameHeight);

		//constructor will also bind overlay width and height.
		MenuHandler menuHandler = new MenuHandler(rootPane, gameView, overlay);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, menuHandler);

		primaryStage.setScene(scene);
		primaryStage.setMinWidth(200.0D);
		primaryStage.setMinHeight(200.0D);
		primaryStage.getIcons().setAll(Images.SHIP);
		primaryStage.show();

		//debug information. useful in development.
		/*
		menuHandler.gameTimer.getFPS().addListener((observable, oldValue, newValue) -> {
			primaryStage.setTitle(newValue + " FPS");
		});
		//*/
	}

	public static void main(String[] args) {
		Images.clinit();
		EntityViewRegistry.clinit();
		launch(args);
	}
}