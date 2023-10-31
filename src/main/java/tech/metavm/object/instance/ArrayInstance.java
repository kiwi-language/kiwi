package tech.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.rest.ArrayParamDTO;
import tech.metavm.object.instance.rest.InstanceFieldValueDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.*;

import java.util.*;

public class ArrayInstance extends Instance implements Collection<Instance> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Instance> elements = new ArrayList<>();
    private final transient List<ArrayListener> listeners = new ArrayList<>();

    public ArrayInstance(ArrayType type) {
        this(type, List.of());
    }

    public ArrayInstance(ArrayType type,
                         List<Instance> elements
    ) {
        super(type);
        for (Instance element : elements) {
            addInternally(element);
        }
    }

    public ArrayInstance(Long id,
                         ClassType type,
                         long version,
                         long syncVersion,
                         List<Instance> elements
    ) {
        super(id, type, version, syncVersion);
        for (Instance element : elements) {
            addInternally(element);
        }
    }

    @NoProxy
    public void initialize(List<Instance> elements) {
        for (Instance element : elements) {
            addInternally(element);
        }
    }

    public boolean isChildArray() {
        return getType().kind() == ArrayKind.CHILD;
    }

    @Override
    public boolean isChild(Instance instance) {
        return isChildArray() && elements.contains(instance);
    }

    public Set<Instance> getChildren() {
        if (getType().kind() == ArrayKind.CHILD) {
            return NncUtils.filterUnique(elements, Instance::isNotNull);
        } else {
            return Set.of();
        }
    }

    public Instance get(int index) {
        checkIndex(index);
        return elements.get(index);
    }

    public Instance getInstance(int i) {
        return elements.get(i);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    public BooleanInstance instanceContains(Instance instance) {
        return InstanceUtils.createBoolean(contains(instance));
    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        return elements.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return elements.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(Instance element) {
        return addInternally(element);
    }

    public Instance remove(int index) {
        var removed = elements.remove(index);
        onRemove(removed);
        return removed;
    }

    public Instance set(int index, Instance element) {
        checkIndex(index);
        checkElement(element);
        Instance removed = elements.set(index, element);
        if (removed != null) {
            onRemove(removed);
        }
        onAdd(element);
        return removed;
    }

    private void checkElement(Instance element) {
        if (!getType().getElementType().isAssignableFrom(element.getType())) {
            throw new BusinessException(ErrorCode.INCORRECT_ELEMENT_TYPE);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new BusinessException(ErrorCode.INDEX_OUT_OF_BOUND);
        }
    }

    private boolean addInternally(Instance element) {
        new ReferenceRT(this, element, null);
        elements.add(element);
        onAdd(element);
        return true;
    }

    public void setElements(List<Instance> elements) {
        for (Instance element : new ArrayList<>(this.elements)) {
            remove(element);
        }
        this.addAll(elements);
    }

    @Override
    public boolean remove(Object element) {
        return removeInternally(element);
    }

    private boolean removeInternally(Object element) {
        //noinspection SuspiciousMethodCalls
        if (elements.remove(element)) {
            Instance removed = (Instance) element;
            onRemove(removed);
            getOutgoingReference(removed, null).clear();
            return true;
        }
        return false;
    }

    private void onRemove(Instance instance) {
        for (ArrayListener listener : listeners) {
            listener.onRemove(instance);
        }
    }

    public void onAdd(Instance instance) {
        for (ArrayListener listener : listeners) {
            listener.onAdd(instance);
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        return addAll((Iterable<? extends Instance>) c);
    }

    public boolean addAll(Iterable<? extends Instance> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean anyChange = false;
        for (Object o : c) {
            if (remove(o)) {
                anyChange = true;
            }
        }
        return anyChange;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        List<Object> toRemove = new ArrayList<>();
        for (Object o : c) {
            if (!contains(o)) {
                toRemove.add(o);
            }
        }
        if (toRemove.isEmpty()) {
            return false;
        }
        for (Object o : toRemove) {
            remove(o);
        }
        return true;
    }

    public int length() {
        return elements.size();
    }

    public List<Instance> getElements() {
        return elements;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return new IdentitySet<>(
                NncUtils.filter(elements, Instance::isReference)
        );
    }

    @Override
    @NoProxy
    public boolean isReference() {
        return true;
    }

    @Override
    public InstanceArrayPO toPO(long tenantId) {
        return toPO(tenantId, new IdentitySet<>());
    }

    @Override
    InstanceArrayPO toPO(long tenantId, IdentitySet<Instance> visited) {
        return new InstanceArrayPO(
                getId(),
                getType().getIdRequired(),
                tenantId,
                elements.size(),
                NncUtils.map(elements, e -> elementToPO(tenantId, e, visited)),
                getVersion(),
                getSyncVersion()
        );
    }

    @Override
    @NoProxy
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return toIdentityPO();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    protected ArrayParamDTO getParam() {
        if (isChildArray()) {
            return new ArrayParamDTO(
                    true,
                    NncUtils.map(elements, e ->
                            new InstanceFieldValueDTO(
                                    e.getTitle(), e.toDTO()
                            )
                    )
            );
        } else {
            return new ArrayParamDTO(
                    false,
                    NncUtils.map(elements, Instance::toFieldValueDTO)
            );
        }
    }

    private static Object elementToPO(long tenantId, Instance element, IdentitySet<Instance> visited) {
        if (element.isNull()) {
            return null;
        }
        if (element instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue();
        } else {
            if (element.isValue()) {
                return element.toPO(tenantId, visited);
            } else {
                return element.toIdentityPO();
            }
        }
    }

    @SuppressWarnings("unused")
    public ArrayInstance __init__() {
        return this;
    }

    @SuppressWarnings("unused")
    public Instance __get__(Instance index) {
        return get(getIndex(index));
    }

    @SuppressWarnings("unused")
    public Instance __set__(Instance index, Instance value) {
        return set(getIndex(index), value);
    }

    @SuppressWarnings("unused")
    public BooleanInstance __remove__(Instance instance) {
        return InstanceUtils.booleanInstance(remove(instance));
    }

    @SuppressWarnings("unused")
    public Instance __removeAt__(Instance index) {
        return remove(getIndex(index));
    }

    private int getIndex(Instance instance) {
        if (instance instanceof LongInstance longInstance) {
            return longInstance.getValue().intValue();
        } else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    @SuppressWarnings("unused")
    public void __clear__() {
        clear();
    }

    @SuppressWarnings("unused")
    public void __add__(Instance instance) {
        add(instance);
    }

    @SuppressWarnings("unused")
    public LongInstance __size__() {
        return InstanceUtils.longInstance(size());
    }

    @Override
    @NoProxy
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }

    @Override
    public InstanceFieldValueDTO toFieldValueDTO() {
        return new InstanceFieldValueDTO(getTitle(), toDTO());
    }

    public void clear() {
        for (Instance instance : new ArrayList<>(elements)) {
            remove(instance);
        }
    }

    public void addListener(ArrayListener listener) {
        listeners.add(listener);
    }

    @Override
    public String toString() {
        return "InstanceArray{" +
                "elements=" + elements +
                '}';
    }

}
