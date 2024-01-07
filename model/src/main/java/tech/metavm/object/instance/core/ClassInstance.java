package tech.metavm.object.instance.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceFieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.Property;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClassInstance extends DurableInstance {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassInstance.class);

    private final ReadWriteArray<InstanceField> fields = new ReadWriteArray<>(InstanceField.class);
    private final ReadWriteArray<UnknownField> unknownFields = new ReadWriteArray<>(UnknownField.class);
    private transient Map<Flow, FlowInstance> functions;

    public static ClassInstance create(Map<Field, Instance> data, ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).data(data).build();
    }

    public static ClassInstance allocate(ClassType type) {
        return ClassInstanceBuilder.newBuilder(type).build();
    }

    public static ClassInstance allocate(ClassType type, @Nullable InstanceParentRef parentRef) {
        return ClassInstanceBuilder.newBuilder(type)
                .parentRef(parentRef)
                .build();
    }

    public ClassInstance(Id id, ClassType type, long version, long syncVersion,
                         @Nullable Consumer<DurableInstance> load, @Nullable InstanceParentRef parentRef,
                         @Nullable Map<Field, Instance> data, @Nullable SourceRef sourceRef, boolean ephemeral) {
        super(id, type, version, syncVersion, ephemeral, load);
        setParentRef(parentRef);
        setSourceRef(sourceRef);
        if (data != null)
            reset(data, 0L, 0L);
    }

    public ClassInstance(Id id, ClassType type, boolean ephemeral, @Nullable Consumer<DurableInstance> load) {
        super(id, type, 0, 0, type.isEphemeral() || ephemeral, load);
    }

    public ClassInstance(Id id, Map<Field, Instance> data, ClassType type) {
        super(id, type, 0, 0, type.isEphemeral(), null);
        reset(data, 0L, 0L);
    }

    @NoProxy
    public void reset(Map<Field, Instance> data, long version, long syncVersion) {
        setModified();
        clear();
        setVersion(version);
        setSyncVersion(syncVersion);
        getType().forEachField(field -> {
            Instance fieldValue = data.get(field);
            if (fieldValue == null || fieldValue.isNull()) {
                fieldValue = field.getDefaultValue();
            }
            initFieldInternal(field, fieldValue);
        });
        if (!isNew() && !isLoaded())
            setLoaded(false);
    }


    private void clear() {
        new ArrayList<>(getOutgoingReferences()).forEach(ReferenceRT::clear);
        this.fields.clear();
        this.unknownFields.clear();
    }

    public void forEachField(BiConsumer<Field, Instance> action) {
        ensureLoaded();
        for (InstanceField field : fields) {
            action.accept(field.getField(), field.getValue());
        }
    }


    public Set<IndexKeyRT> getIndexKeys(ParameterizedFlowProvider parameterizedFlowProvider) {
        ensureLoaded();
        return NncUtils.flatMapUnique(
                getType().getConstraints(Index.class),
                c -> c.createIndexKey(this, parameterizedFlowProvider)
        );
    }

    public Set<DurableInstance> getRefInstances() {
        ensureLoaded();
        Set<DurableInstance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            Instance fieldValue = field.getValue();
            if (fieldValue instanceof DurableInstance d) {
                result.add(d);
            }
        }
        for (UnknownField unknownField : unknownFields) {
            Instance fieldValue = unknownField.getValue();
            if (fieldValue instanceof DurableInstance d)
                result.add(d);
        }
        return result;
    }

    private void addField(InstanceField field) {
        NncUtils.requireTrue(fields.get(InstanceField::getField, field) == null,
                () -> new InternalException(
                        String.format("Field '%s' is already added to instance '%s'", field.getName(), this)
                )
        );
        this.fields.add(field);
    }

    public String getTitle() {
        ensureLoaded();
        Field titleField = getType().getTitleField();
        return titleField != null ? field(titleField).getDisplayValue() : tryGetPhysicalId() + "";
    }

    public Object getField(List<Long> fieldPath) {
        ensureLoaded();
        long fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if (fieldPath.size() > 1) {
            List<Long> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) field.getValue(), inst -> inst.getField(subFieldPath));
        } else {
            return field.getValue();
        }
    }

    @NoProxy
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    @NoProxy
    public boolean isReference() {
        return !isValue();
    }

    public Instance getInstanceField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public void setUnknownField(ClassType declaringType, Column column, Instance value) {
        ensureLoaded();
        var field = NncUtils.find(unknownFields,
                f -> f.getDeclaringType() == declaringType && f.getColumn() == column);
        if (field != null) {
            field.setValue(value);
        } else {
            unknownFields.add(new UnknownField(declaringType, column, value));
        }
    }

    @Override
    public boolean isChild(DurableInstance instance) {
        ensureLoaded();
        for (InstanceField field : fields) {
            if (field.getField().isChild()) {
                Instance fieldValue = field.getValue();
                if (fieldValue == instance || (fieldValue instanceof DurableInstance d && d.isChild(instance))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<DurableInstance> getChildren() {
        ensureLoaded();
        var children = new IdentitySet<DurableInstance>();
        for (InstanceField field : fields) {
            if (field.getField().isChild()) {
                Instance fieldValue = field.getValue();
                if (fieldValue.isNotNull()) {
                    children.add((DurableInstance) fieldValue);
                }
            }
        }
        return children;
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        ensureLoaded();
        var nonNullFields = NncUtils.filterAndSort(fields,
                f -> f.getValue().isNotNull(),
                Comparator.comparingLong(InstanceField::getId)
        );
        output.writeInt(nonNullFields.size());
        for (InstanceField field : nonNullFields) {
            output.writeLong(field.getId());
            if (includeChildren && field.getField().isChild() && !field.getField().isLazy())
                output.writeValue(field.getValue());
            else
                output.writeInstance(field.getValue());
        }
    }

    @Override
    @NoProxy
    public void readFrom(InstanceInput input) {
        setLoaded(input.isLoadedFromCache());
        List<Field> fields = getType().getSortedFields();
        var instFields = this.fields;
        int numFields = input.readInt();
        int j = 0;
        for (int i = 0; i < numFields; i++) {
            var fieldId = input.readLong();
            while (j < fields.size() && fields.get(j).tryGetId() < fieldId) {
                instFields.add(new InstanceField(this, fields.get(j), Instances.nullInstance(), false));
                j++;
            }
            Field field;
            if (j < fields.size() && (field = fields.get(j)).tryGetId() == fieldId) {
                input.setParent(this, field);
                var value = input.readInstance();
                instFields.add(new InstanceField(this, field, value, false));
                j++;
            }
        }
        input.setParent(getParent(), getParentField());
        for (; j < fields.size(); j++)
            instFields.add(new InstanceField(this, fields.get(j), Instances.nullInstance(), false));
    }

    public ClassInstance getClassInstance(Field field) {
        ensureLoaded();
        return (ClassInstance) field(field).getValue();
    }

    public Instance getField(String fieldPath) {
        ensureLoaded();
        int idx = fieldPath.indexOf('.');
        if (idx == -1) {
            return getField(getType().getFieldNyNameRequired(fieldPath));
        } else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            ClassInstance fieldInstance = (ClassInstance) getInstanceField(fieldName);
            return NncUtils.get(fieldInstance, inst -> inst.getField(subPath));
        }
    }

    public Instance getInstanceField(String fieldName) {
        ensureLoaded();
        return field(getType().tryGetFieldByName(fieldName)).getValue();
    }

    public void setField(String fieldCode, Instance value) {
        var field = getType().getFieldByCode(fieldCode);
        setFieldInternal(field, value);
    }

    public void setField(Field field, Instance value) {
        ensureLoaded();
        setFieldInternal(field, value);
    }

    void setOrInitField(Field field, Instance value) {
        ensureLoaded();
        if(isFieldInitialized(field))
            setFieldInternal(field, value);
        else
            initFieldInternal(field, value);
    }

    @Override
    public ClassInstance tryGetSource() {
        return (ClassInstance) super.tryGetSource();
    }

    private void setFieldInternal(Field field, Instance value) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        if (field.isReadonly())
            throw new BusinessException(ErrorCode.CAN_NOT_MODIFY_READONLY_FIELD);
        if (field.isChild() && value.isNotNull())
            ((DurableInstance) value).setParent(this, field);
        setModified();
        field(field).setValue(value);
    }

    public boolean isFieldInitialized(Field field) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        return fields.get(InstanceField::getField, field) != null;
    }

    public void initField(Field field, Instance value) {
        ensureLoaded();
        initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Instance value) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        NncUtils.requireFalse(isFieldInitialized(field));
        if (field.isChild() && value.isNotNull())
            ((DurableInstance) value).setParent(this, field);
        addField(new InstanceField(this, field, value));
    }

    public StringInstance getStringField(Field field) {
        return (StringInstance) getField(field(field).getField());
    }

    public LongInstance getLongField(Field field) {
        return (LongInstance) getField(field);
    }

    public DoubleInstance getDoubleField(Field field) {
        return (DoubleInstance) getField(field);
    }

    public Instance getField(Field field) {
        ensureLoaded();
        return field(field).getValue();
    }

    public FlowInstance getFunction(Method method, ParameterizedFlowProvider parameterizedFlowProvider) {
        ensureLoaded();
        if (functions == null) {
            functions = new HashMap<>();
        }
        var concreteFlow = getType().tryResolveMethod(method, parameterizedFlowProvider);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowInstance(getType().tryResolveMethod(method, parameterizedFlowProvider), this));
    }

    public Instance getProperty(Property property, ParameterizedFlowProvider parameterizedFlowProvider) {
        return switch (property) {
            case Field field -> getField(field);
            case Method method -> getFunction(method, parameterizedFlowProvider);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    protected InstanceField field(Field field) {
        var instanceField = fields.get(InstanceField::getField, field);
        if (instanceField != null)
            return instanceField;
        var unknownField = unknownFields.get(UnknownField::getColumn, field.getColumn());
        if (unknownField != null) {
            unknownFields.remove(unknownField);
            instanceField = new InstanceField(this, field, unknownField.getValue());
            addField(instanceField);
            return instanceField;
        }
        throw new InternalException("Can not find instance field for '" + field + "'");
    }

    protected InstanceField field(long fieldId) {
        return field(getType().getField(fieldId));
    }

    @Override
    protected ClassInstanceParam getParam() {
        ensureLoaded();
        return new ClassInstanceParam(NncUtils.map(fields, InstanceField::toDTO));
    }

    @Override
    @NoProxy
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitClassInstance(this);
    }

    @Override
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
        ensureLoaded();
        for (var field : fields)
            field.getValue().accept(visitor);
        for (var unknownField : unknownFields)
            unknownField.getValue().accept(visitor);
    }

    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
        ensureLoaded();
        for (var field : fields) {
            if (field.getField().isChild())
                field.getValue().accept(visitor);
        }
    }

    @Override
    public FieldValue toFieldValueDTO() {
        ensureLoaded();
        if (isValue()) {
            return new InstanceFieldValue(
                    getTitle(),
                    toDTO()
            );
        } else {
            return new ReferenceFieldValue(
                    getTitle(),
                    Objects.requireNonNull(getInstanceIdString(), "Id required")
            );
        }
    }

    public List<InstanceField> fields() {
        ensureLoaded();
        return fields.toList();
    }

    public ArrayInstance getInstanceArray(Field field) {
        ensureLoaded();
        return field(field).getInstanceArray();
    }

    public ArrayInstance getInstanceArray(String fieldName) {
        ensureLoaded();
        return field(getType().tryGetFieldByName(fieldName)).getInstanceArray();
    }

    private void ensureFieldInitialized(Field field) {
        if (fields.get(InstanceField::getField, field) != null) {
            return;
        }
        var unknownField = unknownFields.remove(UnknownField::getColumn, field.getColumn());
        if (unknownField != null) {
            initFieldInternal(field, unknownField.getValue());
            return;
        }
        throw new BusinessException(
                ErrorCode.FIELD_NOT_INITIALIZED,
                getType().getName(),
                field.getName()
        );
    }

    public void ensureAllFieldsInitialized() {
        ensureLoaded();
        for (Field field : getType().getAllFields()) {
            ensureFieldInitialized(field);
        }
    }

}
