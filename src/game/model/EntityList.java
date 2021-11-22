package game.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import game.model.entities.Entity;

/**
"list" is a bit of a misnomer; this class behaves more like a {@link Set} of entities.
it is however ordered, in the sense that {@link #iterator} will provide
entities in the same order in which they were added to the list.

the real power though is that it also maintains sets of
entities which match a specific {@link EntityFilter filter}.
this allows for fast iteration over all entities which match a filter,
while skipping everything which doesn't match that filter.

removing entities via the {@link #iterator} will
update the filtered entity sets accordingly.

@author Michael Johnston (tky886)
*/
public class EntityList implements Iterable<Entity> {

	private final LinkedHashSet<Entity> orderedEntities = new LinkedHashSet<>();
	private final Map<EntityFilter, Set<Entity>> filteredEntities = new EnumMap<>(EntityFilter.class);
	private int cap = 100;

	public EntityList() {
		for (EntityFilter filter : EntityFilter.FILTERS) {
			this.filteredEntities.put(filter, new HashSet<>(8));
		}
	}

	public void addEntity(Entity entity) {
		this.orderedEntities.add(entity);
		for (EntityFilter filter : EntityFilter.FILTERS) {
			if (filter.matches(entity)) {
				this.filteredEntities.get(filter).add(entity);
			}
		}
		//used for debugging when an entity doesn't implement {@link Entity#tickInteraction} properly,
		//and does not get removed from this list when it's supposed to.
		if (this.orderedEntities.size() >= this.cap) {
			System.err.println("More than " + this.cap + " entities!");
			//welcome to generics hell.
			//this local variable cannot be inlined.
			Map<Class<?>, Integer> counts = (
				this
				.orderedEntities
				.stream()
				.collect(Collectors.toMap(Object::getClass, e -> 1, Integer::sum))
			);
			counts
			.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(Map.Entry<Class<?>, Integer>::getValue).reversed())
			.forEach((Map.Entry<Class<?>, Integer> entry) -> System.err.println(entry.getValue() + "x " + entry.getKey().getName()));

			this.cap += 100;
		}
	}

	public void clear() {
		this.orderedEntities.clear();
		this.filteredEntities.values().forEach(Set::clear);
		this.cap = 100;
	}

	/**
	returns all the entities in this list.
	the returned set is unmodifiable.
	if you want to add or remove entities,
	call {@link #addEntity} or {@link #removeEntity} instead.
	*/
	public Set<Entity> getEntities() {
		return Collections.unmodifiableSet(this.orderedEntities);
	}

	/**
	returns all the entities in this list which match the provided filter.
	the returned set is unmodifiable.
	if you want to add or remove entities,
	call {@link #addEntity} or {@link #removeEntity} instead.
	*/
	@SuppressWarnings("unchecked")
	public <E extends Entity> Set<E> getEntities(EntityFilter filter) {
		return Collections.unmodifiableSet((Set<E>)(this.filteredEntities.get(filter)));
	}

	void removeFilteredEntity(Entity entity) {
		for (EntityFilter filter : EntityFilter.FILTERS) {
			if (filter.matches(entity)) {
				this.filteredEntities.get(filter).remove(entity);
			}
		}
	}

	public void removeEntity(Entity entity) {
		this.orderedEntities.remove(entity);
		this.removeFilteredEntity(entity);
	}

	public boolean containsEntity(Entity entity) {
		return this.orderedEntities.contains(entity);
	}

	@Override
	public Iterator<Entity> iterator() {
		Iterator<Entity> delegate = this.orderedEntities.iterator();
		return new Iterator<Entity>() {

			private Entity lastEntity;

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public Entity next() {
				return this.lastEntity = delegate.next();
			}

			@Override
			public void forEachRemaining(Consumer<? super Entity> action) {
				this.lastEntity = null;
				delegate.forEachRemaining(action);
			}

			@Override
			public void remove() {
				Entity entity = this.lastEntity;
				if (entity == null) throw new IllegalStateException();
				this.lastEntity = null;
				delegate.remove();
				EntityList.this.removeFilteredEntity(entity);
			}
		};
	}

	@Override
	public Spliterator<Entity> spliterator() {
		return this.orderedEntities.spliterator();
	}

	@Override
	public void forEach(Consumer<? super Entity> action) {
		this.orderedEntities.forEach(action);
	}
}