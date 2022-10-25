package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class Instance extends AbsInstance {

    private final long tenantId;
    private long version;
    private long syncVersion;
    private final InstanceContext context;
    private final Map<Long, InstanceField> id2field = new LinkedHashMap<>();
    private final Map<String, InstanceField> name2field = new LinkedHashMap<>();

    private Instance(long tenantId, Type type, Long id, List<InstanceField> fields, InstanceContext context, long version, long syncVersion) {
        super(id, type);
        this.tenantId = tenantId;
        this.context = context;
        this.version = version;
        this.syncVersion = syncVersion;
        initFields(fields);
    }

    public Instance(InstancePO record,
                    List<RelationPO> relations,
                    InstanceContext context) {
        super(record.id(), context.getType(record.typeId()));
        this.context = context;
        tenantId = record.tenantId();
        version = record.version();
        syncVersion = record.syncVersion();

        Map<Long, List<RelationPO>> relationMap = NncUtils.toMultiMap(relations, RelationPO::getFieldId);

        for (Field field : getType().getFields()) {
            Object fieldValue;
            if(field.isGeneralPrimitive()) {
                fieldValue = record.get(field.getColumn().name());
            }
            else if(field.isSingleValued()) {
                fieldValue = NncUtils.mapFirst(relationMap.get(field.getId()), RelationPO::getDestInstanceId);
            }
            else {
                fieldValue = NncUtils.map(relationMap.get(field.getId()), RelationPO::getDestInstanceId);
            }
            addField(new InstanceField(this, field, fieldValue));
        }
    }

    public Instance(long tenantId,
                    InstanceDTO instanceDTO,
                    InstanceContext context) {
        super(instanceDTO.id(), context.getType(instanceDTO.typeId()));
        this.context = context;
        this.tenantId = tenantId;
        this.version = 1;

        for (InstanceFieldDTO fieldDTO : instanceDTO.fields()) {
            Field field = getType().getField(fieldDTO.fieldId());
            addField(new InstanceField(this, field, fieldDTO));
        }
    }

    private void initFields(List<InstanceField> fields) {
        for (InstanceField field : fields) {
            addField(field);
        }
    }

    private void addField(InstanceField field) {
        name2field.put(field.getName(), field);
        id2field.put(field.getId(), field);
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
            return field.getResolvedValue();
        }
    }

    public boolean isPersistent() {
        return type.isPersistent();
    }

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

    public void set(long fieldId, Object fieldValue) {
        field(fieldId).setRawValue(fieldValue);
    }

    public Long getLong(String fieldName) {
        return (Long) getRaw(fieldName);
    }

    public List<Long> getLongList(String fieldName) {
        return (List<Long>) getRaw(fieldName);
    }

    public Object getRaw(Field field) {
        return id2field.get(field.getId()).getValue();
    }

    public Object getRaw(long fieldId) {
        return id2field.get(fieldId).getValue();
    }

    private InstanceField field(Field field) {
        return id2field.get(field.getId());
    }

    private InstanceField field(String name) {
        return name2field.get(name);
    }

    public long getVersion() {
        return version;
    }

    public VersionPO nextVersion() {
        return new VersionPO(getTenantId(), getId(), version + 1);
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void update(InstanceDTO update) {
        for (InstanceFieldDTO fieldUpdate : update.fields()) {
            InstanceField field = field(fieldUpdate.fieldId());
            if(field == null) {
                throw BusinessException.fieldNotFound(fieldUpdate.fieldId());
            }
            field.setRawValue(fieldUpdate.value());
        }
    }

    private InstanceField field(long fieldId) {
        return id2field.get(fieldId);
    }

    public InstanceDTO toDTO() {
        context.loadRelationTitles(List.of(this));
        return new InstanceDTO(
                getId(),
                getType().getId(),
                getType().getName(),
                getTitle(),
                NncUtils.map(name2field.values(), InstanceField::toDTO)
        );
    }

    public InstancePO toPO() {
        return new InstancePO(
                tenantId,
                getId(),
                getType().getId(),
                getTitle(),
                getTableData(),
                version,
                syncVersion
        );
    }

    public void incVersion() {
        version++;
    }

    public List<InstanceRelation> getRelations() {
        List<InstanceRelation> relations = new ArrayList<>();
        for (InstanceField field : fields()) {
            if(field.isGeneralRelation()) {
                for (Long destInstanceId : field.getDestInstanceIds()) {
                    relations.add(
                            new InstanceRelation(this, field, destInstanceId)
                    );
                }
            }
        }
        return relations;
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
        return name2field.values();
    }

    @Override
    public Instance copy() {
        Instance copy = new Instance(
                tenantId,
                getType(),
                getId(),
                List.of(),
                context,
                version,
                syncVersion
        );
        copy.initFields(
                NncUtils.map(fields(), f -> f.copy(copy))
        );
        return copy;
    }

    InstanceContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "Instance {category: " + getType().getName() + ", id: " + getId() + ", title: " + getTitle() + "}";
    }
}
