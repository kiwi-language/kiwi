package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.expression.VoidStructuralVisitor;
import org.metavm.object.type.Klass;
import org.metavm.util.IdentitySet;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CopyVisitor extends ElementVisitor<Element> {

    public static final Logger logger = LoggerFactory.getLogger(CopyVisitor.class);

    public final Element root;
    private final IdentityHashMap<Object, Object> map = new IdentityHashMap<>();
    private final Set<Object> descendants = new IdentitySet<>();
    private final Map<Object, List<Consumer<Object>>> valueListeners = new HashMap<>();
    private final LinkedList<Object> elements = new LinkedList<>();
    private final boolean strictEphemeral;

    public CopyVisitor(Element root, boolean strictEphemeral) {
        this.root = root;
        this.strictEphemeral = strictEphemeral;
        root.accept(new VoidStructuralVisitor() {

            @Override
            public Void visitElement(Element element) {
                descendants.add(element);
                return super.visitElement(element);
            }
        });
    }

    public void enterElement(Object element) {
        this.elements.push(element);
    }

    public void exitElement() {
        elements.pop();
    }

    protected Klass currentClass() {
        return currentElement(Klass.class);
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
            throw new InternalException("Unprocessed children: " + Utils.join(valueListeners.keySet(), EntityUtils::getEntityDesc));
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

    protected Object getValue(Object object, Consumer<Object> setter) {
        return switch (object) {
            case ValueObject ignored -> {
                if (object instanceof Element element)
                    yield element.accept(this);
                else
                    yield defaultCopy(object, null);
            }
            case List<?> list -> copyList(list);
            case Set<?> set -> copySet(set);
            case Map<?, ?> m -> copyMap(m);
            case Object[] array -> copyArray(array);
            case null, default -> {
                if (descendants.contains(object))
                    yield Objects.requireNonNullElseGet(map.get(object), () -> addDummy(object, setter));
                yield substituteReference(object);
            }
        };
    }

    private Object[] copyArray(Object[] array) {
        var componentType = array.getClass().getComponentType();
        if(componentType.isPrimitive() || componentType == String.class)
            return array;
        var copy = (Object[]) java.lang.reflect.Array.newInstance(componentType, array.length);
        for (int i = 0; i < array.length; i++) {
            int _i = i;
            copy[i] = getValue(array[i], v -> copy[_i] = v);
        }
        return copy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List copyList(List list) {
        var copy = new ArrayList();
        int i = 0;
        for (var t : list) {
            int _i = i++;
            copy.add(getValue(t, v -> copy.set(_i, v)));
        }
        return copy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set copySet(Set set) {
        var copy = new HashSet();
        for (Object o : set) {
            var e = getValue(o, copy::add);
            if (!EntityProxyFactory.isDummy(e))
                copy.add(e);
        }
        return copy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map copyMap(Map map) {
        var copy = new HashMap();
        map.forEach((k, v) -> {
            var k1 = getValue(k, k2 -> {
            });
            if (EntityProxyFactory.isDummy(k1))
                throw new IllegalStateException("Dummy is not supported for map key: " + k);
            copy.put(k1, getValue(v, v1 -> copy.put(k1, v1)));
        });
        return copy;
    }

    private Object addDummy(Object value, Consumer<Object> setter) {
        valueListeners.computeIfAbsent(value, k -> new ArrayList<>()).add(setter);
        return EntityProxyFactory.makeDummy(EntityUtils.getRealType(value.getClass()), value);
    }

    protected Object substituteReference(Object reference) {
        return reference;
    }

    public Object copy(Object object) {
        var copy = copy0(object);
//        initializeCopies();
        return copy;
    }

    protected Object copy0(Object object) {
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
        var copy = ReflectionUtils.allocateInstance(EntityUtils.getRealType(entity.getClass()));
        if (copy instanceof Entity e && !(copy instanceof ValueObject))
            e.setEphemeral();
        return copy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected final Object defaultCopy(Object entity, @Nullable Object existing) {
//        logger.debug("Copying {}", entity.getClass().getSimpleName());
        var t = map.get(entity);
        if (t != null)
            return t;
        try {
            enterElement(entity);
            return switch (entity) {
                case List<?> list -> {
                    var copy = new ArrayList<>();
                    int i = 0;
                    for (Object o : list) {
                        final int _i = i;
                        copy.add(getValue(o, v -> copy.set(_i, v)));
                    }
                    yield copy;
                }
                default -> {
                    var entityType = EntityUtils.getRealType(entity.getClass());
                    var copy = existing != null ? existing : allocateCopy(entity);
                    addCopy(entity, copy);
                    var desc = DescStore.get(entityType);
                    for (EntityProp prop : desc.getProps()) {
                        if (prop.isCopyIgnore())
                            continue;
                        var fieldValue = prop.get(entity);
                        Object fieldValueCopy;
                        if (fieldValue == null)
                            fieldValueCopy = null;
                        else
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

    protected void initializeCopies() {
        for (Object copy : map.values()) {
            if(copy instanceof LoadAware loadAware)
                loadAware.onLoad();
        }
    }

}

