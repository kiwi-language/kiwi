package tech.metavm.entity;

import com.intellij.util.TriConsumer;
import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class ChildArray<T extends Entity> extends ReadonlyArray<T> {

    public ChildArray(Class<T> klass) {
        super(klass);
    }

    public ChildArray(TypeReference<T> typeRef) {
        super(typeRef);
    }

    public ChildArray(Type type) {
        super(type);
    }

    @Override
    protected Class<?> getRawClass() {
        return ChildArray.class;
    }

    public boolean remove(Object value) {
        //noinspection SuspiciousMethodCalls
        return table.remove(value);
    }

    public void addChildren(Iterable<? extends T> children) {
        children.forEach(this::addChild);
    }

    public void resetChildren(Iterable<? extends T> children) {
        table.clear();
        addChildren(children);
    }

    @Override
    protected void getDescendants(List<Object> descendants, IdentitySet<Object> visited) {
        if(visited.contains(this))
            throw new InternalException("Circular reference detected in entity structure");
        descendants.add(this);
        visited.add(this);
        for (T child : table)
            child.getDescendants(descendants, visited);
    }

    public void clear() {
        table.clear();
    }

    public void removeIf(Predicate<T> filter) {
        table.removeIf(filter);
    }

    public void addChildAfter(T child, T anchor) {
        super.addChild(child, null);
        table.addAfter(child, anchor);
    }

    public void addChild(T child) {
        super.addChild(child, null);
        table.add(child);
    }

    public void addChild(int index, T child) {
        super.addChild(child, null);
        table.add(index, child);
    }

    public void addFirstChild(T child) {
        super.addChild(child, null);
        table.addFirst(child);
    }

    public <P extends BaseDTO> void update(List<P> params,
                                           BiFunction<P, EntityParentRef, T> create,
                                           BiConsumer<T, P> update)  {
        Map<RefDTO, T> map = new HashMap<>();
        for (T t : table) {
            map.put(t.getRef(), t);
        }
        table.clear();
        var parentRef = EntityParentRef.fromArray(this);
        for (P param : params) {
            var existing = map.get(param.getRef());
            if(existing != null) {
                table.add(existing);
                update.accept(existing, param);
            }
            else {
                addChild(create.apply(param, parentRef));
            }
        }
    }

}
