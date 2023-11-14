package tech.metavm.entity;

import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CopyVisitor extends ElementVisitor<Element> {

    private final static Set<String> PROP_NAME_BLACKLIST = Set.of(
            "id", "version", "syncVersion", "persisted"
    );

    public final Object root;
    private final Map<Object, Object> map = new IdentityHashMap<>();
    private final Set<Object> descendants = new IdentitySet<>();
    private final Map<Object, List<Consumer<Object>>> valueListeners = new HashMap<>();

    public CopyVisitor(Object root) {
        this.root = root;
        if (root instanceof Entity entity)
            descendants.addAll(entity.getDescendants());
        else
            descendants.add(root);
    }

    protected void addCopy(Object original, Object copy) {
        map.put(original, copy);
        var listeners = valueListeners.remove(original);
        if(listeners != null) {
            for (Consumer<Object> listener : listeners) {
                listener.accept(copy);
            }
        }
    }

    public void check() {
        if(!valueListeners.isEmpty()) {
            throw new InternalException("Unprocessed children");
        }
    }

    Object getRoot() {
        return root;
    }

    @Override
    public Element visitElement(Element element) {
        return (Element) defaultCopy(element, null);
    }

    private Object getValue(Object value, Consumer<Object> setter) {
        if (descendants.contains(value)) {
            var existing = map.get(value);
            if(existing != null)
                return existing;
            else
                return addDummy(value, setter);
        } else
            return substituteReference(value);
    }

    private Object addDummy(Object value, Consumer<Object> setter) {
        valueListeners.computeIfAbsent(value, k -> new ArrayList<>()).add(setter);
        return EntityProxyFactory.makeDummy(value.getClass());
    }

    protected Object substituteReference(Object reference) {
        return reference;
    }

    public final Object copy(Object object) {
        if (object == null)
            return null;
        if (object instanceof Element element) {
            var copied = map.get(element);
            if (copied != null)
                return copied;
            return element.accept(this);
        } else
            return defaultCopy(object, null);
    }

    protected Object defaultCopy(Object entity, @Nullable Object existing) {
        var copied = map.get(entity);
        if (copied != null)
            return copied;
        return switch (entity) {
            case ChildArray<?> childArray -> {
                var copy = new ChildArray<>(childArray.getElementType());
                addCopy(entity, copy);
                for (Entity child : childArray)
                    copy.addChild((Entity) copy(child));
                yield copy;
            }
            //noinspection rawtypes
            case ReadWriteArray readWriteArray -> {
                var copy = new ReadWriteArray<>(readWriteArray.getElementType());
                addCopy(entity, copy);
                for (int i = 0; i < readWriteArray.size(); i++) {
                    final int _i = i;
                    //noinspection unchecked
                    copy.add(getValue(readWriteArray.get(i), v -> readWriteArray.set(_i, v)));
                }
                yield copy;
            }
            case ReadonlyArray<?> objects -> throw new InternalException("Readonly array copy not supported yet");
            default -> {
                var entityType = EntityUtils.getRealType(entity.getClass());
                var copy = existing != null ? existing : ReflectUtils.allocateInstance(entityType);
                addCopy(entity, copy);
                var desc = DescStore.get(entityType);
                for (EntityProp prop : desc.getNonTransientProps()) {
                    if (PROP_NAME_BLACKLIST.contains(prop.getName()))
                        continue;
                    Object fieldValue;
                    if (prop.isChildEntity())
                        fieldValue = copy(prop.get(entity));
                    else
                        fieldValue = getValue(prop.get(entity), v -> prop.set(copy, v));
                    prop.set(copy, fieldValue);
                }
                yield copy;
            }
        };
    }

}
