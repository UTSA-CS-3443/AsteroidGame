package game.view.entities;

import java.util.HashMap;
import java.util.Map;

import game.model.entities.*;
import game.model.upgrades.*;
import game.view.Images;

/**
holds mappings for all entity classes to their associated {@link EntityView}.
@see EntityView

@author Michael Johnston (tky886)
*/
public class EntityViewRegistry {

	private static final Map<Class<? extends Entity>, EntityView<?>> REGISTRY = new HashMap<>(16);
	static {
		register(       BackgroundEntity.class,  BackgroundView.INSTANCE);
		register(             ShipEntity.class,        ShipView.INSTANCE);
		register(         AsteroidEntity.class,    AsteroidView.INSTANCE);
		register(      PlasmaPulseEntity.class, PlasmaPulseView.INSTANCE);
		register(      StatUpgradeEntity.class, StatUpgradeView.INSTANCE);
		register(WideSpreadUpgradeEntity.class, SimpleImageEntityView.of(Images.WIDE_SPREAD_UPGRADE));
		register(  TimeWarpUpgradeEntity.class, SimpleImageEntityView.of(Images.TIME_WARP_UPGRADE));
		register( ExtraLifeUpgradeEntity.class, SimpleImageEntityView.of(Images.EXTRA_LIFE_UPGRADE));
		register(     GhostUpgradeEntity.class, SimpleImageEntityView.of(Images.GHOST_UPGRADE));
	}

	/** adds an associated {@link EntityView} for the entity class. */
	public static <E extends Entity> void register(Class<E> entityClass, EntityView<E> view) {
		REGISTRY.put(entityClass, view);
	}

	/** returns the {@link EntityView} associated with the entity class. */
	public static <E extends Entity> EntityView<E> get(Class<E> entityClass) {
		@SuppressWarnings("unchecked")
		EntityView<E> view = (EntityView<E>)(REGISTRY.get(entityClass));
		if (view != null) return view;
		else throw new IllegalStateException("No view registered for " + entityClass);
	}

	/** returns the {@link EntityView} associated with the entity's class. */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> EntityView<E> get(E entity) {
		return get((Class<E>)(entity.getClass()));
	}

	//does nothing, but will trigger classloading.
	public static void clinit() {}
}