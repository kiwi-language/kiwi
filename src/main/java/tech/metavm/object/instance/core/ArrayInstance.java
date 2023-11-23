package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.ArrayListener;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.ArrayInstanceParam;
import tech.metavm.object.instance.rest.InstanceFieldValue;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import java.util.*;
import java.util.function.Consumer;

public class ArrayInstance extends Instance implements Iterable<Instance> /*implements Collection<Instance>*/ {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Instance> elements = new ArrayList<>();
    private final transient List<ArrayListener> listeners = new ArrayList<>();

    public ArrayInstance(ArrayType type) {
        this(null, type, null);
    }

    public ArrayInstance(Long id, ArrayType type, @Nullable Consumer<Instance> load) {
        super(id, type, null, 0, 0, load);
    }

    public ArrayInstance(ArrayType type, List<Instance> elements) {
        super(type);
        this.addAll(elements);
    }

    public ArrayInstance(ArrayType type, @Nullable InstanceParentRef parentRef) {
        super(type, parentRef);
    }

    @NoProxy
    public void reset(List<Instance> elements) {
        clearInternal();
        for (Instance element : elements)
            addInternally(element);
        if(!isNew() && !isLoaded())
            setLoaded(false);
    }

    private void clearInternal() {
        for (Instance instance : new ArrayList<>(elements)) {
            remove(instance);
        }
    }

    @NoProxy
    public boolean isChildArray() {
        return getType().getKind() == ArrayKind.CHILD;
    }

    @Override
    public boolean isChild(Instance instance) {
        ensureLoaded();
        return isChildArray() && elements.contains(instance);
    }

    public Set<Instance> getChildren() {
        ensureLoaded();
        if (getType().getKind() == ArrayKind.CHILD) {
            return NncUtils.filterUnique(elements, Instance::isNotNull);
        } else {
            return Set.of();
        }
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        ensureLoaded();
        output.writeInt(size());
        boolean isChildArray = isChildArray();
        for (Instance element : elements) {
            if(isChildArray && includeChildren)
                output.writeInstance(element);
            else
                output.writeReference(element);
        }
    }

    @Override
    @NoProxy
    public void readFrom(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
        var parent = getParent();
        var parentField = getParentField();
        var elements = this.elements;
        int len = input.readInt();
        input.setParent(this, null);
        for (int i = 0; i < len; i++) {
            var element = input.readInstance();
            elements.add(element);
            if(element.isNotPrimitive())
                new ReferenceRT(this, element, null);
        }
        input.setParent(parent, parentField);
    }

    public Instance get(int index) {
        ensureLoaded();
        checkIndex(index);
        return elements.get(index);
    }

    public Instance getInstance(int i) {
        ensureLoaded();
        return elements.get(i);
    }

    public int size() {
        ensureLoaded();
        return elements.size();
    }

    public boolean isEmpty() {
        ensureLoaded();
        return elements.isEmpty();
    }

    public boolean contains(Object o) {
        ensureLoaded();
        //noinspection SuspiciousMethodCalls
        return elements.contains(o);
    }

    public BooleanInstance instanceContains(Instance instance) {
        ensureLoaded();
        return InstanceUtils.createBoolean(contains(instance));
    }

    @NotNull
    public Iterator<Instance> iterator() {
        ensureLoaded();
        return elements.iterator();
    }

    public boolean add(Instance element) {
        ensureLoaded();
        NncUtils.requireFalse(isChildArray(),
                () -> new BusinessException(ErrorCode.CAN_NOT_ASSIGN__CHILD_FIELD));
        return addInternally(element);
    }

    void addChild(Instance element) {
        ensureLoaded();
        NncUtils.requireTrue(isChildArray());
        addInternally(element);
    }

    void removeChild(Instance element) {
        ensureLoaded();
        removeInternally(element);
    }

    public Instance remove(int index) {
        ensureLoaded();
        checkIndex(index);
        var removed = elements.remove(index);
        onRemove(removed);
        return removed;
    }

    public Instance set(int index, Instance element) {
        ensureLoaded();
        checkIndex(index);
        checkElement(element);
        Instance removed = elements.set(index, element);
        if (removed != null)
            onRemove(removed);
        onAdd(element);
        return removed;
    }

