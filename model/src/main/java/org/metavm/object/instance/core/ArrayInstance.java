package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.object.instance.ArrayListener;
import org.metavm.object.instance.rest.ArrayInstanceParam;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayInstance extends DurableInstance implements Iterable<Instance> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Instance> elements = new ArrayList<>();
    private final transient List<ArrayListener> listeners = new ArrayList<>();

    public ArrayInstance(ArrayType type) {
        this(null, type, type.isEphemeral(), null);
    }

    public ArrayInstance(Id id, ArrayType type, boolean ephemeral, @Nullable Consumer<DurableInstance> load) {
        super(id, type, 0, 0, ephemeral, load);
    }

    public ArrayInstance(ArrayType type, List<? extends Instance> elements) {
        super(type);
        this.addAll(elements);
    }

    public ArrayInstance(ArrayType type, @Nullable InstanceParentRef parentRef) {
        super(type);
        setParentRef(parentRef);
    }

    public ArrayInstance(ArrayType type, @Nullable InstanceParentRef parentRef, boolean ephemeral) {
        super(null, type, 0L, 0L, ephemeral, null);
        setParentRef(parentRef);
    }

    @NoProxy
    @Override
    public void setType(Type type) {
        if(type instanceof ArrayType)
            super.setType(type);
        else
            throw new IllegalArgumentException(type + " is not an array type");
    }

    @NoProxy
    public void reset(List<Instance> elements) {
        clearInternal();
        for (Instance element : elements)
            addInternally(element);
        if (!isNew() && !isLoaded())
            setLoaded(false);
    }

    private void clearInternal() {
        for (Instance instance : new ArrayList<>(elements)) {
            removeElement(instance);
        }
    }

    @NoProxy
    public boolean isChildArray() {
        return getType().getKind() == ArrayKind.CHILD;
    }

    public Set<DurableInstance> getChildren() {
        ensureLoaded();
        if (getType().getKind() == ArrayKind.CHILD) {
            return NncUtils.filterAndMapUnique(elements, Instance::isNotNull, Instance::resolveDurable);
        } else {
            return Set.of();
        }
    }

    @Override
    protected void writeBody(InstanceOutput output) {
        ensureLoaded();
        var elements = this.elements;
        int size = 0;
        for (Instance element : elements) {
            if (!element.shouldSkipWrite())
                size++;
        }
        output.writeInt(size);
        if (isChildArray()) {
            for (Instance element : elements) {
                if (!element.shouldSkipWrite())
                    output.writeRecord(element);
            }
        } else {
            for (Instance element : elements) {
                if (!element.shouldSkipWrite()) {
                    output.writeInstance(element);
                }
            }
        }
    }

    @Override
    @NoProxy
    public void readFrom(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
        var parentField = getParentField();
        var elements = this.elements;
        int len = input.readInt();
        input.setParentField(null);
        for (int i = 0; i < len; i++) {
            var element = input.readInstance();
            elements.add(element);
        }
        input.setParentField(parentField);
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
        return Instances.createBoolean(contains(instance));
    }

    @NotNull
    public Iterator<Instance> iterator() {
        ensureLoaded();
        return elements.iterator();
    }

    public ListIterator<Instance> listIterator() {
        ensureLoaded();
        return elements.listIterator();
    }

    public boolean addElement(Instance element) {
        ensureLoaded();
        return addInternally(element);
    }

    /**
     * For instance copy only
     */
    void setElementDirectly(int index, Instance instance) {
        this.elements.set(index, instance);
    }

    void removeChild(Instance element) {
        ensureLoaded();
        removeInternally(element);
    }

    public Instance removeElement(int index) {
        ensureLoaded();
        checkIndex(index);
        var removed = elements.remove(index);
        onRemove(removed);
        return removed;
    }

    public Instance setElement(int index, Instance element) {
        ensureLoaded();
        checkIndex(index);
        element = checkElement(element);
        if (isChildArray() && element.isNotNull())
            element.resolveDurable().setParent(this, null);
        var removed = elements.set(index, element);
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
        AssertUtils.assertTrue(index >= 0 && index < elements.size(), ErrorCode.INDEX_OUT_OF_BOUND);
    }

    private boolean addInternally(Instance element) {
        element = checkElement(element);
        if (isChildArray() && element.isNotNull())
            element.resolveDurable().setParent(this, null);
        elements.add(element);
        onAdd(element);
        return true;
    }

    public void setElements(List<Instance> elements) {
        ensureLoaded();
        for (Instance element : new ArrayList<>(this.elements))
            removeElement(element);
        this.addAll(elements);
    }

    public boolean removeElement(Object element) {
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
        for (ArrayListener listener : listeners)
            listener.onRemove(instance);
    }

    private void onAdd(Instance instance) {
        setModified();
        for (ArrayListener listener : listeners)
            listener.onAdd(instance);
    }

    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        return addAll((Iterable<? extends Instance>) c);
    }

    public boolean addAll(Iterable<? extends Instance> c) {
        ensureLoaded();
        c.forEach(this::addElement);
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

    public Instance getElement(int index) {
        ensureLoaded();
        return elements.get(index);
    }

    @Override
    public Set<DurableInstance> getRefInstances() {
        ensureLoaded();
        return new IdentitySet<>(
                NncUtils.filterByType(elements, DurableInstance.class)
        );
    }

//    @Override
    @NoProxy
    public boolean isReference() {
        return true;
    }

    @Override
    public String getTitle() {
        ensureLoaded();
        List<Instance> first = elements.subList(0, Math.min(3, elements.size()));
        return NncUtils.join(first, Instance::getTitle, ",") + (elements.size() > 3 ? "..." : "");
    }

    @Override
    public void forEachChild(Consumer<DurableInstance> action) {
        if(isChildArray()) {
            elements.forEach(e -> {
                if (e instanceof InstanceReference r)
                    action.accept(r.resolve());
            });
        }
    }

    @Override
    public void forEachMember(Consumer<DurableInstance> action) {
        if(isChildArray()) {
            elements.forEach(e -> {
                if (e instanceof InstanceReference r)
                    action.accept(r.resolve());
            });
        }
        else {
            elements.forEach(e -> {
                if(e instanceof InstanceReference r && r.isValueReference())
                    action.accept(r.resolve());
            });
        }
    }

    @Override
    public void forEachReference(Consumer<InstanceReference> action) {
        elements.forEach(e -> {
            if(e instanceof InstanceReference r)
                action.accept(r);
        });
    }

    @Override
    public void forEachReference(BiConsumer<InstanceReference, Boolean> action) {
        boolean isChild = isChildArray();
        elements.forEach(e -> {
            if(e instanceof InstanceReference r)
                action.accept(r, isChild);
        });
    }

    @Override
    public void transformReference(Function<InstanceReference, InstanceReference> function) {
        var it = elements.listIterator();
        while (it.hasNext()) {
            var v = it.next();
            if(v instanceof InstanceReference r) {
                var r1 = function.apply(r);
                if(r1 != r)
                    it.set(r1);
            }
        }
    }

    //    @Override
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

//    @Override
//    @NoProxy
//    public <R> R accept(InstanceVisitor<R> visitor) {
//        return visitor.visitArrayInstance(this);
//    }

//    @Override
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
        ensureLoaded();
        elements.forEach(visitor::visit);
    }

