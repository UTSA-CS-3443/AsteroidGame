package game.model;

import game.model.entities.AsteroidEntity;
import game.model.entities.Entity;

/**
used as a way to categorize entities.
this can be used in combination with {@link EntityList}
to iterate over only entities which are included in a specific filter.
this will improve performance, as entities that are not included
in the filter are automatically excluded from iteration.
explicit type checks are not necessary for the caller that wants to iterate.

when I designed this system,
I thought I would need a lot of different filters,
so I made it reasonably extensible.
later it turned out that I didn't actually
need to filter on anything more than asteroids,
which is why this enum only has one constant now.
maybe in the future I'll have a need to filter on more types,
but for now at least I think I'll just leave this system as-is.
if it ain't broke, don't fix it.

@author Michael Johnston (tky886)
*/
public enum EntityFilter {
	ASTEROID(AsteroidEntity.class);

	public static final EntityFilter[] FILTERS = values();

	public final Class<? extends Entity> entityClass;

	EntityFilter(Class<? extends Entity> entityClass) {
		this.entityClass = entityClass;
	}

	public boolean matches(Entity entity) {
		return this.entityClass.isInstance(entity);
	}
}