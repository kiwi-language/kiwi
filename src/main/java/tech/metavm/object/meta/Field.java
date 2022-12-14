package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("字段")
public class Field extends Entity {

    @EntityField("名称")
    private String name;
    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField("可见范围")
    private Access access;
    @EntityField("默认值")
    @Nullable
    private Object defaultValue;
    @EntityField("是否作为标题")
    private boolean asTitle;
    @EntityField("列")
    private final Column column;
    @EntityField("类型")
    private Type type;
    @EntityField("是否从对象字段")
    private boolean isChildField;

    public Field(String name, ClassType declaringType, Type type) {
        this(name, declaringType, Access.GLOBAL, false, false, null, type, false);
    }

    public Field(
             String name,
             ClassType declaringType,
             Access access,
             Boolean unique,
             boolean asTitle,
             Object defaultValue,
             Type type,
             boolean isChildField
    ) {
        this.declaringType = requireNonNull(declaringType, "属性所属类型");
        this.access = requireNonNull(access, "属性访问控制");
        this.type = type;
        this.asTitle = asTitle;
        setName(name);
        this.column = NncUtils.requireNonNull(declaringType.allocateColumn(this),
                "Fail to allocate a column for field " + this);
        setDefaultValue(defaultValue);
        declaringType.addField(this);
        this.isChildField = isChildField;
        if(unique != null) {
            setUnique(unique);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    @JsonIgnore
    public ClassType getDeclaringType() {
        return declaringType;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public boolean isChildField() {
        return isChildField;
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

    void setType(Type type) {
//        ClassType concreteType = type.getConcreteType();
//        if(!EntityUtils.entityEquals(getConcreteType(), concreteType)) {
//            throw BusinessException.invalidField(this, "列类型不允许修改");
//        }
//        if(isArray() != type.isArray()) {
//            throw BusinessException.invalidField(this, "是否多选不支持修改");
//        }
        this.type = type;
    }

    public void setChildField(boolean childField) {
        isChildField = childField;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public void setDefaultValue(@Nullable Object defaultValue) {
        this.defaultValue = defaultValue;
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
            ConstraintFactory.newUniqueConstraint(List.of(this));
        }
    }

    public void remove() {
        declaringType.removeField(this);
//        context.remove(this);
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isArray() {
        return type.isArray();
    }

    public boolean isReference() {
        return type.isReference();
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
        return getConcreteType().isBoolean();
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

    public Column getColumn() {
        return column;
    }

    public String getColumnName() {
        return NncUtils.get(column, Column::name);
    }

//    public Object preprocessValue(Object rawValue) {
//        return ValueFormatter.parse(rawValue, type);
//    }

    public String getDisplayValue(Instance value) {
        if(value == null) {
            return "";
        }
        return value.getTitle();
    }

    public String getStrRawDefaultValue() {
        return DefaultValueUtil.convertToStr(defaultValue, getType().getCategory().code(), isArray());
    }

    public String getQualifiedName() {
        return declaringType.getName() + "." + name;
    }

    public FieldPO toPO() {

        return new FieldPO(
                id,
                getTenantId(),
                name,
                declaringType.getId(),
                access.code(),
                isUnique(),
                getStrRawDefaultValue(),
                NncUtils.get(column, Column::name),
                asTitle,
                type.getId()
        );
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
                withType ? type.toDTO() : null,
                isChildField
        );
    }

    public boolean isTime() {
        return getType().isTime();
    }

    @Override
    public String toString() {
        return "Field " + getQualifiedName() + ":" + type.getName();
    }

}
