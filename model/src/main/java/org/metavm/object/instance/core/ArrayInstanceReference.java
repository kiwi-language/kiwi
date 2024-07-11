//package org.metavm.object.instance.core;
//
//import org.jetbrains.annotations.NotNull;
//import org.metavm.entity.IEntityContext;
//import org.metavm.entity.NoProxy;
//import org.metavm.entity.Tree;
//import org.metavm.object.instance.ArrayListener;
//import org.metavm.object.instance.rest.ArrayInstanceParam;
//import org.metavm.object.instance.rest.InstanceDTO;
//import org.metavm.object.instance.rest.InstanceFieldValue;
//import org.metavm.object.instance.rest.InstanceParam;
//import org.metavm.object.type.ArrayType;
//import org.metavm.object.type.Field;
//import org.metavm.object.type.Type;
//import org.metavm.object.type.Types;
//import org.metavm.object.type.rest.dto.InstanceParentRef;
//import org.metavm.util.InstanceInput;
//import org.metavm.util.InstanceOutput;
//
//import javax.annotation.Nullable;
//import java.util.*;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//
//public class ArrayInstanceReference extends ArrayInstance implements InstanceReference {
//
//    private ArrayInstance target;
//    private final Runnable loader;
//
//    public ArrayInstanceReference(Id id, Runnable loader) {
//        super(id, Types.getAnyArrayType(), false, d -> {
//        });
//        this.loader = loader;
//    }
//
//    @Override
//    @NoProxy
//    public void setType(Type type) {
//        ensureLoaded();
//        target.setType(type);
//    }
//
//    @Override
//    @NoProxy
//    public void reset(List<Instance> elements) {
//        ensureLoaded();
//        target.reset(elements);
//    }
//
//    @Override
//    @NoProxy
//    public boolean isChildArray() {
//        ensureLoaded();
//        return target.isChildArray();
//    }
//
//    @Override
//    public boolean isChild(DurableInstance instance) {
//        ensureLoaded();
//        return target.isChild(instance);
//    }
//
//    @Override
//    public Set<DurableInstance> getChildren() {
//        ensureLoaded();
//        return target.getChildren();
//    }
//
//    @Override
//    public void writeBody(InstanceOutput output) {
//        ensureLoaded();
//        target.writeBody(output);
//    }
//
//    @Override
//    @NoProxy
//    public void readFrom(InstanceInput input) {
//        ensureLoaded();
//        target.readFrom(input);
//    }
//
//    @Override
//    public Instance get(int index) {
//        ensureLoaded();
//        return target.get(index);
//    }
//
//    @Override
//    public Instance getInstance(int i) {
//        ensureLoaded();
//        return target.getInstance(i);
//    }
//
//    @Override
//    public int size() {
//        ensureLoaded();
//        return target.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        ensureLoaded();
//        return target.isEmpty();
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        ensureLoaded();
//        return target.contains(o);
//    }
//
//    @Override
//    public BooleanInstance instanceContains(Instance instance) {
//        ensureLoaded();
//        return target.instanceContains(instance);
//    }
//
//    @Override
//    public @NotNull Iterator<Instance> iterator() {
//        ensureLoaded();
//        return target.iterator();
//    }
//
//    @Override
//    public ListIterator<Instance> listIterator() {
//        ensureLoaded();
//        return target.listIterator();
//    }
//
//    @Override
//    public boolean addElement(Instance element) {
//        ensureLoaded();
//        return target.addElement(element);
//    }
//
//    @Override
//    public void setElementDirectly(int index, Instance instance) {
//        ensureLoaded();
//        target.setElementDirectly(index, instance);
//    }
//
//    @Override
//    public void removeChild(Instance element) {
//        ensureLoaded();
//        target.removeChild(element);
//    }
//
//    @Override
//    public Instance removeElement(int index) {
//        ensureLoaded();
//        return target.removeElement(index);
//    }
//
//    @Override
//    public Instance setElement(int index, Instance element) {
//        ensureLoaded();
//        return target.setElement(index, element);
//    }
//
//    @Override
//    public void setElements(List<Instance> elements) {
//        ensureLoaded();
//        target.setElements(elements);
//    }
//
//    @Override
//    public boolean removeElement(Object element) {
//        ensureLoaded();
//        return target.removeElement(element);
//    }
//
//    @Override
//    public boolean addAll(@NotNull Collection<? extends Instance> c) {
//        ensureLoaded();
//        return target.addAll(c);
//    }
//
//    @Override
//    public boolean addAll(Iterable<? extends Instance> c) {
//        ensureLoaded();
//        return target.addAll(c);
//    }
//
//    @Override
//    public int length() {
//        ensureLoaded();
//        return target.length();
//    }
//
//    @Override
//    public List<Instance> getElements() {
//        ensureLoaded();
//        return target.getElements();
//    }
//
//    @Override
//    public Instance getElement(int index) {
//        ensureLoaded();
//        return target.getElement(index);
//    }
//
//    @Override
//    public Set<DurableInstance> getRefInstances() {
//        ensureLoaded();
//        return target.getRefInstances();
//    }
//
//    @Override
//    @NoProxy
//    public boolean isReference() {
//        ensureLoaded();
//        return target.isReference();
//    }
//
//    @Override
//    public String getTitle() {
//        ensureLoaded();
//        return target.getTitle();
//    }
//
//    @Override
//    public ArrayInstanceParam getParam() {
//        ensureLoaded();
//        return target.getParam();
//    }
//
//    @Override
//    @NoProxy
//    public <R> R accept(InstanceVisitor<R> visitor) {
//        ensureLoaded();
//        return target.accept(visitor);
//    }
//
//    @Override
//    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
//        ensureLoaded();
//        target.acceptReferences(visitor);
//    }
//
//    @Override
//    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
//        ensureLoaded();
//        target.acceptChildren(visitor);
//    }
//
//    @Override
//    public ArrayInstance __init__() {
//        ensureLoaded();
//        return target.__init__();
//    }
//
//    @Override
//    public Instance __get__(Instance index) {
//        ensureLoaded();
//        return target.__get__(index);
//    }
//
//    @Override
//    public Instance __set__(Instance index, Instance value) {
//        ensureLoaded();
//        return target.__set__(index, value);
//    }
//
//    @Override
//    public BooleanInstance __remove__(Instance instance) {
//        ensureLoaded();
//        return target.__remove__(instance);
//    }
//
//    @Override
//    public Instance __removeAt__(Instance index) {
//        ensureLoaded();
//        return target.__removeAt__(index);
//    }
//
//    @Override
//    public void __clear__() {
//        ensureLoaded();
//        target.__clear__();
//    }
//
//    @Override
//    public void __add__(Instance instance) {
//        ensureLoaded();
//        target.__add__(instance);
//    }
//
//    @Override
//    public LongInstance __size__() {
//        ensureLoaded();
//        return target.__size__();
//    }
//
//    @Override
//    public ArrayType getType() {
//        ensureLoaded();
//        return target.getType();
//    }
//
//    @Override
//    public InstanceFieldValue toFieldValueDTO() {
//        ensureLoaded();
//        return target.toFieldValueDTO();
//    }
//
//    @Override
//    public void clear() {
//        ensureLoaded();
//        target.clear();
//    }
//
//    @Override
//    public ArrayInstance tryGetSource() {
//        ensureLoaded();
//        return target.tryGetSource();
//    }
//
//    @Override
//    public void addListener(ArrayListener listener) {
//        ensureLoaded();
//        target.addListener(listener);
//    }
//
//    @Override
//    public boolean removeIf(Predicate<Instance> filter) {
//        ensureLoaded();
//        return target.removeIf(filter);
//    }
//
//    @Override
//    public void writeTree(TreeWriter treeWriter) {
//        ensureLoaded();
//        target.writeTree(treeWriter);
//    }
//
//    @Override
//    public List<Object> toJson(IEntityContext context) {
//        ensureLoaded();
//        return target.toJson(context);
//    }
//
//    @Override
//    public boolean isMutable() {
//        ensureLoaded();
//        return target.isMutable();
//    }
//
//    @Override
//    public boolean isDurable() {
//        return true;
//    }
//
//    @Override
//    public boolean isEphemeral() {
//        return false;
//    }
//
//    @Override
//    public boolean shouldSkipWrite() {
//        return false;
//    }
//
//    @Override
//    @NoProxy
//    public boolean isRemoved() {
//        if(target == null)
//            return false;
//        ensureLoaded();
//        return target.isRemoved();
//    }
//
//    @Override
//    @NoProxy
//    public boolean isNew() {
//        return false;
//    }
//
//    @Override
//    @NoProxy
//    public void setRemoved() {
//        ensureLoaded();
//        target.setRemoved();
//    }
//
//    @Override
//    @NoProxy
//    public boolean isPersisted() {
//        return true;
//    }
//
//    @Override
//    public void setTarget(@NotNull DurableInstance target) {
//        this.target = (ArrayInstance) target;
//    }
//
//    @Override
//    public ArrayInstance resolve() {
//        return target;
//    }
//
//    @Override
//    @NoProxy
//    public void ensureLoaded() {
//        if(target == null)
//            load();
//    }
//
//    private void load() {
//        loader.run();
//    }
//
//    @Override
//    @NoProxy
//    public boolean isInitialized() {
//        return target != null && target.isInitialized();
//    }
//
//    @Override
//    @NoProxy
//    public boolean isLoaded() {
//        return target != null;
//    }
//
//    @Override
//    public DurableInstance getRoot() {
//        ensureLoaded();
//        return target.getRoot();
//    }
//
//    @Override
//    @NoProxy
//    public void ensureMutable() {
//        ensureLoaded();
//        target.ensureMutable();
//    }
//
//    @Override
//    public long getVersion() {
//        ensureLoaded();
//        return target.getVersion();
//    }
//
//    @Override
//    public long getSyncVersion() {
//        ensureLoaded();
//        return target.getSyncVersion();
//    }
//
//    @Override
//    @NoProxy
//    public void addOutgoingReference(ReferenceRT reference) {
//        ensureLoaded();
//        target.addOutgoingReference(reference);
//    }
//
//    @Override
//    @NoProxy
//    public void removeOutgoingReference(ReferenceRT reference) {
//        ensureLoaded();
//        target.removeOutgoingReference(reference);
//    }
//
//    @Override
//    @NoProxy
//    public Set<ReferenceRT> getOutgoingReferences() {
//        ensureLoaded();
//        return target.getOutgoingReferences();
//    }
//
//    @Override
//    public ReferenceRT getOutgoingReference(Instance target, Field field) {
//        ensureLoaded();
//        return this.target.getOutgoingReference(target, field);
//    }
//
//    @Override
//    public Tree toTree() {
//        ensureLoaded();
//        return target.toTree();
//    }
//
//    @Override
//    public void writeRecord(InstanceOutput output) {
//        ensureLoaded();
//        target.writeRecord(output);
//    }
//
//    @Override
//    public void write(InstanceOutput output) {
//        ensureLoaded();
//        target.write(output);
//    }
//
//    @Override
//    @NoProxy
//    public boolean isModified() {
//        ensureLoaded();
//        return target.isModified();
//    }
//
//    @Override
//    @NoProxy
//    public void setModified() {
//        ensureLoaded();
//        target.setModified();
//    }
//
//    @Override
//    public String getDescription() {
//        ensureLoaded();
//        return target.getDescription();
//    }
//
//    @Override
//    public void incVersion() {
//        ensureLoaded();
//        target.incVersion();
//    }
//
//    @Override
//    @NoProxy
//    public void setVersion(long version) {
//        ensureLoaded();
//        target.setVersion(version);
//    }
//
//    @Override
//    @NoProxy
//    public void setSyncVersion(long syncVersion) {
//        ensureLoaded();
//        target.setSyncVersion(syncVersion);
//    }
//
//    @Override
//    @NoProxy
//    public Object getNativeObject() {
//        ensureLoaded();
//        return target.getNativeObject();
//    }
//
//    @Override
//    @NoProxy
//    public void setNativeObject(Object nativeObject) {
//        ensureLoaded();
//        target.setNativeObject(nativeObject);
//    }
//
//    @Override
//    @NoProxy
//    public Object toSearchConditionValue() {
//        ensureLoaded();
//        return target.toSearchConditionValue();
//    }
//
//    @Override
//    public long nextNodeId() {
//        ensureLoaded();
//        return target.nextNodeId();
//    }
//
//    @Override
//    public long getNextNodeId() {
//        ensureLoaded();
//        return target.getNextNodeId();
//    }
//
//    @Override
//    public void setNextNodeId(long nextNodeId) {
//        ensureLoaded();
//        target.setNextNodeId(nextNodeId);
//    }
//
//    @Override
//    public boolean isViewSaved() {
//        ensureLoaded();
//        return target.isViewSaved();
//    }
//
//    @Override
//    public void setViewSaved() {
//        ensureLoaded();
//        target.setViewSaved();
//    }
//
//    @Override
//    public boolean isValue() {
//        ensureLoaded();
//        return target.isValue();
//    }
//
//    @Override
//    public Id getOldId() {
//        ensureLoaded();
//        return target.getOldId();
//    }
//
//    @Override
//    public boolean isSeparateChild() {
//        ensureLoaded();
//        return target.isSeparateChild();
//    }
//
//    @Override
//    public boolean isPendingChild() {
//        ensureLoaded();
//        return target.isPendingChild();
//    }
//
//    @Override
//    public DurableInstance getAggregateRoot() {
//        ensureLoaded();
//        return target.getAggregateRoot();
//    }
//
//    @Override
//    public void migrate() {
//        ensureLoaded();
//        target.migrate();
//    }
//
//    @Override
//    public void writeForwardingPointers(InstanceOutput output) {
//        ensureLoaded();
//        target.writeForwardingPointers(output);
//    }
//
//    @Override
//    public void setPendingChild(boolean pendingChild) {
//        ensureLoaded();
//        target.setPendingChild(pendingChild);
//    }
//
//    @Override
//    public Instance convert(Type type) {
//        ensureLoaded();
//        return target.convert(type);
//    }
//
//    @Override
//    public StringInstance toStringInstance() {
//        ensureLoaded();
//        return target.toStringInstance();
//    }
//
//    @Override
//    public String toStringValue() {
//        ensureLoaded();
//        return target.toStringValue();
//    }
//
//    @Override
//    public String getQualifiedTitle() {
//        ensureLoaded();
//        return target.getQualifiedTitle();
//    }
//
//    @Override
//    public InstanceDTO toDTO() {
//        ensureLoaded();
//        return target.toDTO();
//    }
//
//    @Override
//    public InstanceDTO toDTO(InstanceParam param) {
//        ensureLoaded();
//        return target.toDTO(param);
//    }
//
//    @Override
//    public String getText() {
//        ensureLoaded();
//        return target.getText();
//    }
//
//    @Override
//    public void forEach(Consumer<? super Instance> action) {
//        ensureLoaded();
//        target.forEach(action);
//    }
//
//    @Override
//    public Spliterator<Instance> spliterator() {
//        ensureLoaded();
//        return target.spliterator();
//    }
//
//
//    @Override
//    public void setParentRef(@Nullable InstanceParentRef parentRef) {
//        ensureLoaded();
//        target.setParentRef(parentRef);
//    }
//
//    @Override
//    @NoProxy
//    public void setParent(DurableInstance parent, @Nullable Field parentField) {
//        ensureLoaded();
//        target.setParent(parent, parentField);
//    }
//
//    @Override
//    @NoProxy
//    public void setParentInternal(@Nullable InstanceParentRef parentRef) {
//        ensureLoaded();
//        target.setParentInternal(parentRef);
//    }
//
//    @Override
//    public void setEphemeral() {
//        ensureLoaded();
//        target.setEphemeral();
//    }
//
//    @Override
//    @NoProxy
//    public void setParentInternal(@Nullable DurableInstance parent, @Nullable Field parentField, boolean setRoot) {
//        ensureLoaded();
//        target.setParentInternal(parent, parentField, setRoot);
//    }
//
//    @Override
//    public boolean isRoot() {
//        ensureLoaded();
//        return target.isRoot();
//    }
//
//    @Override
//    @Nullable
//    public DurableInstance getParent() {
//        ensureLoaded();
//        return target.getParent();
//    }
//
//    @Override
//    @Nullable
//    public Field getParentField() {
//        ensureLoaded();
//        return target.getParentField();
//    }
//
//    @Override
//    @Nullable
//    public InstanceParentRef getParentRef() {
//        ensureLoaded();
//        return target.getParentRef();
//    }
//
//    @Override
//    public boolean setChangeNotified() {
//        ensureLoaded();
//        return target.setChangeNotified();
//    }
//
//    @Override
//    public boolean setRemovalNotified() {
//        ensureLoaded();
//        return target.setRemovalNotified();
//    }
//
//    @Override
//    public boolean setAfterContextInitIdsNotified() {
//        ensureLoaded();
//        return target.setAfterContextInitIdsNotified();
//    }
//
//}
