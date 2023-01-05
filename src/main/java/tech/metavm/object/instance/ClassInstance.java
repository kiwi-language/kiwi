package tech.metavm.object.instance;

import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceFieldValueDTO;
import tech.metavm.object.instance.rest.ClassInstanceParamDTO;
import tech.metavm.object.instance.rest.ReferenceFieldValueDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.IndexConstraintRT;
import tech.metavm.util.*;

import java.util.*;

public class ClassInstance extends Instance {

    protected final ClassType type;
    private transient long version;
    private transient long syncVersion;
    private final Table<InstanceField> fields = new Table<>(InstanceField.class, 1);

    public static Instance allocate(ClassType type) {
        return new ClassInstance(type);
    }

    protected ClassInstance(ClassType type) {
        super(type);
        this.type = type;
    }

    private ClassInstance(ClassType type, Long id, List<InstanceField> fields, long version, long syncVersion) {
        super(id, type, version, syncVersion);
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        initFields(fields);
    }

    public ClassInstance(Map<Field, Instance> data, ClassType type) {
        this(null, data, type, 0L, 0L);
    }

    public ClassInstance(Long id, Map<Field, Instance> data, ClassType type, long version, long syncVersion) {
        super(id, type, version, syncVersion);
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;

        for (Field field : type.getFields()) {
            Instance fieldValue = NncUtils.orElse(data.get(field), InstanceUtils::nullInstance);
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    @NoProxy
    public void initialize(Map<Field, Instance> fieldMap) {
        for (Field field : type.getFields()) {
            Instance fieldValue = fieldMap.get(field);
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    public List<IndexItemPO> getUniqueKeys(long tenantId) {
        List<IndexConstraintRT> uniqueConstraints = type.getConstraints(IndexConstraintRT.class);
        return NncUtils.map(
                uniqueConstraints,
                c -> c.getKey(tenantId,this)
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

    private void initFields(List<InstanceField> fields) {
        for (InstanceField field : fields) {
            addField(field);
        }
    }

    private void addField(InstanceField field) {
        fields.add(field);
    }

    public String getTitle() {
        Field titleField = type.getTileField();
        return titleField != null ? field(titleField).getDisplayValue() : getId() + "";
    }

    public Instance getResolved(List<Long> fieldPath) {
        long fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if(fieldPath.size() > 1) {
            List<Long> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get((ClassInstance) field.getValue(), inst -> inst.getResolved(subFieldPath));
        }
        else {
            return field.getValue();
        }
    }

    @SuppressWarnings("unused")
    public boolean isPersistent() {
        return type.isPersistent();
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
        return type;
    }

    @NoProxy
    public boolean isReference() {
        return !isValue();
    }

    public Instance getInstance(Field field) {
        return field(field).getValue();
    }

    public ClassInstance getClassInstance(Field field) {
        return (ClassInstance) field(field).getValue();
    }

    public Object get(String fieldPath) {
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
//        field(fieldValue.fieldId()).set(fieldValue);
//    }

    public void set(Field field, Instance value) {
        field(field).setValue(value);
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

    public IntInstance getInt(Field field) {
        return (IntInstance) field(field).getValue();
    }

    public IntInstance getInt(long fieldId) {
        return (IntInstance) get(fieldId);
    }

    @SuppressWarnings("unused")
    public IntInstance getInt(String fieldName) {
        return (IntInstance) get(fieldName);
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

    public Instance get(Field field) {
        return field(field).getValue();
    }

    public String getIndexValue(Field field) {
        return IndexKeyPO.getIndexColumn(get(field));
    }

    public Instance get(long fieldId) {
        return get(type.getField(fieldId));
    }

    protected InstanceField field(Field field) {
        return NncUtils.requireNonNull(
                fields.get(InstanceField::getField, field),
                "Can not find instance field for field '" + field + "'"
        );
    }

    private InstanceField field(String name) {
        return fields.get(field -> field.getField().getName(), name);
    }

    public long getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
    public long getSyncVersion() {
        return syncVersion;
    }

//    public void update(InstanceDTO update) {
//        for (InstanceFieldDTO fieldUpdate : update.fields()) {
//            InstanceField field = field(fieldUpdate.fieldId());
//            if(field == null) {
//                throw BusinessException.fieldNotFound(fieldUpdate.fieldId());
//            }
//            field.set(fieldUpdate);
//        }
//    }

    protected InstanceField field(long fieldId) {
        return field(type.getField(fieldId));
    }

    @Override
    protected Object getParam() {
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
                getId(),
                getType().getId(),
                getTitle(),
                getTableData(tenantId, visited),
                version,
                syncVersion
        );
    }

    @Override
    @NoProxy
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        if(isValue()) {
            return toPO(tenantId, visited);
        }
        else {
            return toReferencePO();
        }
    }

    @SuppressWarnings("unused")
    public void incVersion() {
        version++;
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
                ", type=" + type +
                ", title=" + getTitle() +
                '}';
    }

}
