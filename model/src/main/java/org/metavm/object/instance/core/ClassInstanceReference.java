//package org.metavm.object.instance.core;
//
//import org.jetbrains.annotations.NotNull;
//import org.metavm.entity.IEntityContext;
//import org.metavm.entity.NoProxy;
//import org.metavm.entity.Tree;
//import org.metavm.flow.Method;
//import org.metavm.object.instance.IndexKeyRT;
//import org.metavm.object.instance.rest.FieldValue;
//import org.metavm.object.instance.rest.InstanceDTO;
//import org.metavm.object.instance.rest.InstanceParam;
//import org.metavm.object.type.*;
//import org.metavm.object.type.rest.dto.InstanceParentRef;
//import org.metavm.util.InstanceInput;
//import org.metavm.util.InstanceOutput;
//
//import javax.annotation.Nullable;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.BiConsumer;
//
//public class ClassInstanceReference extends ClassInstance implements InstanceReference {
//
//    private ClassInstance target;
//    private final Runnable loader;
//    private boolean loading;
//
//    public ClassInstanceReference(Id id, Runnable loader) {
//        super(id, ClassInstance.uninitializedKlass.getType(), false, i -> {
//        });
//        this.loader = loader;
//    }
//
//    public void ensureLoaded() {
//        if (target == null)
//            load();
//    }
//
//    @Override
//    public ClassInstance resolve() {
//        return target;
//    }
//
//    @Override
//    public void setTarget(@NotNull DurableInstance target) {
//        this.target = (ClassInstance) target;
//    }
//
//    public void load() {
//        assert !loading : "Instance" + getId() + " is already loading";
//        loading = true;
//        loader.run();
//        loading = false;
//    }
//
//    public boolean isLoaded() {
//        return target != null;
//    }
//
//    @Override
//    @NoProxy
//    public void reset(Map<Field, Instance> data, long version, long syncVersion) {
//        ensureLoaded();
//        target.reset(data, version, syncVersion);
//    }
//
//    @Override
//    public void logFieldTable() {
//        ensureLoaded();
//        ensureLoaded();
//        target.logFieldTable();
//    }
//
//    @Override
//    public void forEachField(BiConsumer<Field, Instance> action) {
//        ensureLoaded();
//        target.forEachField(action);
//    }
//
//    @Override
//    public Set<IndexKeyRT> getIndexKeys() {
//        ensureLoaded();
//        return target.getIndexKeys();
//    }
//
//    @Override
//    public Set<DurableInstance> getRefInstances() {
//        ensureLoaded();
//        return target.getRefInstances();
//    }
//
//    @Override
//    public String getTitle() {
//        ensureLoaded();
//        return target.getTitle();
//    }
//
//    @Override
//    public Object getField(List<Id> fieldPath) {
//        ensureLoaded();
//        return target.getField(fieldPath);
//    }
//
//    @Override
//    public ClassType getType() {
//        ensureLoaded();
//        return target.getType();
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
//    public Instance getInstanceField(Field field) {
//        ensureLoaded();
//        return target.getInstanceField(field);
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
//    public void setType(Type type) {
//        ensureLoaded();
//        target.setType(type);
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
//    public ClassInstance getClassInstance(Field field) {
//        ensureLoaded();
//        return target.getClassInstance(field);
//    }
//
//    @Override
//    public Instance getField(String fieldPath) {
//        ensureLoaded();
//        return target.getField(fieldPath);
//    }
//
//    @Override
//    public Instance getInstanceField(String fieldName) {
//        ensureLoaded();
//        return target.getInstanceField(fieldName);
//    }
//
//    @Override
//    public void setField(String fieldCode, Instance value) {
//        ensureLoaded();
//        target.setField(fieldCode, value);
//    }
//
//    @Override
//    public void setField(Field field, Instance value) {
//        ensureLoaded();
//        target.setField(field, value);
//    }
//
//    @Override
//    public ClassInstance tryGetSource() {
//        ensureLoaded();
//        return target.tryGetSource();
//    }
//
//    @Override
//    public boolean isFieldInitialized(Field field) {
//        ensureLoaded();
//        return target.isFieldInitialized(field);
//    }
//
//    @Override
//    @Nullable
//    public Field findUninitializedField(Klass type) {
//        ensureLoaded();
//        return target.findUninitializedField(type);
//    }
//
//    @Override
//    public void initField(Field field, Instance value) {
//        ensureLoaded();
//        target.initField(field, value);
//    }
//
//    @Override
//    public StringInstance getStringField(Field field) {
//        ensureLoaded();
//        return target.getStringField(field);
//    }
//
//    @Override
//    public LongInstance getLongField(Field field) {
//        ensureLoaded();
//        return target.getLongField(field);
//    }
//
//    @Override
//    public DoubleInstance getDoubleField(Field field) {
//        ensureLoaded();
//        return target.getDoubleField(field);
//    }
//
//    @Override
//    public Instance getField(Field field) {
//        ensureLoaded();
//        return target.getField(field);
//    }
//
//    @Override
//    public Instance getUnknownField(int tag) {
//        ensureLoaded();
//        return target.getUnknownField(tag);
//    }
//
//    @Override
//    public FlowInstance getFunction(Method method) {
//        ensureLoaded();
//        return target.getFunction(method);
//    }
//
//    @Override
//    public Instance getProperty(Property property) {
//        ensureLoaded();
//        return target.getProperty(property);
//    }
//
//    @Override
//    public InstanceField field(Id fieldId) {
//        ensureLoaded();
//        return target.field(fieldId);
//    }
//
//    @Override
//    public InstanceParam getParam() {
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
//    public void writeTree(TreeWriter treeWriter) {
//        ensureLoaded();
//        target.writeTree(treeWriter);
//    }
//
//    @Override
//    public FieldValue toFieldValueDTO() {
//        ensureLoaded();
//        return target.toFieldValueDTO();
//    }
//
//    @Override
//    public boolean isList() {
//        ensureLoaded();
//        return target.isList();
//    }
//
//    @Override
//    public boolean isChildList() {
//        ensureLoaded();
//        return target.isChildList();
//    }
//
//    @Override
//    public ArrayInstance getInstanceArray(Field field) {
//        ensureLoaded();
//        return target.getInstanceArray(field);
//    }
//
//    @Override
//    public Object toJson(IEntityContext context) {
//        ensureLoaded();
//        return target.toJson(context);
//    }
//
//    @Override
//    public void ensureAllFieldsInitialized() {
//        ensureLoaded();
//        target.ensureAllFieldsInitialized();
//    }
//
//    @Override
//    public Klass getKlass() {
//        ensureLoaded();
//        return target.getKlass();
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
//        ensureLoaded();
//        return target.isDurable();
//    }
//
//    @Override
//    public boolean isEphemeral() {
//        ensureLoaded();
//        return target.isEphemeral();
//    }
//
//    @Override
//    public boolean shouldSkipWrite() {
//        ensureLoaded();
//        return target.shouldSkipWrite();
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
//        ensureLoaded();
//        return target.isPersisted();
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
//    public Object toSearchConditionValue() {
//        ensureLoaded();
//        return target.toSearchConditionValue();
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
//    @Nullable
//    public String getStringIdForDTO() {
//        ensureLoaded();
//        return target.getStringIdForDTO();
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
//    @NoProxy
//    public void setModified() {
//        ensureLoaded();
//        target.setModified();
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
//    @Override
//    public DurableInstance getAggregateRoot() {
//        ensureLoaded();
//        return target.getAggregateRoot();
//    }
//
//}
