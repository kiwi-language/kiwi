package tech.metavm.object.instance;

import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.*;

public class Instance implements IInstance, IdInitializing {

    private Long id;
    protected final Type type;
    private transient long version;
    private transient long syncVersion;
    private final Table<InstanceField> fields = new Table<>(1);

    public static Instance allocate(Type type) {
        return new Instance(type);
    }

    protected Instance(Type type) {
        this.type = type;
    }

    private Instance(Type type, Long id, List<InstanceField> fields, long version, long syncVersion) {
        initId(id);
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        initFields(fields);
    }

    public Instance(Map<Field, Object> data, Type type) {
        this(null, data, type, 0L, 0L);
    }

    public Instance(Long id, Map<Field, Object> data, Type type, long version, long syncVersion) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;

        for (Field field : type.getFields()) {
            Object fieldValue = data.get(field);
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    @NoProxy
    public void initialize(Map<Field, Object> fieldMap) {
        for (Field field : type.getFields()) {
            Object fieldValue = fieldMap.get(field);
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    @NoProxy
    public void initId(long id) {
        if(this.id != null) {
            throw new InternalException("id already initialized");
        }
        this.id = id;
    }

    public List<IndexItemPO> getUniqueKeys(long tenantId) {
        List<UniqueConstraintRT> uniqueConstraints = type.getConstraints(UniqueConstraintRT.class);
        return NncUtils.map(
                uniqueConstraints,
                c -> c.getKey(tenantId,this)
        );
    }

    public Set<Instance> getRefInstances() {
        Set<Instance> result = new IdentitySet<>();
        for (InstanceField field : fields) {
            if(field.getField().isReference()) {
                NncUtils.invokeIfNotNull(field.getInstance(), result::add);
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

    public Object getResolved(List<Long> fieldPath) {
        long fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if(fieldPath.size() > 1) {
            List<Long> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get(field.getInstance(), inst -> inst.getResolved(subFieldPath));
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
            return NncUtils.get(field.getInstance(), inst -> inst.get(subFieldPath));
        }
        else {
            return field.getValue();
        }
    }

    @NoProxy
    public Type getType() {
        return type;
    }

    public boolean isValue() {
        return type.isValue();
    }

    @Override
    public Instance getInstance(Field field) {
        if(field.getType().isReference() || field.getType().isValue()) {
            return field(field).getInstance();
        }
        throw new ClassCastException();
    }

    public Type getInstanceType() {
        return getType();
    }

    public Object get(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx == -1) {
            return get(getType().getFieldNyNameRequired(fieldPath));
        }
        else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            Instance fieldInstance = getInstance(fieldName);
            return NncUtils.get(fieldInstance, inst -> inst.get(subPath));
        }
    }

    public Instance getInstance(long fieldId) {
        return field(fieldId).getInstance();
    }

    public Instance getInstance(String fieldName) {
        return field(fieldName).getInstance();
    }

//    public void setRawFieldValue(InstanceFieldDTO fieldValue) {
//        field(fieldValue.fieldId()).set(fieldValue);
//    }

    @Override
    public void set(Field field, Object value) {
        field(field).setValue(value);
    }

    public String getString(long fieldId) {
        return (String) get(field(fieldId).getField());
    }

    public String getString(Field field) {
        return (String) get(field(field).getField());
    }

    @SuppressWarnings("unused")
    public String getString(String fieldName) {
        return (String) get(field(fieldName).getField());
    }

    public Integer getInt(Field field) {
        return (Integer) field(field).getValue();
    }

    public Integer getInt(long fieldId) {
        return (Integer) get(fieldId);
    }

    @SuppressWarnings("unused")
    public Integer getInt(String fieldName) {
        return (Integer) get(fieldName);
    }

    public Long getLong(Field field) {
        return (Long) get(field);
    }

    public Long getLong(long fieldId) {
        return (Long) get(fieldId);
    }

    @SuppressWarnings("unused")
    public Long getLong(String fieldName) {
        return (Long) get(fieldName);
    }

    public Double getDouble(long fieldId) {
        return (Double) get(fieldId);
    }

    public Object get(Field field) {
        return field(field).getValue();
    }

    public String getIndexValue(Field field) {
        return IndexKeyPO.getIndexColumn(get(field));
    }

    public Object get(long fieldId) {
        return get(type.getField(fieldId));
    }

    protected InstanceField field(Field field) {
        return fields.get(InstanceField::getField, field);
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

    @Override
    @NoProxy
    public Long getId() {
        return id;
    }

    public ReferencePO toReferencePO() {
        NncUtils.requireNonNull(id);
        return new ReferencePO(id);
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

    public InstanceDTO toDTO() {
        return new InstanceDTO(
                getId(),
                getType().getId(),
                getType().getName(),
                getTitle(),
                NncUtils.map(fields, InstanceField::toDTO),
                null
        );
    }

    public InstancePO toPO(long tenantId) {
        return toPO(tenantId, new IdentitySet<>());
    }

    InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
        if(visited.contains(this)) {
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

    @Override
    public InstanceArray getInstanceArray(Field field) {
        return field(field).getInstanceArray();
    }

    public InstanceArray getInstanceArray(String fieldName) {
        return field(fieldName).getInstanceArray();
    }

    @Override
    public String toString() {
        return "Instance{" +
                "id=" + id +
                ", type=" + type +
                ", title=" + getTitle() +
                '}';
    }
}
