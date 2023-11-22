package tech.metavm.object.instance.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.InstancePO;
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

public class ClassInstance extends Instance {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassInstance.class);

    private final ReadWriteArray<InstanceField> fields = new ReadWriteArray<>(InstanceField.class);
    private final ReadWriteArray<UnknownField> unknownFields = new ReadWriteArray<>(UnknownField.class);
    private transient Map<Flow, FlowInstance> functions;

    public static ClassInstance allocate(ClassType type) {
        return new ClassInstance(type);
    }

    public ClassInstance(ClassType type) {
        this(null, type, (Consumer<Instance>) null);
    }

    public ClassInstance(Long id, ClassType type, @Nullable Consumer<Instance> load) {
        super(id, type, null, 0, 0, load);
    }

    public ClassInstance(ClassType type, @Nullable InstanceParentRef parentRef) {
        super(null, type, parentRef, 0, 0, null);
    }

    public ClassInstance(Map<Field, Instance> data, ClassType type) {
        this(null, data, type);
    }

    public ClassInstance(Long id, Map<Field, Instance> data, ClassType type) {
        super(id, type, null, 0, 0, null);
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

    public List<IndexEntryPO> getIndexEntries(IEntityContext entityContext) {
        ensureLoaded();
        return NncUtils.flatMap(
                getType().getConstraints(Index.class),
                c -> c.createIndexEntry(this, entityContext)
        );
    }

    public List<IndexKeyRT> getIndexKeys(IEntityContext entityContext) {
        ensureLoaded();
        return NncUtils.flatMap(
                getType().getConstraints(Index.class),
                c -> c.createIndexKey(this, entityContext)
        );
    }

    public Set<Instance> getRefInstances() {
        ensureLoaded();
        Set<Instance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            Instance fieldValue = field.getValue();
            if (fieldValue.isReference()) {
                result.add(fieldValue);
            }
        }
        for (UnknownField unknownField : unknownFields) {
            Instance fieldValue = unknownField.getValue();
            if (fieldValue.isReference())
                result.add(fieldValue);
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
        Field titleField = getType().getTileField();
        return titleField != null ? field(titleField).getDisplayValue() : getId() + "";
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
    public boolean isChild(Instance instance) {
        ensureLoaded();
        for (InstanceField field : fields) {
            if (field.getField().isChild()) {
                Instance fieldValue = field.getValue();
                if (fieldValue == instance || fieldValue.isChild(instance)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Instance> getChildren() {
        ensureLoaded();
        Set<Instance> children = new IdentitySet<>();
        for (InstanceField field : fields) {
            if (field.getField().isChild()) {
                Instance fieldValue = field.getValue();
                if (fieldValue.isNotNull()) {
                    children.add(fieldValue);
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
                output.writeInstance(field.getValue());
            else
                output.writeReference(field.getValue());
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
            while (j < fields.size() && fields.get(j).getIdRequired() < fieldId) {
                instFields.add(new InstanceField(this, fields.get(j), InstanceUtils.nullInstance(), false));
                j++;
            }
            Field field;
            if (j < fields.size() && (field = fields.get(j)).getIdRequired() == fieldId) {
                input.setParent(this, field);
                var value = input.readInstance();
                instFields.add(new InstanceField(this, field, value, false));
                j++;
            }
        }
        input.setParent(getParent(), getParentField());
        for (; j < fields.size(); j++)
            instFields.add(new InstanceField(this, fields.get(j), InstanceUtils.nullInstance(), false));
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

    public void setField(Field field, Instance value) {
        ensureLoaded();
        if (field.isChild()) {
            throw new BusinessException(ErrorCode.CAN_NOT_ASSIGN__CHILD_FIELD);
        }
        setFieldInternal(field, value);
    }

    void setChild(Field field, Instance value) {
        ensureLoaded();
        NncUtils.requireTrue(field.isChild());
        if (isFieldInitialized(field)) {
            setFieldInternal(field, value);
        } else {
            initFieldInternal(field, value);
        }
    }

    private void setFieldInternal(Field field, Instance value) {
        ensureLoaded();
        setModified();
        field(field).setValue(value);
    }

    public boolean isFieldInitialized(Field field) {
        ensureLoaded();
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        return isFieldInitialized0(field);
    }

    private boolean isFieldInitialized0(Field field) {
        ensureLoaded();
        return fields.get(InstanceField::getField, field) != null;
    }

    public void initField(Field field, Instance value) {
        ensureLoaded();
        NncUtils.requireFalse(field.isChild());
        initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Instance value) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        NncUtils.requireFalse(isFieldInitialized0(field));
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

    public FlowInstance getFunction(Flow flow) {
        ensureLoaded();
        if (functions == null) {
            functions = new HashMap<>();
        }
        var concreteFlow = getType().resolveFlow(flow);
        return functions.computeIfAbsent(concreteFlow,
                k -> new FlowInstance(getType().resolveFlow(flow), this));
    }

    public Instance getProperty(Property property) {
        return switch (property) {
            case Field field -> getField(field);
            case Flow flow -> getFunction(flow);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    protected InstanceField field(Field field) {
        var instanceField = fields.get(InstanceField::getField, field);
        if (instanceField != null) {
            return instanceField;
        }
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
        return new ClassInstanceParam(
                NncUtils.map(fields, InstanceField::toDTO)
        );
    }

    @Override
    @NoProxy
    public void accept(InstanceVisitor visitor) {
        visitor.visitClassInstance(this);
    }

    @Override
    public void acceptReferences(InstanceVisitor visitor) {
        ensureLoaded();
        for (InstanceField field : fields)
            field.getValue().accept(visitor);
        for (UnknownField unknownField : unknownFields)
            unknownField.getValue().accept(visitor);
    }

    @Override
    public void acceptChildren(InstanceVisitor visitor) {
        ensureLoaded();
        for (InstanceField field : fields) {
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
                    NncUtils.requireNonNull(getId(), "Id required")
            );
        }
    }

    public InstancePO toPO(long tenantId) {
        return toPO(tenantId, new IdentitySet<>());
    }

    InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
        ensureLoaded();
        if (visited.contains(this)) {
            throw new InternalException("Circular reference");
        }
        ensureAllFieldsInitialized();
        visited.add(this);
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