    private Instance checkElement(Instance element) {
        var elementType = getType().getElementType();
        if (elementType.isAssignableFrom(element.getType()))
            return element;
        else
            return element.convert(elementType);
    }

    private void checkIndex(int index) {
        NncUtils.assertTrue(index >= 0 && index < elements.size(), ErrorCode.INDEX_OUT_OF_BOUND);
    }

    private boolean addInternally(Instance element) {
        element = checkElement(element);
        elements.add(element);
        onAdd(element);
        return true;
    }

    public void setElements(List<Instance> elements) {
        ensureLoaded();
        for (Instance element : new ArrayList<>(this.elements))
            remove(element);
        this.addAll(elements);
    }

    public boolean remove(Object element) {
        ensureLoaded();
        return removeInternally(element);
    }

    private boolean removeInternally(Object element) {
        //noinspection SuspiciousMethodCalls
        if (elements.remove(element)) {
            Instance removed = (Instance) element;
            onRemove(removed);
            return true;
        }
        return false;
    }

    private void onRemove(Instance instance) {
        setModified();
        if (instance.isNotPrimitive())
            getOutgoingReference(instance, null).clear();
        for (ArrayListener listener : listeners)
            listener.onRemove(instance);
    }

    private void onAdd(Instance instance) {
        setModified();
        if(instance.isNotPrimitive())
            new ReferenceRT(this, instance, null);
        for (ArrayListener listener : listeners)
            listener.onAdd(instance);
    }

    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        return addAll((Iterable<? extends Instance>) c);
    }

    public boolean addAll(Iterable<? extends Instance> c) {
        ensureLoaded();
        c.forEach(this::add);
        return true;
    }

    public int length() {
        ensureLoaded();
        return elements.size();
    }

    public List<Instance> getElements() {
        ensureLoaded();
        return elements;
    }

    @Override
    public Set<Instance> getRefInstances() {
        ensureLoaded();
        return new IdentitySet<>(
                NncUtils.filter(elements, Instance::isReference)
        );
    }

    @Override
    @NoProxy
    public boolean isReference() {
        return true;
    }

    public InstancePO toPO(long tenantId) {
        return toPO(tenantId, new IdentitySet<>());
    }

    @Override
    public InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
        ensureLoaded();
        return new InstancePO(
                tenantId,
                getIdRequired(),
                getTitle(),
                getType().getIdRequired(),
                InstanceOutput.toByteArray(this),
                NncUtils.getOrElse(getParent(), Instance::getId, -1L),
                NncUtils.getOrElse(getParentField(), Field::getId, -1L),
                getRoot().getIdRequired(),
                getVersion(),
                getSyncVersion()
        );
    }

    @Override
    public String getTitle() {
        ensureLoaded();
        List<Instance> first = elements.subList(0, Math.min(3, elements.size()));
        return NncUtils.join(first, Instance::getTitle, ",") + (elements.size() > 3 ? "..." : "");
    }

    @Override
    protected ArrayInstanceParam getParam() {
        ensureLoaded();
        if (isChildArray()) {
            return new ArrayInstanceParam(
                    true,
                    NncUtils.map(elements, e ->
                            new InstanceFieldValue(
                                    e.getTitle(), e.toDTO()
                            )
                    )
            );
        } else {
            return new ArrayInstanceParam(
                    false,
                    NncUtils.map(elements, Instance::toFieldValueDTO)
            );
        }
    }

    @Override
    @NoProxy
    public void accept(InstanceVisitor visitor) {
        visitor.visitArrayInstance(this);
    }

    @Override
    public void acceptReferences(InstanceVisitor visitor) {
        ensureLoaded();
        for (Instance element : elements)
            element.accept(visitor);
    }

    @Override
    public void acceptChildren(InstanceVisitor visitor) {
        ensureLoaded();
        if (isChildArray()) {
            acceptReferences(visitor);
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
    public InstanceFieldValue toFieldValueDTO() {
        ensureLoaded();
        return new InstanceFieldValue(getTitle(), toDTO());
    }

    public void clear() {
        ensureLoaded();
        clearInternal();
    }

    public void addListener(ArrayListener listener) {
        listeners.add(listener);
    }

}
