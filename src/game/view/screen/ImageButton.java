package game.view.screen;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
basically an {@link ImageView} which behaves
like a {@link javafx.scene.control.Button}
or a {@link javafx.scene.control.RadioButton}.
separate {@link Image}'s can be provided to configure
how the button should look normally compared to when the
user is hovering over the button or clicking on the button.

@author Michael Johnston (tky886)
*/
public abstract class ImageButton {

	public final ImageView imageView;
	public final Image normalImage, hoveredImage, pressedImage;

	public ImageButton(Image normalImage, Image hoveredImage, Image pressedImage) {
		this. normalImage =  normalImage;
		this.hoveredImage = hoveredImage;
		this.pressedImage = pressedImage;
		this.imageView = new ImageView(normalImage);
		this.imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, this::startHovering);
		this.imageView.addEventHandler(MouseEvent.MOUSE_EXITED,  this::stopHovering);
		this.imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, this::press);
		this.imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, this::click);
	}

	public ImageView getRootComponent() {
		return this.imageView;
	}

	protected abstract void startHovering(MouseEvent event);

	protected abstract void stopHovering(MouseEvent event);

	protected abstract void press(MouseEvent event);

	protected abstract void click(MouseEvent event);

	/**
	behaves like {@link javafx.scene.control.Button}.

	if the user is actively clicking on the button,
	then {@link #pressedImage} will be displayed.
	else if the user is hovering over the button,
	then {@link #hoveredImage} will be displayed.
	else {@link #normalImage} will be displayed.

	the {@link #onClick} handler will only be invoked if the
	user {@link MouseEvent#MOUSE_CLICKED clicks} the button.
	*/
	public static class SingleImageButton extends ImageButton {

		private final EventHandler<MouseEvent> onClick;

		public SingleImageButton(Image normalImage, Image hoveredImage, Image pressedImage, EventHandler<MouseEvent> onClick) {
			super(normalImage, hoveredImage, pressedImage);
			this.onClick = onClick;
		}

		@Override
		protected void startHovering(MouseEvent event) {
			this.imageView.setImage(this.hoveredImage);
		}

		@Override
		protected void stopHovering(MouseEvent event) {
			this.imageView.setImage(this.normalImage);
		}

		@Override
		protected void press(MouseEvent event) {
			this.imageView.setImage(this.pressedImage);
		}

		@Override
		protected void click(MouseEvent event) {
			this.onClick.handle(event);
		}
	}

	/**
	behaves like {@link javafx.scene.control.RadioButton}.
	this implementation is paired with a {@link #buttonValue value} which
	defines what value should be selected when the user clicks this button,
	and an {@link #selectedValue ObjectProperty} which stores the value that is currently selected.

	if the button's {@link #buttonValue value} equals the {@link #selectedValue},
	then the {@link #pressedImage} will be displayed.
	else if the user is hovering over this button,
	then the {@link #hoveredImage} will be displayed.
	else the {@link #normalImage} will be displayed.

	if the user {@link MouseEvent#MOUSE_CLICKED clicks} this button,
	the {@link #selectedValue} will be changed to the {@link #buttonValue},
	and the displayed image will be updated accordingly.
	callers can {@link ObjectProperty#addListener(ChangeListener) add a listener}
	to the {@link #selectedValue} to be notified when the user changes it.
	*/
	public static class ChoiceImageButton<T> extends ImageButton {

		public final ObjectProperty<T> selectedValue;
		public final T buttonValue;

		public ChoiceImageButton(Image normalImage, Image hoveredImage, Image pressedImage, ObjectProperty<T> selectedValue, T buttonValue) {
			super(normalImage, hoveredImage, pressedImage);
			this.selectedValue = selectedValue;
			this.buttonValue = buttonValue;
			if (selectedValue.getValue() == buttonValue) {
				this.imageView.setImage(pressedImage);
			}
			selectedValue.addListener((observable, oldValue, newValue) -> {
				this.imageView.setImage(newValue == this.buttonValue ? this.pressedImage : this.normalImage);
			});
		}

		@Override
		protected void startHovering(MouseEvent event) {
			if (this.buttonValue != this.selectedValue.getValue()) {
				this.imageView.setImage(this.hoveredImage);
			}
		}

		@Override
		protected void stopHovering(MouseEvent event) {
			if (this.buttonValue != this.selectedValue.getValue()) {
				this.imageView.setImage(this.normalImage);
			}
		}

		@Override
		protected void press(MouseEvent event) {
			if (this.buttonValue != this.selectedValue.getValue()) {
				this.imageView.setImage(this.pressedImage);
			}
		}

		@Override
		protected void click(MouseEvent event) {
			this.selectedValue.setValue(this.buttonValue);
		}
	}
}