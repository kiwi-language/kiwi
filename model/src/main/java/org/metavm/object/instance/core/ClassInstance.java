package org.metavm.object.instance.core;

import org.metavm.flow.ClosureContext;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public interface ClassInstance extends Instance {

    Klass uninitializedKlass = KlassBuilder.newBuilder("Uninitialized", "Uninitialized").build();

    static MvClassInstance create(Map<Field, Value> data, ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).data(data).build();
    }

    static MvClassInstance allocate(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).build();
    }

    static MvClassInstance allocateUninitialized(Id id) {
        return ClassInstanceBuilder.newBuilder(uninitializedKlass.getType()).id(id).initFieldTable(false).build();
    }

    static MvClassInstance allocateEmpty(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).initFieldTable(false).build();
    }

    static MvClassInstance allocate(ClassType type, @Nullable InstanceParentRef parentRef) {
        return ClassInstanceBuilder.newBuilder(type)
                .parentRef(parentRef)
                .build();
    }

    void logFields();

    void forEachField(BiConsumer<Field, Value> action);

    default Set<IndexKeyRT> getIndexKeys() {
        var keys = new HashSet<IndexKeyRT>();
        getInstanceType().foreachIndex(indexRef -> keys.addAll(indexRef.createIndexKey(this)));
        return keys;
    }

    @Override
    ClassType getInstanceType();

    String getTitle();

    default Object getField(List<Id> fieldPath) {
        var fieldId = fieldPath.getFirst();
        var fieldValue = getField(getInstanceKlass().getField(fieldId));
        if (fieldPath.size() > 1) {
            var subFieldPath = fieldPath.subList(1, fieldPath.size());
            return Utils.safeCall((ClassInstance) ((Reference) fieldValue).get(), inst -> inst.getField(subFieldPath));
        } else {
            return fieldValue;
        }
    }

    Set<Instance> getChildren();

    void defaultWrite(InstanceOutput output);

    void defaultRead(InstanceInput input);

    default ClassInstance getClassInstance(Field field) {
        return getField(field).resolveObject();
    }

    default Value getField(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if (idx == -1) {
            return getField(getInstanceKlass().getFieldNyName(fieldPath));
        } else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            MvClassInstance fieldInstance = (MvClassInstance) ((Reference) getField(fieldName)).get();
            return Utils.safeCall(fieldInstance, inst -> inst.getField(subPath));
        }
    }

    default Value getInstanceField(String fieldName) {
        return getField(getInstanceKlass().findFieldByName(fieldName));
    }

    default void setField(String fieldCode, Value value) {
        var field = getInstanceKlass().getFieldByName(fieldCode);
        setField(field, value);
    }

    void setField(Field field, Value value);

    void setFieldForce(Field field, Value value);

    boolean isFieldInitialized(Field field);

    @Nullable Field findUninitializedField(Klass type);

    void initField(Field field, Value value);

    default StringValue getStringField(Field field) {
        return (StringValue) getField(field);
    }

    default LongValue getLongField(Field field) {
        return (LongValue) getField(field);
    }

    default DoubleValue getDoubleField(Field field) {
        return (DoubleValue) getField(field);
    }

    Value getField(Field field);

    void tryClearUnknownField(long klassTag, int tag);

    Value getUnknownField(long klassTag, int tag);

    @Nullable Value tryGetUnknown(long klassId, int tag);

    default FlowValue getFunction(MethodRef method) {
        return new FlowValue(Objects.requireNonNull(getInstanceType().findOverride(method)), this);
    }

    default Value getProperty(PropertyRef property) {
        return switch (property) {
            case FieldRef field -> getField(field.getRawField());
            case MethodRef method -> getFunction(method);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    default boolean isList() {
        return getInstanceKlass().isList();
    }

    default boolean isEnum() {
        return getInstanceKlass().isEnum();
    }

    default boolean isChildList() {
        return getInstanceKlass().isChildList();
    }

    default ArrayInstance getInstanceArray(Field field) {
        return getField(field).resolveArray();
    }

    void ensureAllFieldsInitialized();

    Klass getInstanceKlass();

    default boolean isMutable() {
        return getInstanceKlass().getKind() != ClassKind.VALUE;
    }

    void setUnknown(long classTag, int fieldTag, Value value);

    default boolean isSearchable() {
        return getInstanceKlass().isSearchable();
    }

    @Nullable
    ClosureContext getClosureContext();

    void addChild(ClassInstance child);

    Map<String, Value> buildSource();
}