package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.*;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("字段")
public class Field extends Entity {

    public static final IndexDef<Field> INDEX_TYPE_ID = new IndexDef<>(Field.class, false,"type");

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField("可见范围")
    private Access access;
    @EntityField("默认值")
    private Instance defaultValue;
    @EntityField("是否作为标题")
    private boolean asTitle;
    @EntityField("列")
    private final Column column;
    @EntityField("类型")
    private final Type type;
    @EntityField("是否从对象字段")
    private final boolean isChildField;
//    @EntityField("状态")
//    private MetadataState state;

    public Field(String name, ClassType declaringType, Type type) {
        this(name, declaringType, Access.GLOBAL, false, false,
                InstanceUtils.nullInstance(), type, false);
    }

    public Field(
             String name,
             ClassType declaringType,
             Access access,
             Boolean unique,
             boolean asTitle,
             Instance defaultValue,
             Type type,
             boolean isChildField
    ) {
        this.declaringType = requireNonNull(declaringType, "属性所属类型");
        this.access = requireNonNull(access, "属性访问控制");
        this.type = type;
        this.asTitle = asTitle;
        setName(name);
        this.column = NncUtils.requireNonNull(declaringType.allocateColumn(this),
                "Fail to allocate a column for indexItem " + this);
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
        setUnique(update.unique());
    }

//    public void setState(MetadataState state) {
//        this.state = state;
//    }
//
//    public MetadataState getState() {
//        return state;
//    }

    public boolean isReady() {
        return true;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public void setDefaultValue(Instance defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setUnique(boolean unique) {
        if(unique && isArray()) {
            throw BusinessException.invalidField(this, "数组不支持唯一性约束");
        }
        Index constraint = declaringType.getUniqueConstraint(List.of(this));
        if(constraint != null && !unique) {
            declaringType.removeConstraint(constraint);
        }
        if(constraint == null && unique) {
            ConstraintFactory.newUniqueConstraint(List.of(this));
        }
    }

    public List<Object> beforeRemove() {
        List<Index> fieldIndices = declaringType.getFieldIndices(this);
        List<Object> cascades = new ArrayList<>();
        for (Index fieldIndex : fieldIndices) {
            declaringType.removeConstraint(fieldIndex);
            cascades.add(fieldIndex);
        }
        declaringType.removeField(this);
        return cascades;
    }

    @Override
    public void onBind(IEntityContext context) {
        if(isNullable() || getDefaultValue().isNotNull()) {
            return;
        }
        if(context.getInstanceContext().existsInstances(declaringType)) {
            throw BusinessException.notNullFieldWithoutDefaultValue(this);
        }
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

    public Instance getDefaultValue() {
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
                defaultValue.toFieldValueDTO(),
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
        return "Field " + declaringType.getName() + "." + getName() + ":" + type.getName();
    }

}
