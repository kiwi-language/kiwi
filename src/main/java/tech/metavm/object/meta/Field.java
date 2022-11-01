package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

public class Field extends Entity {
    private String name;
    private final Type declaringType;
    private Access access;
    private Object defaultValue;
    private boolean asTitle;
    private final Column column;
    private Type type;

    public Field(FieldPO po, Type declaringType, Type type) {
        this(
                po.getId(),
                po.getName(),
                declaringType,
                Access.getByCodeRequired(po.getAccess()),
                po.getUnique(),
                po.getAsTitle(),
                DefaultValueUtil.convertFromStr(po.getDefaultValue(), type),
                Column.valueOf(po.getColumnName()),
                type,
                declaringType.getContext(),
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
        this.declaringType = requireNonNull(owner, "属性所属类型");
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

    public Type getDeclaringType() {
        return declaringType;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public TypeCategory getConcreteTypeCategory() {
        return getConcreteType().getCategory();
    }

    public Type getConcreteType() {
        return type.getConcreteType();
    }

    public void update(FieldDTO update) {
        if(update.typeId() != null && !Objects.equals(type.getId(), update.typeId())) {
            throw BusinessException.invalidField(this, "类型不允许修改");
        }
        setName(update.name());
        setAccess(Access.getByCodeRequired(update.access()));
        setUnique(update.unique());
        setAsTitle(update.asTitle());
        setDefaultValue(update.defaultValue());
        setUnique(update.unique());
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
            throw BusinessException.invalidField(this, "数组不支持唯一性约束");
        }
        UniqueConstraintRT constraint = declaringType.getUniqueConstraint(List.of(this));
        if(constraint != null && !unique) {
            declaringType.removeConstraint(constraint.getId());
        }
        if(constraint == null && unique) {
            declaringType.addConstraint(ConstraintFactory.newUniqueConstraint(List.of(this)));
        }
    }

    public void remove() {
        declaringType.removeField(this);
        context.remove(this);
    }

    public boolean isCustomTyped() {
        return !isGeneralPrimitive();
    }

    public boolean isTable() {
        return type.isClass();
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
        return getConcreteType().isLong();
    }

    public boolean isNumber() {
        return getConcreteType().isDouble();
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

    public boolean isGeneralPrimitive() {
        return type.isNotNull() ? type.isPrimitive() : type.getUnderlyingType().isPrimitive();
    }

    public boolean isUnique() {
        return declaringType.getUniqueConstraint(List.of(this)) != null;
    }

    public boolean isNotNull() {
        return getType().isNotNull();
    }

    public boolean isAsTitle() {
        return asTitle;
    }

    public void setAsTitle(boolean asTitle) {
        if(asTitle) {
            Field titleField = declaringType.getTileField();
            if(titleField != null && !titleField.equals(this)) {
                throw BusinessException.multipleTitleFields();
            }
        }
        this.asTitle = asTitle;
    }

    public List<EnumConstant> getChoiceOptions() {
        return getConcreteType().isEnum() ? getConcreteType().getEnumConstants() : List.of();
    }

    public Column getColumn() {
        return column;
    }

    public String getColumnName() {
        return column.name();
    }

    public Object preprocessValue(Object rawValue) {
        return ValueFormatter.parse(rawValue, type);
    }

    public String getStrRawDefaultValue() {
        return DefaultValueUtil.convertToStr(defaultValue, getType().getCategory().code(), isArray());
    }

    public String getFullyQualifiedName() {
        return declaringType.getName() + "." + name;
    }

    public FieldPO toPO() {
        FieldPO po = new FieldPO();
        po.setId(id);
        po.setTenantId(ContextUtil.getTenantId());
        po.setName(name);
        po.setDeclaringTypeId(declaringType.getId());
        po.setUnique(isUnique());
        po.setDefaultValue(getStrRawDefaultValue());
        po.setAccess(access.code());
        po.setColumnName(NncUtils.get(column, Column::name));
        po.setAsTitle(asTitle);
        po.setTypeId(type.getId());
        return po;
    }

    public FieldDTO toDTO() {
        return toDTO(false);
    }

    public FieldDTO toDTO(boolean withType) {
        return new FieldDTO(
                id,
                name,
                access.code(),
                defaultValue,
                isUnique(),
                asTitle,
                declaringType.getId(),
                type.getId(),
                withType ? type.toDTO(false, false, false) : null
        );
    }

    public boolean isTime() {
        return getType().isTime();
    }

}
