package tech.metavm.object.instance.core;

import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceFieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.object.meta.Property;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

public class ClassInstance extends Instance {

    private final ReadWriteArray<InstanceField> fields = new ReadWriteArray<>(InstanceField.class);
    private final ReadWriteArray<DirtyField> dirtyFields = new ReadWriteArray<>(DirtyField.class);
    private transient Map<Flow, FlowInstance> functions;

    public static ClassInstance allocate(ClassType type) {
        return new ClassInstance(type);
    }

    public ClassInstance(ClassType type) {
        super(null, type, null, 0, 0);
    }

    public ClassInstance(ClassType type, @Nullable InstanceParentRef parentRef) {
        super(null, type, parentRef, 0, 0);
    }

    public ClassInstance(Map<Field, Instance> data, ClassType type) {
        this(null, data, type, null, 0L, 0L);
    }

    public ClassInstance(Map<Field, Instance> data, ClassType type,
                         @Nullable InstanceParentRef parentRef) {
        this(null, data, type, parentRef, 0L, 0L);

    }

    public ClassInstance(@Nullable Long id, Map<Field, Instance> data, ClassType type,
                         @Nullable InstanceParentRef parentRef,
                         long version, long syncVersion) {
        super(id, type, parentRef, version, syncVersion);
        data.forEach(this::initField);
        ensureAllFieldsInitialized();
    }

    @NoProxy
    public void reload(Map<Field, Instance> data,
                       long version, long syncVersion) {
        reset();
        setVersion(version);
        setSyncVersion(syncVersion);
        for (Field field : getType().getAllFields()) {
            Instance fieldValue = data.get(field);
            if (fieldValue == null || fieldValue.isNull()) {
                fieldValue = field.getDefaultValue();
            }
            initFieldInternal(field, fieldValue);
        }
    }

    private void reset() {
        for (ReferenceRT ref : new ArrayList<>(getOutgoingReferences())) {
            ref.clear();
        }
        this.fields.clear();
    }

    public List<IndexEntryPO> getIndexEntries(IEntityContext entityContext) {
        return NncUtils.flatMap(
                getType().getConstraints(Index.class),
                c -> c.createIndexEntry(this, entityContext)
        );
    }

    public List<IndexKeyRT> getIndexKeys(IEntityContext entityContext) {
        return NncUtils.flatMap(
                getType().getConstraints(Index.class),
                c -> c.createIndexKey(this, entityContext)
        );
    }

