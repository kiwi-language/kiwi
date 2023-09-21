package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("字段")
public class Field extends Entity implements UpdateAware {

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
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField(value = "是否静态", code = "static")
    private boolean _static;
    @EntityField(value = "静态属性值", code = "static")
    private Instance staticValue;
    @Nullable
    private Expression initializer;

//    @EntityField("状态")
//    private MetadataState state;

    public Field(
            String name,
            @Nullable String code,
            ClassType declaringType,
            Type type,
            Access access,
            Boolean unique,
            boolean asTitle,
            Instance defaultValue,
            boolean isChildField,
            boolean isStatic,
            Instance staticValue
    ) {
        setName(name);
        this.code = code;
        this.declaringType = requireNonNull(declaringType, "属性所属类型");
        this.access = requireNonNull(access, "属性访问控制");
        this.type = type;
        this.asTitle = asTitle;
        this.column = NncUtils.requireNonNull(declaringType.allocateColumn(this),
                "Fail to allocate a column for field " + this);
        setDefaultValue(defaultValue);
        this.isChildField = isChildField;
        if(unique != null) {
            setUnique(unique);
        }
        this._static = isStatic;
        this.staticValue = staticValue;
        declaringType.addField(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public void setCode(@org.jetbrains.annotations.Nullable String code) {
        this.code = code;
    }

    @Nullable
    public String getCode() {
        return code;
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
            throw BusinessException.invalidField(this, "类型不支持修改");
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

    public Instance getStaticValue() {
        if(isStatic()) return staticValue;
        else throw new InternalException("Can not get static value from an instance field");
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
        if(type.isAnonymous()) {
            cascades.add(type);
        }
        if(declaringType.isEnumConstantField(this)) {
            cascades.add(staticValue);
        }
        declaringType.removeField(this);
        return cascades;
    }

    public LongInstance getLong(@Nullable ClassInstance instance) {
        if(isStatic()) {
            return (LongInstance) getStaticValue();
        }
        else {
            return NncUtils.requireNonNull(instance).getLong(this);
        }
    }

    public DoubleInstance getDouble(@Nullable ClassInstance instance) {
        if(isStatic()) {
            return (DoubleInstance) getStaticValue();
        }
        else {
            return NncUtils.requireNonNull(instance).getDouble(this);
        }
    }

    public StringInstance getString(@Nullable ClassInstance instance) {
        if(isStatic()) {
            return (StringInstance) getStaticValue();
        }
        else {
            return NncUtils.requireNonNull((instance)).getString(this);
        }
    }

    public Instance get(@Nullable ClassInstance instance) {
        if(isStatic()) {
            return getStaticValue();
        }
        else {
            return NncUtils.requireNonNull((instance)).get(this);
        }
    }

    public boolean isStatic() {
        return _static;
    }

    public void setStatic(boolean _static) {
        this._static = _static;
    }

    public void setStaticValue(Instance staticValue) {
        this.staticValue = staticValue;
    }

    @Nullable
    public Expression getInitializer() {
        return initializer;
    }

    public void setInitializer(@Nullable Expression initializer) {
        this.initializer = initializer;
    }

    @Override
    public void onBind(IEntityContext context) {
        if(isNullable() || getDefaultValue().isNotNull()) {
            return;
        }
        if(Objects.requireNonNull(context.getInstanceContext()).existsInstances(declaringType)) {
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
        try(var context = SerializeContext.enter()) {
            return new FieldDTO(
                    context.getTmpId(this),
                    id,
                    name,
                    code,
                    access.code(),
                    defaultValue.toFieldValueDTO(),
                    isUnique(),
                    asTitle,
                    declaringType.getId(),
                    context.getRef(type),
                    withType ? type.toDTO() : null,
                    isChildField,
                    _static
            );
        }
    }

    public boolean isTime() {
        return getType().isTime();
    }

    @Override
    public String toString() {
        return "Field " + getDesc();
    }

    private String getDesc() {
        return getQualifiedName() + ":" + type.getName();
    }

    @Override
    public void onUpdate(ClassInstance instance) {
        if(isStatic()) {
            var staticValueField = ModelDefRegistry.getField(Field.class, "staticValue");
            var value = instance.get(staticValueField);
            if(!type.isInstance(value)) {
                throw new BusinessException(ErrorCode.STATIC_FIELD__CAN_NOT_BE_NULL, getQualifiedName());
            }
        }
    }

}
