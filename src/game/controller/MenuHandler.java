package game.controller;

import game.common.AbstractIntegerBinding;
import game.common.Util;
import game.common.AbstractDoubleBinding;
import game.view.GameView;
import game.view.boilerplate.BetterTransition;
import game.view.boilerplate.DelayedRunnable;
import game.view.menu.IngameOverlayView;
import game.view.menu.PauseMenu;
import game.view.menu.Menu;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
controller which handles positioning of both the
in-game overlay and any menus the user has open,
since the position is tied to the overlay's height.

the menu handler has 2 "states": open and closed.
open means the current {@link #menu} is visible and the gray
background covers the window, hiding the {@link #gameView}.
closed means the current {@link #menu} is outside the
bounds of the window, and is not visible to the user.
in the closed state, only the overlay is visible.
it is possible to be in neither of these states
if we are currently transitioning between them.
transitions take 0.25 seconds by default.

@author Michael Johnston (tky886)
*/
public class MenuHandler implements EventHandler<KeyEvent> {

	public final Pane rootPane;
	public final GameView gameView;
	public final IngameOverlayView overlay;
	public final GameTimer gameTimer;

	public final BetterTransition transition;
	private final VerticalFracProperty verticalFrac;

	private final DelayedRunnable layout;
	private Menu menu;

	public MenuHandler(Pane rootPane, GameView gameView, IngameOverlayView overlay) {
		this.rootPane  = rootPane;
		this.gameView  = gameView;
		this.overlay   = overlay;
		this.gameTimer = new GameTimer(this); //todo: I don't like mutual constructor dependencies. see if I can resolve this.
		VerticalFracProperty frac = this.verticalFrac = this.new VerticalFracProperty(1.0D);
		this.layout = new DelayedRunnable(this::doLayout);
		this.transition = BetterTransition.create(frac::set);
		this.transition.changeCycleDuration(Duration.seconds(0.25D));

		overlay.width.bind(rootPane.widthProperty());
		ReadOnlyDoubleProperty maxHeight = rootPane.heightProperty();
		overlay.height.bind(AbstractDoubleBinding.create(
			() -> Util.mix(IngameOverlayView.MIN_HEIGHT, maxHeight.doubleValue(), frac.doubleValue()),
			maxHeight, frac
		));
		overlay.iconOpacity.bind(AbstractIntegerBinding.create(
			() -> Util.round((1.0F - frac.floatValue()) * 255.0F),
			frac
		));
		ChangeListener<Number> layout = (observable, oldValue, newValue) -> this.layout();
		overlay.width .addListener(layout);
		overlay.height.addListener(layout);
		//don't need to add listener to frac too because height will change whenever frac does.

		this.setMenu(new PauseMenu(this));
	}

	/**
	sets the menu to display.
	if the state is currently open, the displayed menu will change instantly.
	if the state is currently closed, the user will not
	notice anything changed until they pause the game.
	*/
	public void setMenu(Menu menu) {
		if (this.menu != null) { //will be null during initialization.
			this.rootPane.getChildren().remove(this.menu.getRootComponent());
		}
		this.menu = menu;
		this.rootPane.getChildren().add(menu.getRootComponent());
		this.doLayout();
	}

	/**
	sets the menu to display and starts the transition
	to the open state if the stat is not currently open.
	*/
	public void openMenu(Menu menu) {
		this.setMenu(menu);
		this.startOpening();
	}

	public void layout() {
		this.layout.runLater();
	}

	private void doLayout() {
		double x = (this.overlay.width.doubleValue() - this.menu.getRootComponent().getLayoutBounds().getWidth()) * 0.5D;
		//center = (rootPane.height - menuToDisplay.height) * 0.5
		//offset = overlay.height - rootPane.height
		//center + offset = (rootPane.height - menuToDisplay.height) * 0.5 + overlay.height - rootPane.height
		//= rootPane.height * 0.5 - menuToDisplay.height * 0.5 + overlay.height - rootPane.height
		//= rootPane.height * 0.5 - rootPane.height - menuToDisplay.height * 0.5 + overlay.height
		//= rootPane.height * -0.5 - menuToDisplay.height * 0.5 + overlay.height
		//= overlay.height - (rootPane.height + menuToDisplay.height) * 0.5
		double y = this.overlay.height.doubleValue() - (this.rootPane.getHeight() + this.menu.getRootComponent().getLayoutBounds().getHeight()) * 0.5D;
		this.menu.getRootComponent().relocate(x, y);
	}

	@Override
	public void handle(KeyEvent event) {
		if (event.getCode() == KeyCode.ESCAPE) {
			double frac = this.verticalFrac.get();
			if (frac == 0.0D) {
				this.startOpening0();
			}
			else if (frac == 1.0D) {
				this.startClosing0();
			}
		}
	}

	/** starts transitioning to the open state if we are currently closed. */
	public void startOpening() {
		//don't permit multiple transitions playing simultaneously.
		if (this.isClosed()) this.startOpening0();
	}

	private void startOpening0() {
		this.transition.addSingleUseFinishListener(event -> this.gameTimer.pause());
		this.transition.playForwardsFromStart();
	}

	/** starts transitioning to the closed state if we are currently open. */
	public void startClosing() {
		//don't permit multiple transitions playing simultaneously.
		if (this.isOpen()) this.startClosing0();
	}

	private void startClosing0() {
		this.menu.startClose();
		this.transition.addSingleUseFinishListener(event -> this.menu.finishClose());
		this.gameTimer.play();
		this.transition.playBackwardsFromEnd();
	}

	public ReadOnlyDoubleProperty verticalFracProperty() {
		return this.verticalFrac;
	}

	public double getVerticalFrac() {
		return this.verticalFrac.get();
	}

	public boolean isOpen() {
		return this.verticalFrac.get() == 1.0D;
	}

	public boolean isClosed() {
		return this.verticalFrac.get() == 0.0D;
	}

	private class VerticalFracProperty extends ReadOnlyDoublePropertyBase {

		private double frac;

		public VerticalFracProperty() {}

		public VerticalFracProperty(double frac) {
			this.frac = frac;
		}

		@Override
		public Object getBean() {
			return MenuHandler.this;
		}

		@Override
		public String getName() {
			return "verticalFrac";
		}

		@Override
		public double get() {
			return this.frac;
		}

		void set(double frac) {
			if (this.frac != frac) {
				this.frac = frac;
				this.fireValueChangedEvent();
			}
		}
	}
}