    public Set<Instance> getRefInstances() {
        Set<Instance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            Instance fieldValue = field.getValue();
            if (fieldValue.isReference()) {
                result.add(fieldValue);
            }
        }
        for (DirtyField dirtyField : dirtyFields) {
            Instance fieldValue = dirtyField.getValue();
            if (fieldValue.isReference()) {
                result.add(fieldValue);
            }
        }
        return result;
    }

    private void addField(InstanceField field) {
        NncUtils.requireTrue(fields.get(InstanceField::getField, field) == null,
                "Field '" + field.getName() + " is already added");
        fields.add(field);
    }

    public String getTitle() {
        Field titleField = getType().getTileField();
        return titleField != null ? field(titleField).getDisplayValue() : getId() + "";
    }

    public Object getField(List<Long> fieldPath) {
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
        return field(field).getValue();
    }

    public void setDirtyField(ClassType declaringType, Column column, Instance value) {
        var field = NncUtils.find(dirtyFields,
                f -> f.getDeclaringType() == declaringType && f.getColumn() == column);
        if (field != null) {
            field.setValue(value);
        } else {
            dirtyFields.add(new DirtyField(declaringType, column, value));
        }
    }

    @Override
    public boolean isChild(Instance instance) {
        for (InstanceField field : fields) {
            if (field.getField().isChildField()) {
                Instance fieldValue = field.getValue();
                if (fieldValue == instance || fieldValue.isChild(instance)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Instance> getChildren() {
        Set<Instance> children = new IdentitySet<>();
        for (InstanceField field : fields) {
            if (field.getField().isChildField()) {
                Instance fieldValue = field.getValue();
                if (fieldValue.isNotNull()) {
                    children.add(fieldValue);
                }
            }
        }
        return children;
    }

    public ClassInstance getClassInstance(Field field) {
        return (ClassInstance) field(field).getValue();
    }

    public Instance getField(String fieldPath) {
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
        return field(getType().tryGetFieldByName(fieldName)).getValue();
    }

    public void setField(Field field, Instance value) {
        if (field.isChildField()) {
            throw new BusinessException(ErrorCode.CAN_NOT_ASSIGN__CHILD_FIELD);
        }
        setFieldInternal(field, value);
    }

    void setChild(Field field, Instance value) {
        NncUtils.requireTrue(field.isChildField());
        if (isFieldInitialized(field)) {
            setFieldInternal(field, value);
        } else {
            initFieldInternal(field, value);
        }
    }

    private void setFieldInternal(Field field, Instance value) {
        field(field).setValue(value);
    }

    public boolean isFieldInitialized(Field field) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        return fields.get(InstanceField::getField, field) != null;
    }

    public void initField(Field field, Instance value) {
        NncUtils.requireFalse(field.isChildField());
        initFieldInternal(field, value);
    }

    private void initFieldInternal(Field field, Instance value) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        NncUtils.requireFalse(isFieldInitialized(field));
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
        return field(field).getValue();
    }

    public FlowInstance getFunction(Flow flow) {
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
        var dirtyField = dirtyFields.get(DirtyField::getColumn, field.getColumn());
        if (dirtyField != null) {
            dirtyFields.remove(dirtyField);
            instanceField = new InstanceField(this, field, dirtyField.getValue());
            fields.add(instanceField);
            return instanceField;
        }
        throw new InternalException("Can not find instance field for '" + field + "'");
    }

    protected InstanceField field(long fieldId) {
        return field(getType().getField(fieldId));
    }

    @Override
    protected ClassInstanceParam getParam() {
        return new ClassInstanceParam(
                NncUtils.map(fields, InstanceField::toDTO)
        );
    }

    @Override
    public FieldValue toFieldValueDTO() {
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
        if (visited.contains(this)) {
            throw new InternalException("Circular reference");
        }
        ensureAllFieldsInitialized();
        visited.add(this);
        return new InstancePO(
                tenantId,
                getIdRequired(),
                getType().getId(),
                getTitle(),
                getTableData(tenantId, visited),
                NncUtils.get(getParent(), Instance::getId),
                NncUtils.get(getParentField(), Field::getId),
                getVersion(),
                getSyncVersion()
        );
    }

    @Override
    @NoProxy
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        if (isValue()) {
            return toPO(tenantId, visited);
        } else {
            return toIdentityPO();
        }
    }

    private Map<String, Map<String, Object>> getTableData(long tenantId, IdentitySet<Instance> visited) {
        Map<String, Map<String, Object>> rawData = new HashMap<>();
        var type = getType();
        Map<ClassType, List<DirtyField>> dirtyFieldMap = NncUtils.toMultiMap(
                dirtyFields,
                DirtyField::getDeclaringType
        );
        while (type != null) {
            Map<String, Object> subMap = new HashMap<>();
            for (Field field : type.getFields()) {
                subMap.put(field.getColumnName(), getField(field).toColumnValue(tenantId, visited));
            }
            var dirtyFields = dirtyFieldMap.get(type);
            if (dirtyFields != null) {
                for (DirtyField dirtyField : dirtyFields) {
                    subMap.put(dirtyField.getColumn().name(), dirtyField.getValue().toColumnValue(tenantId, visited));
                }
            }
            if (!subMap.isEmpty()) {
                rawData.put(NncUtils.toBase64(type.getIdRequired()), subMap);
            }
            type = type.getSuperClass();
        }
        return rawData;
    }

    public List<InstanceField> fields() {
        return fields.toList();
    }

    public ArrayInstance getInstanceArray(Field field) {
        return field(field).getInstanceArray();
    }

    public ArrayInstance getInstanceArray(String fieldName) {
        return field(getType().tryGetFieldByName(fieldName)).getInstanceArray();
    }

    private void ensureFieldInitialized(Field field) {
        if(fields.get(InstanceField::getField, field) != null) {
            return;
        }
        var dirtyField = dirtyFields.remove(DirtyField::getColumn, field.getColumn());
        if(dirtyField != null) {
            initFieldInternal(field, dirtyField.getValue());
            return;
        }
        throw new BusinessException(
                ErrorCode.FIELD_NOT_INITIALIZED,
                getType().getName(),
                field.getName()
        );
    }

    public void ensureAllFieldsInitialized() {
        for (Field field : getType().getAllFields()) {
            ensureFieldInitialized(field);
        }
    }
}
