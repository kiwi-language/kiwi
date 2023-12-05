package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityUtils;

import java.util.*;
import java.util.function.Consumer;

public class EntityColl<T extends Entity> implements Collection<T> {
    private final List<T> dataSource;
    private boolean loaded;
    private final LinkedList<T> list = new LinkedList<>();
    private final Map<Long, T> index = new HashMap<>();

    public EntityColl() {
        dataSource = new ArrayList<>();
        loaded = true;
    }

    public EntityColl(List<T> dataSource) {
        this.dataSource = dataSource;
        loaded = false;
    }

    private void ensureLoaded() {
        if(!loaded) {
            for (T t : dataSource) {
                add0(t);
            }
            loaded = true;
        }
    }

    public boolean add(T entity) {
        ensureLoaded();
        add0(entity);
        return true;
    }

    private void add0(T entity) {
        list.add(entity);
        if(entity.getId() != null) {
            index.put(entity.getId(), entity);
        }
    }

    @Override
    public boolean remove(Object o) {
        ensureLoaded();
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        ensureLoaded();
        for (T t : c) {
            add0(t);
        }
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        ensureLoaded();
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        ensureLoaded();
        return false;
    }

    @Override
    public void clear() {
        ensureLoaded();
        list.clear();
        index.clear();
    }

    public void addAfter(T entity, T prev) {
        ensureLoaded();
        Objects.requireNonNull(entity);
        Objects.requireNonNull(prev);
        boolean added = false;
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            T t = it.next();
            if(EntityUtils.entityEquals(t, prev)) {
                it.add(entity);
                added = true;
                break;
            }
        }
        if(!added) {
            throw new RuntimeException("predecessor " + prev + " not found");
        }
        if(entity.getId() != null) {
            index.put(entity.getId(), entity);
        }
    }

    public T remove(long id) {
        ensureLoaded();
        T removed = index.remove(id);
        if(removed != null) {
            list.removeIf(t -> Objects.equals(t.getId(), id));
            return removed;
        }
        else {
            return null;
        }
    }

    public void rebuildIndex() {
        ensureLoaded();
        index.clear();
        for (T t : list) {
            if(t.getId() != null) {
                index.put(t.getId(), t);
            }
        }
    }

    public boolean remove(T entity) {
        ensureLoaded();
        if(entity.getId() != null) {
            return index.remove(entity.getId()) != null;
        }
        else {
            return list.removeIf(t -> EntityUtils.entityEquals(t, entity));
        }
    }

    public Collection<T> values() {
        ensureLoaded();
        return list;
    }

    public T get(long id) {
        ensureLoaded();
        return index.get(id);
    }

    public int size() {
        ensureLoaded();
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        ensureLoaded();
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        ensureLoaded();
        return list.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        ensureLoaded();
        return list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        ensureLoaded();
        return list.toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        ensureLoaded();
        return list.toArray(a);
    }

    public T getFirst() {
        ensureLoaded();
        return list.getFirst();
    }

    public void addFirst(T node) {
        ensureLoaded();
        list.addFirst(node);
        if(node.getId() != null) {
            index.put(node.getId(), node);
        }
    }
}
