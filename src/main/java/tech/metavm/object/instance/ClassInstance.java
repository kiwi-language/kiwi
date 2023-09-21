package tech.metavm.object.instance;

import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceFieldValueDTO;
import tech.metavm.object.instance.rest.ClassInstanceParamDTO;
import tech.metavm.object.instance.rest.ReferenceFieldValueDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

public class ClassInstance extends Instance {

    private final ClassType klass;
    private final Table<InstanceField> fields = new Table<>(InstanceField.class, 1);

    public static ClassInstance allocate(ClassType type) {
        return new ClassInstance(type);
    }

    protected ClassInstance(ClassType type) {
//        this(Map.of(), type);
        super(null, type,0,0);
        this.klass = type;
    }

    public ClassInstance(Map<Field, Instance> data, ClassType type) {
        this(null, data, type, 0L, 0L);
    }

    public ClassInstance(@Nullable Long id, Map<Field, Instance> data, ClassType type, long version, long syncVersion) {
        super(id, type, version, syncVersion);
        this.klass = type;
        initialize(data, version, syncVersion);
    }

    @NoProxy
    public void initialize(Map<Field, Instance> data, long version, long syncVersion) {
        setVersion(version);
        setSyncVersion(syncVersion);

        for (Field field : klass.getFields()) {
            Instance fieldValue = data.get(field);
            if(fieldValue == null || fieldValue.isNull()) {
                fieldValue = field.getDefaultValue();
            }
            initializeField(field, fieldValue);
        }
    }

    public List<IndexEntryPO> getIndexEntries(long tenantId) {
        return NncUtils.map(
                klass.getConstraints(Index.class),
                c -> c.createIndexEntry(tenantId,this)
        );
    }

    public List<IndexKeyRT> getIndexKeys() {
        return NncUtils.map(
                klass.getConstraints(Index.class),
                c -> c.createIndexKey(this)
        );
    }

