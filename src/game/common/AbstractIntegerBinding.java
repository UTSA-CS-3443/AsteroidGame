package game.common;

import java.util.function.IntSupplier;

import javafx.beans.Observable;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
convenience {@link IntegerBinding} which allows
its dependencies to be specified at creation time.
they will be {@link #bind bound} during construction,
and {@link #unbind unbound} on {@link #dispose disposal}.
*/
public abstract class AbstractIntegerBinding extends IntegerBinding {

	private final Observable[] dependencies;
	private ObservableList<Observable> dependenciesView;

	public AbstractIntegerBinding(Observable... dependencies) {
		this.dependencies = dependencies;
		this.bind(dependencies);
	}

	public static AbstractIntegerBinding create(IntSupplier supplier, Observable... dependencies) {
		return new AbstractIntegerBinding(dependencies) {

			@Override
			protected int computeValue() {
				return supplier.getAsInt();
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