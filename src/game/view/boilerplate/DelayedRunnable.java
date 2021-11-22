package game.view.boilerplate;

import javafx.application.Platform;

/**
holder for an action which is expected to be
called multiple times as the result of some trigger.
or, the action has multiple triggers which
are expected to be called at the same time.

for example, some layout logic may need to be
performed when the user changes the window size.
it is normal (and somewhat expected) for the
width and height to be changed at the same time,
but the layout logic should only happen once.

@author Michael Johnston (tky886)
*/
public class DelayedRunnable implements Runnable {

	private final Runnable action;
	private boolean isQueued;

	public DelayedRunnable(Runnable action) {
		this.action = action;
	}

	public void runLater() {
		if (!this.isQueued) {
			this.isQueued = true;
			Platform.runLater(this);
		}
	}

	public boolean isQueued() {
		return this.isQueued;
	}

	@Override
	@Deprecated //don't call directly. call runLater() instead. that's like the whole point of this class.
	public void run() {
		try {
			this.action.run();
		}
		finally {
			this.isQueued = false;
		}
	}
}