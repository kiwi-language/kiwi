package tech.metavm.entity;

import tech.metavm.object.type.ClassType;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CopyVisitor extends ElementVisitor<Element> {

    public final Object root;
    private final IdentityHashMap<Object, Object> map = new IdentityHashMap<>();
    private final Set<Object> descendants = new IdentitySet<>();
    private final Map<Object, List<Consumer<Object>>> valueListeners = new HashMap<>();
    private final LinkedList<Object> elements = new LinkedList<>();

    public CopyVisitor(Object root) {
        this.root = root;
        EntityUtils.forEachDescendant(root, descendants::add);
    }

    public void enterElement(Object element) {
        this.elements.push(element);
    }

    public void exitElement() {
        elements.pop();
    }

    protected ClassType currentClass() {
        return currentElement(ClassType.class);
    }

    protected <T> T currentElement(Class<T> klass) {
        for (Object element : elements) {
            if (klass.isInstance(element))
                return klass.cast(element);
        }
        throw new InternalException("No enclosing element of type '" + klass.getName() + "' is found");
    }

    protected @Nullable Object getExistingCopy(Object object) {
        return null;
    }

    protected @Nullable Long getCopyTmpId(Object object) {
        return null;
    }

    protected void addCopy(Object original, Object copy) {
        map.put(original, copy);
        var listeners = valueListeners.remove(original);
        if (listeners != null) {
            for (Consumer<Object> listener : listeners) {
                listener.accept(copy);
            }
        }
    }

    public void check() {
        if (!valueListeners.isEmpty()) {
            throw new InternalException("Unprocessed children");
        }
    }

    protected Object getRoot() {
        return root;
    }

    @Override
    public Element visitElement(Element element) {
        return (Element) defaultCopy(element, getExistingCopy(element));
    }

    protected Object getCopy(Object original) {
        return map.get(original);
    }

    protected Object getValue(Object value, Consumer<Object> setter) {
        if (descendants.contains(value)) {
            var existing = map.get(value);
            if (existing != null)
                return existing;
            else
                return addDummy(value, setter);
        } else
            return substituteReference(value);
    }

    private Object addDummy(Object value, Consumer<Object> setter) {
        valueListeners.computeIfAbsent(value, k -> new ArrayList<>()).add(setter);
        return EntityProxyFactory.makeDummy(EntityUtils.getRealType(value.getClass()), value);
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
            return defaultCopy(object, getExistingCopy(object));
    }

    protected Object allocateCopy(Object entity) {
        return ReflectionUtils.allocateInstance(EntityUtils.getRealType(entity.getClass()));
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    protected final Object defaultCopy(Object entity, @Nullable Object existing) {
        var t = map.get(entity);
        if (t != null)
            return t;
        try {
            enterElement(entity);
            return switch (entity) {
                case ChildArray<?> childArray -> {
                    ChildArray copy;
                    if (existing != null) {
                        copy = (ChildArray) existing;
                        copy.clear();
                    } else
                        copy = new ChildArray<>(childArray.getElementType());
                    addCopy(entity, copy);
                    for (Entity child : childArray) {
                        copy.addChild((Entity) copy(child));
                    }
                    yield copy;
                }
                case ReadWriteArray readWriteArray -> {
                    ReadWriteArray copy;
                    if (existing != null) {
                        copy = (ReadWriteArray) existing;
                        copy.clear();
                    } else
                        copy = new ReadWriteArray<>(readWriteArray.getElementType());
                    addCopy(entity, copy);
                    for (int i = 0; i < readWriteArray.size(); i++) {
                        final int _i = i;
                        copy.add(getValue(readWriteArray.get(i), v -> copy.set(_i, v)));
                    }
                    yield copy;
                }
                case ReadonlyArray<?> objects -> throw new InternalException("Readonly array copy not supported yet");
                default -> {
                    var entityType = EntityUtils.getRealType(entity.getClass());
                    var copy = existing != null ? existing : allocateCopy(entity);
                    var tmpId = getCopyTmpId(entity);
                    if (copy instanceof Entity entityCopy && entityCopy.isIdNull() && tmpId != null) {
                        entityCopy.setTmpId(tmpId);
                    }
                    addCopy(entity, copy);
                    var desc = DescStore.get(entityType);
                    for (EntityProp prop : desc.getNonTransientProps()) {
                        if (prop.getField().isAnnotationPresent(CopyIgnore.class))
                            continue;
                        var fieldValue = prop.get(entity);
                        Object fieldValueCopy;
                        if (fieldValue == null)
                            fieldValueCopy = null;
                        else if (prop.isChildEntity()) {
                            assert copy instanceof Entity;
                            fieldValueCopy = ((Entity) copy).addChild((Entity) copy(fieldValue), prop.getName());
                        } else
                            fieldValueCopy = getValue(fieldValue, v -> prop.set(copy, v));
                        prop.set(copy, fieldValueCopy);
                    }
                    yield copy;
                }
            };
        } finally {
            exitElement();
        }
    }

}

