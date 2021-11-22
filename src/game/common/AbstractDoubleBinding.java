package game.common;

import java.util.function.DoubleSupplier;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
convenience {@link DoubleBinding} which allows
its dependencies to be specified at creation time.
they will be {@link #bind bound} during construction,
and {@link #unbind unbound} on {@link #dispose disposal}.
*/
public abstract class AbstractDoubleBinding extends DoubleBinding {

	private final Observable[] dependencies;
	private ObservableList<Observable> dependenciesView;

	public AbstractDoubleBinding(Observable... dependencies) {
		this.dependencies = dependencies;
		this.bind(dependencies);
	}

	public static AbstractDoubleBinding create(DoubleSupplier supplier, Observable... dependencies) {
		return new AbstractDoubleBinding(dependencies) {

			@Override
			protected double computeValue() {
				return supplier.getAsDouble();
			}
		};
	}

	@Override
	public ObservableList<?> getDependencies() {
		if (this.dependenciesView == null) {
			this.dependenciesView = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.dependencies));
		}
		return this.dependenciesView;
	}

	@Override
	public void dispose() {
		this.unbind(this.dependencies);
	}
}