    public Set<Instance> getRefInstances() {
        Set<Instance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            Instance fieldValue = field.getValue();
            if(fieldValue.isReference()) {
                result.add(fieldValue);
            }
        }
        return result;
    }

    private void addField(InstanceField field) {
        fields.add(field);
    }

    public String getTitle() {
        Field titleField = klass.getTileField();
        return titleField != null ? field(titleField).getDisplayValue() : getId() + "";
    }

    public Instance getResolved(List<Field> fieldPath) {
        Field field = fieldPath.get(0);
        InstanceField instanceField = field(field);
        if(fieldPath.size() > 1) {
            List<Field> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) instanceField.getValue(), inst -> inst.getResolved(subFieldPath));
        }
        else {
            return instanceField.getValue();
        }
    }

    @SuppressWarnings("unused")
    public boolean isPersistent() {
        return klass.isPersistent();
    }

    @SuppressWarnings("unused")
    public Object get(List<Long> fieldPath) {
        long fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if(fieldPath.size() > 1) {
            List<Long> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) field.getValue(), inst -> inst.get(subFieldPath));
        }
        else {
            return field.getValue();
        }
    }

    @NoProxy
    public ClassType getType() {
        return klass;
    }

    @NoProxy
    public boolean isReference() {
        return !isValue();
    }

    public Instance getInstance(Field field) {
        return field(field).getValue();
    }

    @Override
    public boolean isChild(Instance instance) {
        for (InstanceField field : fields) {
            if(field.getField().isChildField()) {
                Instance fieldValue = field.getValue();
                if(fieldValue == instance || fieldValue.isChild(instance)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Instance> getChildren() {
        Set<Instance> children = new IdentitySet<>();
        for (InstanceField field : fields) {
            if(field.getField().isChildField()) {
                Instance fieldValue = field.getValue();
                if(fieldValue.isNotNull()) {
                    children.add(fieldValue);
                }
            }
        }
        return children;
    }

    public ClassInstance getClassInstance(Field field) {
        return (ClassInstance) field(field).getValue();
    }

    public Instance get(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx == -1) {
            return get(getType().getFieldNyNameRequired(fieldPath));
        }
        else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            ClassInstance fieldInstance = (ClassInstance) getInstance(fieldName);
            return NncUtils.get(fieldInstance, inst -> inst.get(subPath));
        }
    }

    public Instance getInstance(long fieldId) {
        return field(fieldId).getValue();
    }

    public Instance getInstance(String fieldName) {
        return field(fieldName).getValue();
    }

//    public void setRawFieldValue(InstanceFieldDTO fieldValue) {
//        indexItem(fieldValue.fieldId()).set(fieldValue);
//    }

    public void set(Field field, Instance value) {
        field(field).setValue(value);
    }

    public boolean isFieldInitialized(Field field) {
        NncUtils.requireTrue(field.getDeclaringType().isAssignableFrom(getType()));
        return fields.get(InstanceField::getField, field) != null;
    }

    public void initializeField(Field field, Instance value) {
        NncUtils.requireFalse(isFieldInitialized(field));
        addField(new InstanceField(this, field, value));
    }

    public StringInstance getString(long fieldId) {
        return (StringInstance) get(field(fieldId).getField());
    }

    public StringInstance getString(Field field) {
        return (StringInstance) get(field(field).getField());
    }

    @SuppressWarnings("unused")
    public StringInstance getString(String fieldName) {
        return (StringInstance) field(fieldName).getValue();
    }

    public LongInstance getLong(Field field) {
        return (LongInstance) get(field);
    }

    public LongInstance getLong(long fieldId) {
        return (LongInstance) get(fieldId);
    }

    @SuppressWarnings("unused")
    public LongInstance getLong(String fieldName) {
        return (LongInstance) get(fieldName);
    }

    public DoubleInstance getDouble(long fieldId) {
        return (DoubleInstance) get(fieldId);
    }

    public DoubleInstance getDouble(Field field) {
        return (DoubleInstance) get(field);
    }

    public Instance get(Field field) {
        return field(field).getValue();
    }

    public Instance get(long fieldId) {
        return get(klass.getField(fieldId));
    }

    protected InstanceField field(Field field) {
        return NncUtils.requireNonNull(
                fields.get(InstanceField::getField, field),
                "Can not find instance field for '" + field + "'"
        );
    }

    private InstanceField field(String name) {
        return fields.get(field -> field.getField().getName(), name);
    }


//    public void update(InstanceDTO update) {
//        for (InstanceFieldDTO fieldUpdate : update.fields()) {
//            InstanceField indexItem = indexItem(fieldUpdate.fieldId());
//            if(indexItem == null) {
//                throw BusinessException.fieldNotFound(fieldUpdate.fieldId());
//            }
//            indexItem.set(fieldUpdate);
//        }
//    }

    protected InstanceField field(long fieldId) {
        return field(klass.getField(fieldId));
    }

    @Override
    protected ClassInstanceParamDTO getParam() {
        return new ClassInstanceParamDTO(
                NncUtils.map(fields, InstanceField::toDTO)
        );
    }

    @Override
    public FieldValueDTO toFieldValueDTO() {
        if(isValue()) {
            return new InstanceFieldValueDTO(
                    getTitle(),
                    toDTO()
            );
        }
        else {
            return new ReferenceFieldValueDTO(
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
        visited.add(this);
        return new InstancePO(
                tenantId,
                getIdRequired(),
                getType().getId(),
                getTitle(),
                getTableData(tenantId, visited),
                getVersion(),
                getSyncVersion()
        );
    }

    @Override
    @NoProxy
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        if(isValue()) {
            return toPO(tenantId, visited);
        }
        else {
            return toIdentityPO();
        }
    }

    private Map<String, Object> getTableData(long tenantId, IdentitySet<Instance> visited) {
        Map<String, Object> rawData = new HashMap<>();
        for (InstanceField field : fields()) {
            rawData.put(field.getColumnName(), field.getColumnValue(tenantId, visited));
        }
        return rawData;
    }

    public Collection<InstanceField> fields() {
        return fields;
    }

    public ArrayInstance getInstanceArray(Field field) {
        return field(field).getInstanceArray();
    }

    public ArrayInstance getInstanceArray(String fieldName) {
        return field(fieldName).getInstanceArray();
    }

    @Override
    public String toString() {
        return "Instance{" +
                "id=" + getId() +
                ", type=" + klass +
                ", title=" + getTitle() +
                '}';
    }

}
