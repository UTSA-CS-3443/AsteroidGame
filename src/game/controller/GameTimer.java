package game.controller;

import game.view.GameView;
import game.view.boilerplate.BetterTransition;
import game.view.menu.GameOverMenu;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.util.Duration;

/**
I couldn't find a built-in subclass of
Animation which continues progressing forever.
setting the cycle count to {@link Animation#INDEFINITE} kind of works,
but then it still starts over once per second.
so as a workaround, I check if the current
frac is less than the previous frac.
if it is, then it means we just started over.
since the cycle duration is one second,
starting over means we jumped one second into the past.
so I add an extra second to compensate.

@author Michael Johnston (tky886)
*/
public class GameTimer extends Transition {

	public final MenuHandler menuHandler;
	public final GameView gameView;

	private double prevFrac;
	private int framesSinceLastSecond;
	private FPSProperty fps;

	public GameTimer(MenuHandler menuHandler) {
		this.menuHandler = menuHandler;
		this.gameView = menuHandler.gameView;
		this.setCycleDuration(Duration.seconds(1.0D));
		this.setCycleCount(Animation.INDEFINITE);
		this.setInterpolator(Interpolator.LINEAR);
	}

	public ReadOnlyIntegerProperty getFPS() {
		if (this.fps == null) {
			this.fps = this.new FPSProperty();
		}
		return this.fps;
	}

	@Override
	protected void interpolate(double frac) {
		double deltaTime = frac - this.prevFrac;
		if (deltaTime < 0.0D) { //rollover to the next second.
			deltaTime += 1.0D;
			if (this.fps != null) this.fps.set(this.framesSinceLastSecond);
			this.framesSinceLastSecond = 0;
		}

		if (!this.gameView.game.tick(deltaTime)) {
			BetterTransition transition = this.menuHandler.transition;
			transition.changeCycleDuration(Duration.seconds(0.5D));
			transition.addSingleUseFinishListener(event -> {
				transition.changeCycleDuration(Duration.seconds(0.25D));
			});
			this.menuHandler.openMenu(new GameOverMenu(this.menuHandler));
			this.pause();
		}
		this.gameView.render();

		this.framesSinceLastSecond++;
		this.prevFrac = frac;
	}

	private class FPSProperty extends ReadOnlyIntegerPropertyBase {

		private int fps;

		@Override
		public Object getBean() {
			return GameTimer.this;
		}

		@Override
		public String getName() {
			return "fps";
		}

		@Override
		public int get() {
			return this.fps;
		}

		void set(int fps) {
			this.fps = fps;
			this.fireValueChangedEvent();
		}
	}
}