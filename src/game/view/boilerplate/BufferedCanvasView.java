package game.view.boilerplate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;

/**
holder for a {@link BufferedCanvas} which also controls drawing to that {@link #canvas}.
drawing will be performed whenever the {@link #width} or {@link #height} changes,
or whenever {@link #render} is called.

@author Michael Johnston (tky886)
*/
public abstract class BufferedCanvasView {

	public final DoubleProperty width, height;
	public final BufferedCanvas canvas;
	private final DelayedRunnable reRenderTask;

	public BufferedCanvasView() {
		this(0.0D, 0.0D);
	}

	public BufferedCanvasView(double width, double height) {
		this.width  = new SimpleDoubleProperty(this, "width",  width );
		this.height = new SimpleDoubleProperty(this, "height", height);
		this.canvas = new BufferedCanvas(width, height);
		this.canvas.width .bind(this.width );
		this.canvas.height.bind(this.height);
		this.reRenderTask = new DelayedRunnable(() -> {
			this.doRender();
			this.canvas.flush();
		});

		ChangeListener<Number> onResize = (observable, oldValue, newValue) -> this.render();
		this.width .addListener(onResize);
		this.height.addListener(onResize);
	}

	public void render() {
		this.reRenderTask.runLater();
	}

	protected abstract void doRender();
}