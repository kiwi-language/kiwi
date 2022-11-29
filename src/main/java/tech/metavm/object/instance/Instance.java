package tech.metavm.object.instance;

import tech.metavm.entity.IdInitializing;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.ContextUtil.getTenantId;

public class Instance implements IInstance, IdInitializing {

    private Long id;
    protected final Type type;
    private transient long version;
    private transient long syncVersion;
    private final java.lang.reflect.Type entityType;
    private final Table<InstanceField> fields = new Table<>(1);

    private Instance(Type type, Long id, List<InstanceField> fields, long version, long syncVersion, Class<?> entityType) {
        initId(id);
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        this.entityType = entityType;
        initFields(fields);
    }

    public Instance(Map<Field, Object> data, Type type, Class<?> entityType) {
        this(null, data, type, 0L, 0L, entityType);
    }

    public Instance(Long id, Map<Field, Object> data, Type type, long version, long syncVersion, Class<?> entityType) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.syncVersion = syncVersion;
        this.entityType = entityType;

        for (Field field : type.getFields()) {
            Object fieldValue = data.get(field);
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    public void initId(long id) {
        if(this.id != null) {
            throw new InternalException("id already initialized");
        }
        this.id = id;
    }

//    public Instance(InstanceDTO instanceDTO,
//                    InstanceContext context) {
////        super(context);
//        type = context.getEntityContext().getType(instanceDTO.typeId());
//        this.version = 1;
//
//        Map<Long, InstanceFieldDTO> fieldDTOMap = NncUtils.toMap(instanceDTO.fields(), InstanceFieldDTO::fieldId);
//        for (Field field : type.getFields()) {
//            InstanceFieldDTO fieldDTO = fieldDTOMap.computeIfAbsent(
//                    field.getId(), fieldId -> InstanceFieldDTO.valueOf(fieldId, null)
//            );
//            addField(new InstanceField(this, field, fieldDTO));
//        }
//        context.bind(this);
//    }

    public List<IndexItemPO> getUniqueKeys() {
        List<UniqueConstraintRT> uniqueConstraints = type.getConstraints(UniqueConstraintRT.class);
        return NncUtils.map(
                uniqueConstraints,
                c -> c.getKey(this)
        );
    }

    private void initFields(List<InstanceField> fields) {
        for (InstanceField field : fields) {
            addField(field);
        }
    }

    private void addField(InstanceField field) {
//        name2field.put(field.getName(), field);
        fields.add(field);
    }

//    public long getTenantId() {
//        return context.getTenantId();
//    }

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
    public Object getRaw(List<Long> fieldPath) {
        long fieldId = fieldPath.get(0);
        InstanceField field = field(fieldId);
        if(fieldPath.size() > 1) {
            List<Long> subFieldPath = fieldPath.subList(1, fieldPath.size());
            return NncUtils.get(field.getInstance(), inst -> inst.getRaw(subFieldPath));
        }
        else {
            return field.getValue();
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public Instance getInstance(Field field) {
        if(field.getType().isReference()) {
            return field(field).getInstance();
        }
        throw new ClassCastException();
    }

    public Type getInstanceType() {
        return getType();
    }

    public Object getRaw(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx == -1) {
            return getRaw(getType().getFieldNyNameRequired(fieldPath));
        }
        else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            Instance fieldInstance = getInstance(fieldName);
            return NncUtils.get(fieldInstance, inst -> inst.getRaw(subPath));
        }
    }

    public Instance getInstance(long fieldId) {
        return field(fieldId).getInstance();
    }

    public Instance getInstance(String fieldName) {
        return field(fieldName).getInstance();
    }

    public void setRawFieldValue(InstanceFieldDTO fieldValue) {
        field(fieldValue.fieldId()).set(fieldValue);
    }

    @Override
    public void set(Field field, Object value) {
        field(field).setValue(value);
    }

    public String getString(long fieldId) {
        return (String) getRaw(field(fieldId).getField());
    }

    public String getString(Field field) {
        return (String) getRaw(field(field).getField());
    }

    @SuppressWarnings("unused")
    public String getString(String fieldName) {
        return (String) getRaw(field(fieldName).getField());
    }

    public Integer getInt(Field field) {
        return (Integer) field(field).getValue();
    }

    public Integer getInt(long fieldId) {
        return (Integer) getRaw(fieldId);
    }

    @SuppressWarnings("unused")
    public Integer getInt(String fieldName) {
        return (Integer) getRaw(fieldName);
    }

    public Long getLong(long fieldId) {
        return (Long) getRaw(fieldId);
    }

    @SuppressWarnings("unused")
    public Long getLong(String fieldName) {
        return (Long) getRaw(fieldName);
    }

    public Double getDouble(long fieldId) {
        return (Double) getRaw(fieldId);
    }

    public Object getRaw(Field field) {
        return field(field).getValue();
    }

    public String getIndexValue(Field field) {
        return IndexKeyPO.getIndexColumn(getRaw(field));
    }

    public Object getRaw(long fieldId) {
        return getRaw(type.getField(fieldId));
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

//    public VersionPO nextVersion() {
//        return new VersionPO(getTenantId(), getId(), version + 1);
//    }

    @SuppressWarnings("unused")
    public long getSyncVersion() {
        return syncVersion;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void update(InstanceDTO update) {
        for (InstanceFieldDTO fieldUpdate : update.fields()) {
            InstanceField field = field(fieldUpdate.fieldId());
            if(field == null) {
                throw BusinessException.fieldNotFound(fieldUpdate.fieldId());
            }
            field.set(fieldUpdate);
        }
    }

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

    public InstancePO toPO() {
        return new InstancePO(
                getTenantId(),
                getId(),
                getType().getId(),
                getTitle(),
                getTableData(),
                version,
                syncVersion
        );
    }

    @SuppressWarnings("unused")
    public void incVersion() {
        version++;
    }

    private Map<String, Object> getTableData() {
        Map<String, Object> rawData = new HashMap<>();
        for (InstanceField field : fields()) {
            if(field.isGeneralPrimitive()) {
                rawData.put(field.getColumnName(), field.getColumnValue());
            }
        }
        return rawData;
    }

    public Collection<InstanceField> fields() {
        return fields;
    }

    @Override
    public java.lang.reflect.Type getEntityType() {
        return entityType;
    }

    @Override
    public InstanceArray getInstanceArray(Field field) {
        return field(field).getInstanceArray();
    }

    //    @Override
//    public InstanceContext getContext() {
//        return context;
//    }

//    public Instance copy() {
//        Instance copy = new Instance(
//                getType(),
//                getId(),
//                List.of(),
////                context,
//                version,
//                syncVersion,
//                entityType
//        );
//        copy.initFields(
//                NncUtils.map(fields(), f -> f.copy(copy))
//        );
//        return copy;
//    }

//    public void remove() {
//        context.remove(this);
//    }

    @Override
    public String toString() {
        return "Instance {category: " + getType().getName() + ", id: " + getId() + ", title: " + getTitle() + "}";
    }
}
