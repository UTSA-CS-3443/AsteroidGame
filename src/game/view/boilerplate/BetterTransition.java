package game.view.boilerplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleConsumer;

import game.common.Util;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
provides a number of extra utility methods beyond what Transition normally provides.

@author Michael Johnston (tky886)
*/
public abstract class BetterTransition extends Transition {

	public static final Interpolator SMOOTHSTEP_INTERPOLATOR = new Interpolator() {

		@Override
		protected double curve(double t) {
			return Util.smooth(t);
		}

		@Override
		public String toString() {
			return "GameTimer.SMOOTHSTEP_INTERPOLATOR";
		}
	};

	private List<EventHandler<ActionEvent>> onFinished;
	private Set<EventHandler<ActionEvent>> onFinishedSingle;

	public BetterTransition() {
		this.init();
	}

	public BetterTransition(double targetFramerate) {
		super(targetFramerate);
		this.init();
	}

	public static BetterTransition create(DoubleConsumer fracUpdater) {
		return new BetterTransition() {

			@Override
			protected void interpolate(double frac) {
				fracUpdater.accept(frac);
			}
		};
	}

	public static BetterTransition create(double targetFramerate, DoubleConsumer fracUpdater) {
		return new BetterTransition(targetFramerate) {

			@Override
			protected void interpolate(double frac) {
				fracUpdater.accept(frac);
			}
		};
	}

	private void init() {
		this.setInterpolator(SMOOTHSTEP_INTERPOLATOR);
		EventHandler<ActionEvent> handler = event -> {
			//semantics:
			//1: give all handlers a chance to handle the event, even if some of them throw an exception.
			//2: remove all the single-use handlers, even if some of them previously threw an exception.
			//3: if any handlers threw an exception, re-throw that exception.
			//3a: if more than one handler threw an exception, the first one will be re-thrown,
			//and all the others will be added as suppressed exceptions.
			Throwable error = null;
			if (this.onFinished != null) {
				for (EventHandler<ActionEvent> h : this.onFinished) {
					try {
						h.handle(event);
					}
					catch (Throwable throwable) {
						if (error != null) error.addSuppressed(throwable);
						else error = throwable;
					}
				}
			}
			if (this.onFinishedSingle != null && !this.onFinishedSingle.isEmpty()) {
				if (this.onFinished != null) this.onFinished.removeIf(this.onFinishedSingle::contains);
				this.onFinishedSingle.clear();
			}
			if (error != null) throw Util.throwExceptionUnsafely(error);
		};
		this.setOnFinished(handler);
		//can't override setFinishListener because it's final,
		//so this is the next best option.
		this.onFinishedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != handler) {
				this.setOnFinished(handler); //restore old value.
				throw new UnsupportedOperationException("Don't call setOnFinished(). Call addFinishListener() instead.");
			}
		});
	}

	/**
	delegates to {@link #setCycleDuration}.
	I can't make {@link #setCycleDuration} public because it's final,
	so a delegator was the next best option.
	*/
	public void changeCycleDuration(Duration duration) {
		this.setCycleDuration(duration);
	}

	/**
	adds a listener which will be invoked exactly once when this transition finishes,
	even if this transition is restarted after it finishes.
	after the listener is invoked, it will be removed from our listener list.
	*/
	public void addSingleUseFinishListener(EventHandler<ActionEvent> onFinished) {
		this.addFinishListener(onFinished);
		if (this.onFinishedSingle == null) {
			this.onFinishedSingle = new HashSet<>(1);
		}
		this.onFinishedSingle.add(onFinished);
	}

	/**
	adds a listener which will be invoked every time this transition finishes.
	it can finish more than once if it is restarted after finishing.
	*/
	public void addFinishListener(EventHandler<ActionEvent> onFinished) {
		if (this.onFinished == null) {
			this.onFinished = new ArrayList<>(1);
		}
		this.onFinished.add(onFinished);
	}

	public void removeFinishListener(EventHandler<ActionEvent> onFinished) {
		if (this.onFinished != null) this.onFinished.remove(onFinished);
	}

	public void playForwards() {
		this.setRate(Math.abs(this.getRate()));
		this.play();
	}

	public void playBackwards() {
		this.setRate(-Math.abs(this.getRate()));
		this.play();
	}

	public void playForwardsFromStart() {
		this.setRate(Math.abs(this.getRate()));
		this.jumpTo(Duration.ZERO);
		this.play();
	}

	public void playBackwardsFromEnd() {
		this.setRate(-Math.abs(this.getRate()));
		this.jumpTo(this.getTotalDuration());
		this.play();
	}
}