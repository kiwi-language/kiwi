package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityUtils;

import java.util.*;
import java.util.function.Consumer;

public class EntityColl<T extends Entity> implements Collection<T> {
    private final LinkedList<T> list = new LinkedList<>();
    private final Map<Long, T> index = new HashMap<>();

    public boolean add(T entity) {
        list.add(entity);
        if(entity.getId() != null) {
            index.put(entity.getId(), entity);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    public void addAfter(T entity, T prev) {
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
        index.clear();
        for (T t : list) {
            if(t.getId() != null) {
                index.put(t.getId(), t);
            }
        }
    }

    public boolean remove(T entity) {
        if(entity.getId() != null) {
            return index.remove(entity.getId()) != null;
        }
        else {
            return list.removeIf(t -> EntityUtils.entityEquals(t, entity));
        }
    }

    public Collection<T> values() {
        return list;
    }

    public T get(long id) {
        return index.get(id);
    }

    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return list.toArray(a);
    }

    public T getFirst() {
        return list.getFirst();
    }

    public void addFirst(T node) {
        list.addFirst(node);
        if(node.getId() != null) {
            index.put(node.getId(), node);
        }
    }
}
