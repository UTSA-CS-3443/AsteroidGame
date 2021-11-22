package game.view.menu;

import game.controller.MenuHandler;
import javafx.scene.Node;

/**
Menu's are kind of a combination of a view and a controller.
they have a GUI component to display (see {@link #getRootComponent}),
but they will also typically set up some event handlers in their constructors.
there are also {@link #startClose} and {@link #finishClose}
methods which also act a bit like controller logic.

I don't see this as violating the MVC design pattern
because FXML loading does basically the same thing.

@author Michael Johnston (tky886)
*/
public abstract class Menu {

	public final MenuHandler menuHandler;

	public Menu(MenuHandler menuHandler) {
		this.menuHandler = menuHandler;
	}

	public abstract Node getRootComponent();

	public void startClose() {}

	public void finishClose() {}
}