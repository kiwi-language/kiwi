package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TitleFieldDTO;
import tech.metavm.util.*;

import java.util.*;

import static tech.metavm.util.NncUtils.requireNonNull;

public class Field extends Entity {
    private String name;
    private final Type owner;
    private Access access;
    private Object defaultValue;
    private boolean unique;
    private boolean asTitle;
    private final Column column;
    private Type type;

    public Field(FieldPO po, Type owner, Type type) {
        this(
                po.getId(),
                po.getName(),
                owner,
                Access.getByCodeRequired(po.getAccess()),
                po.getUnique(),
                po.getAsTitle(),
                DefaultValueUtil.convertFromStr(po.getDefaultValue(), type.getCategory()),
                Column.valueOf(po.getColumnName()),
                type,
                owner.getContext(),
                false
        );
    }

    public Field(FieldDTO fieldDTO, Type owner, Type type) {
        this(
                null,
                fieldDTO.name(),
                owner,
                Access.getByCodeRequired(fieldDTO.access()),
                fieldDTO.unique(),
                fieldDTO.asTitle(),
                fieldDTO.defaultValue(),
                null,
                type,
                owner.getContext(),
                true
        );
    }

    Field(
             Long id,
             String name,
             Type owner,
             Access access,
             boolean unique,
             boolean asTitle,
             Object defaultValue,
             Column column,
             Type type,
             EntityContext context,
             boolean addToType
    ) {
        super(id, context);
        this.owner = requireNonNull(owner, "属性所属类型");
        this.access = requireNonNull(access, "属性访问控制");
        this.type = type;
        this.asTitle = asTitle;
        this.column = column != null ? column : owner.allocateColumn(this);
        setName(name);
        setUnique(unique);
        setDefaultValue(defaultValue);
        if(addToType) {
            owner.addField(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public Type getOwner() {
        return owner;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public TypeCategory getBaseTypeCategory() {
        return getConcreteType().getCategory();
    }

    public Type getConcreteType() {
        return type.getConcreteType();
    }

    public void update(FieldDTO update) {
        if(update.targetId() != null && !Objects.equals(getConcreteType().getId(), update.targetId())) {
            throw BusinessException.invalidField(this, "关联类型不允许修改");
        }
        Type type = context.resolveType(update);
        setType(type);
        setAccess(Access.getByCodeRequired(update.access()));
        setUnique(update.unique());
        setAsTitle(update.asTitle());
        setDefaultValue(update.defaultValue());
    }

    public void setType(Type type) {
        Type concreteType = type.getConcreteType();
        if(!EntityUtils.entityEquals(getConcreteType(), concreteType)) {
            throw BusinessException.invalidField(this, "列类型不允许修改");
        }
        if(isArray() != type.isArray()) {
            throw BusinessException.invalidField(this, "是否多选不支持修改");
        }
        this.type = type;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = preprocessValue(defaultValue);
    }

    public void setUnique(boolean unique) {
        if(unique && isArray()) {
            throw BusinessException.invalidField(this, "当前属性类型不支持唯一性约束");
        }
        this.unique = unique;
    }

    public void remove() {
        owner.removeField(this);
        context.remove(this);
    }

    public boolean isComposite() {
        return !isPrimitive();
    }

    public boolean isTable() {
        return type.isTable();
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isArray() {
        return type.isArray();
    }

    public boolean isNullable() {
        return !isNotNull();
    }

    public boolean isSingleValued() {
        return !isArray();
    }

    public boolean isInt64() {
        return getConcreteType().isInt64();
    }

    public boolean isNumber() {
        return getConcreteType().isNumber();
    }

    public boolean isBool() {
        return getConcreteType().isBool();
    }

    public boolean isString() {
        return getConcreteType().isString();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isNotNull() {
        return getType().isNotNull();
    }

    public boolean isAsTitle() {
        return asTitle;
    }

    public void setAsTitle(boolean asTitle) {
        if(asTitle) {
            Field titleField = owner.getTileField();
            if(titleField != null && !titleField.equals(this)) {
                throw BusinessException.multipleTitleFields();
            }
        }
        this.asTitle = asTitle;
    }

    public List<ChoiceOption> getChoiceOptions() {
        return getConcreteType().isEnum() ? getConcreteType().getChoiceOptions() : List.of();
    }

    public ChoiceOption getOption(long id) {
        return getConcreteType().getChoiceOption(id);
    }

    public Column getColumn() {
        return column;
    }

    public String getColumnName() {
        return column.name();
    }

    public Object preprocessValue(Object value) {
        return ValueParser.convertValue(getType().getCategory(), isArray(), value);
    }

    public String getStrRawDefaultValue() {
        return DefaultValueUtil.convertToStr(defaultValue, getType().getCategory().code(), isArray());
    }

    public String getFullName() {
        return owner.getName() + "." + name;
    }

    public FieldPO toPO() {
        FieldPO po = new FieldPO();
        po.setId(id);
        po.setTenantId(ContextUtil.getTenantId());
        po.setName(name);
        po.setOwnerId(owner.getId());
        po.setUnique(unique);
        po.setRequired(isNotNull());
        po.setMultiValued(isArray());
        po.setDefaultValue(getStrRawDefaultValue());
        po.setAccess(access.code());
        po.setColumnName(NncUtils.get(column, Column::name));
        po.setAsTitle(asTitle);
        po.setTypeId(type.getId());
        return po;
    }

    public Set<Long> getDefaultSelectedOptionIds() {
        if(defaultValue instanceof Long) {
            return Set.of((Long) defaultValue);
        }
        if(defaultValue instanceof Collection) {
            return new HashSet<>((Collection) defaultValue);
        }
        return Set.of();
    }

    public FieldDTO toDTO() {
        return toDTO(asTitle);
    }

    public TitleFieldDTO toTitleDTO() {
        if(!asTitle) {
            throw new RuntimeException("Field " + name + " is not the title field");
        }
        return new TitleFieldDTO(
                name,
                getType().getCategory().code(),
                unique,
                defaultValue
        );
    }

    public FieldDTO toDTO(boolean asTitle) {
        return new FieldDTO(
                id,
                name,
                getBaseTypeCategory().code(),
                access.code(),
                isNotNull(),
                defaultValue,
                unique,
                asTitle,
                isArray(),
                owner.getId(),
                getConcreteType().isPrimitive() ? null : getConcreteType().getId(),
                getConcreteType().getName(),
                getChoiceOptionDTOs(),
                type.getId()
        );
    }

    private List<ChoiceOptionDTO> getChoiceOptionDTOs() {
        return getConcreteType().isEnum() ? OptionUtil.getOptionDTOs(getConcreteType(), getDefaultSelectedOptionIds()) : List.of();
    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Field nField = (Field) o;
//        return tenantId == nField.tenantId && Objects.equals(objectId, nField.objectId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(tenantId, objectId);
//    }

    public boolean contentEquals(Field that) {
        return equals(that)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(owner, that.owner)
                && Objects.equals(defaultValue, that.defaultValue)
                && access == that.access
                && unique == that.unique;
    }

    public boolean isTime() {
        return getType().isTime();
    }

}
