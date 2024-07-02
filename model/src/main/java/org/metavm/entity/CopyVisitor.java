package org.metavm.entity;

import org.metavm.api.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.IdentitySet;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CopyVisitor extends ElementVisitor<Element> {

    public static final Logger logger = LoggerFactory.getLogger(CopyVisitor.class);

    public final Object root;
    private final IdentityHashMap<Object, Object> map = new IdentityHashMap<>();
    private final Set<Object> descendants = new IdentitySet<>();
    private final Map<Object, List<Consumer<Object>>> valueListeners = new HashMap<>();
    private final LinkedList<Object> elements = new LinkedList<>();
    private final boolean strictEphemeral;

    public CopyVisitor(Object root, boolean strictEphemeral) {
        this.root = root;
        this.strictEphemeral = strictEphemeral;
        EntityUtils.forEachDescendant(root, descendants::add, true);
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
            throw new InternalException("Unprocessed children: " + NncUtils.join(valueListeners.keySet(), EntityUtils::getEntityDesc));
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
            case Value ignored -> {
                if (object instanceof Element element)
                    yield element.accept(this);
                else
                    yield defaultCopy(object, null);
            }
            case List<?> list -> copyList(list);
            case Set<?> set -> copySet(set);
            case Map<?, ?> m -> copyMap(m);
            case null, default -> {
                if (descendants.contains(object))
                    yield Objects.requireNonNullElseGet(map.get(object), () -> addDummy(object, setter));
                yield substituteReference(object);
            }
        };
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
        if (copy instanceof Entity e && !(copy instanceof Value))
            e.setStrictEphemeral(strictEphemeral);
        return copy;
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
                    } else {
                        copy = new ChildArray<>(childArray.getElementType());
                        copy.setStrictEphemeral(strictEphemeral);
                    }
                    addCopy(entity, copy);
                    for (Entity child : childArray) {
                        copy.addChild((Entity) copy0(child));
                    }
                    yield copy;
                }
                case ReadWriteArray readWriteArray -> {
                    ReadWriteArray copy;
                    if (existing != null) {
                        copy = (ReadWriteArray) existing;
                        copy.clear();
                    } else {
                        copy = new ReadWriteArray<>(readWriteArray.getElementType());
                        copy.setStrictEphemeral(strictEphemeral);
                    }
                    addCopy(entity, copy);
                    for (int i = 0; i < readWriteArray.size(); i++) {
                        final int _i = i;
                        copy.add(getValue(readWriteArray.get(i), v -> copy.set(_i, v)));
                    }
                    yield copy;
                }
                case ValueArray valueArray -> {
                    var copy = new ValueArray<>(valueArray.getElementType(), List.of());
                    var table = copy.secretlyGetTable();
                    for (int i = 0; i < valueArray.size(); i++) {
                        final int _i = i;
                        table.add(getValue(valueArray.get(i), v -> table.set(_i, v)));
                    }
                    yield copy;
                }
                case ReadonlyArray<?> objects -> throw new InternalException("Readonly array copy not supported yet");
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
                    var tmpId = getCopyTmpId(entity);
                    if (copy instanceof Entity entityCopy && entityCopy.isIdNull() && tmpId != null) {
                        entityCopy.setTmpId(tmpId);
                    }
                    addCopy(entity, copy);
                    var desc = DescStore.get(entityType);
                    for (EntityProp prop : desc.getProps()) {
                        if (prop.isCopyIgnore())
                            continue;
                        var fieldValue = prop.get(entity);
                        Object fieldValueCopy;
                        if (fieldValue == null)
                            fieldValueCopy = null;
                        else if (prop.isChildEntity()) {
                            assert copy instanceof Entity;
                            fieldValueCopy = ((Entity) copy).addChild((Entity) copy0(fieldValue), prop.getName());
                        } else
                            fieldValueCopy = getValue(fieldValue, v -> prop.set(copy, v));
                        try {
                            prop.set(copy, fieldValueCopy);
                        } catch (RuntimeException e) {
                            logger.info("Fail to set field {}. entity: {}, fieldValue: {}, fieldValueCopy: {}", prop, EntityUtils.getEntityPath(entity),
                                    EntityUtils.getEntityDesc(fieldValue), EntityUtils.getEntityDesc(fieldValueCopy));
                            throw e;
                        }
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