//    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
        ensureLoaded();
        if (isChildArray())
            elements.forEach(visitor::visit);
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
        return setElement(getIndex(index), value);
    }

    @SuppressWarnings("unused")
    public BooleanInstance __remove__(Instance instance) {
        return Instances.booleanInstance(removeElement(instance));
    }

    @SuppressWarnings("unused")
    public Instance __removeAt__(Instance index) {
        return removeElement(getIndex(index));
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
        addElement(instance);
    }

    @SuppressWarnings("unused")
    public LongInstance __size__() {
        return Instances.longInstance(size());
    }

    @Override
    public ArrayType getType() {
        ensureLoaded();
        return (ArrayType) super.getType();
    }

//    @Override
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

    public boolean removeIf(Predicate<Instance> filter) {
        ensureLoaded();
        return elements.removeIf(filter);
    }

//    @Override
    protected void writeTree(TreeWriter treeWriter) {
        ensureLoaded();
        treeWriter.writeLine(getType().getName());
        treeWriter.indent();
        if(isChildArray()) {
            for (Instance element : elements) {
                if(element instanceof InstanceReference r)
                    r.resolve().writeTree(treeWriter);
                else
                    treeWriter.writeLine(element.getTitle());
            }
        }
        else {
            for (Instance element : elements) {
                if(element instanceof InstanceReference r && r.isValueReference())
                    r.resolve().writeTree(treeWriter);
                else
                    treeWriter.writeLine(element.getTitle());
            }
        }
        treeWriter.deIndent();
    }

    @Override
    public void accept(DurableInstanceVisitor visitor) {
        visitor.visitArrayInstance(this);
    }

    public List<Object> toJson(IEntityContext context) {
        var list = new ArrayList<>();
        forEach(e -> list.add(e.toJson(context)));
        return list;
    }

//    @Override
    public boolean isMutable() {
        return getType().getKind() != ArrayKind.VALUE;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public DurableInstance copy() {
        var copy = new ArrayInstance(getType());
        if(isChildArray()) {
            var copyElements = copy.elements;
            elements.forEach(e -> {
                if (e instanceof InstanceReference r)
                    e = r.resolve().copy().getReference();
                copyElements.add(e);
            });
        }
        else
            copy.elements.addAll(elements);
        return copy;
    }
}
