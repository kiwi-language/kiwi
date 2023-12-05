package tech.metavm.object.type;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("字段")
public class Field extends Property implements UpdateAware, GlobalKey {

    @EntityField("可见范围")
    private Access access;
    @EntityField("默认值")
    private Instance defaultValue;
    @EntityField("是否作为标题")
    private boolean asTitle;
    @EntityField("懒加载")
    private boolean lazy;
    @EntityField("列")
    private final Column column;
    @EntityField("是否子对象字段")
    private final boolean isChild;
    @EntityField(value = "静态属性值", code = "static")
    private Instance staticValue;
    @Nullable
    private Expression initializer;
    @Nullable
    private final Field template;

    public Field(
            Long tmpId,
            String name,
            @Nullable String code,
            ClassType declaringType,
            Type type,
            Access access,
            Boolean unique,
            boolean asTitle,
            Instance defaultValue,
            boolean isChild,
            boolean isStatic,
            boolean lazy,
            Instance staticValue,
            @Nullable Field template,
            @Nullable Column column,
            MetadataState state
    ) {
        super(tmpId, name, code, type, declaringType, isStatic, state);
        setName(name);
        requireNonNull(type);
        requireNonNull(declaringType, "属性所属类型不能为空");
        this.access = requireNonNull(access, "属性访问控制");
        this.asTitle = asTitle;
        if (column != null) {
            NncUtils.requireTrue(declaringType.checkColumnAvailable(column));
            this.column = column;
        } else {
            this.column = NncUtils.requireNonNull(declaringType.allocateColumn(this),
                    "Fail to allocate a column for field " + this);
        }
        setDefaultValue(defaultValue);
        this.isChild = isChild;
        if (unique != null) {
            setUnique(unique);
        }
        this.lazy = lazy;
        this.staticValue = staticValue;
        this.template = template;
        declaringType.addField(this);
    }

    public Access getAccess() {
        return access;
    }

    public boolean isChild() {
        return isChild;
    }

    public TypeCategory getConcreteTypeCategory() {
        return getConcreteType().getCategory();
    }

    public Type getConcreteType() {
        return getType().getConcreteType();
    }

    public void update(FieldDTO update) {
        if (update.typeId() != null && !Objects.equals(getType().getId(), update.typeId())) {
            throw BusinessException.invalidField(this, "类型不支持修改");
        }
        setName(update.name());
        setAccess(Access.getByCodeRequired(update.access()));
        setUnique(update.unique());
        setAsTitle(update.asTitle());
        setUnique(update.unique());
    }

    public Field getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public void setDefaultValue(Instance defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Instance getStaticValue() {
        if (isStatic()) return staticValue;
        else throw new InternalException("Can not get static value from an instance field");
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setUnique(boolean unique) {
        if (unique && isArray()) {
            throw BusinessException.invalidField(this, "数组不支持唯一性约束");
        }
        Index constraint = declaringType.getUniqueConstraint(List.of(this));
        if (constraint != null && !unique) {
            declaringType.removeConstraint(constraint);
        }
        if (constraint == null && unique) {
            ConstraintFactory.newUniqueConstraint(getName(), getCode(), List.of(this));
        }
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        List<Index> fieldIndices = declaringType.getFieldIndices(this);
        List<Object> cascades = new ArrayList<>();
        for (Index fieldIndex : fieldIndices) {
            declaringType.removeConstraint(fieldIndex);
            cascades.add(fieldIndex);
        }
        if (declaringType.isEnumConstantField(this)) {
            cascades.add(staticValue);
        }
        return cascades;
    }

    public LongInstance getLong(@Nullable ClassInstance instance) {
        if (isStatic()) {
            return (LongInstance) getStaticValue();
        } else {
            return NncUtils.requireNonNull(instance).getLongField(this);
        }
    }

    public DoubleInstance getDouble(@Nullable ClassInstance instance) {
        if (isStatic()) {
            return (DoubleInstance) getStaticValue();
        } else {
            return NncUtils.requireNonNull(instance).getDoubleField(this);
        }
    }

    public StringInstance getString(@Nullable ClassInstance instance) {
        if (isStatic()) {
            return (StringInstance) getStaticValue();
        } else {
            return NncUtils.requireNonNull((instance)).getStringField(this);
        }
    }

    public Instance get(@Nullable ClassInstance instance) {
        if (isStatic()) {
            return getStaticValue();
        } else {
            return NncUtils.requireNonNull((instance)).getField(this);
        }
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
        if (isNullable() || isStatic() || getState() == MetadataState.INITIALIZING
                || isChild() && getType().isArray()) {
            return;
        }
        if (Objects.requireNonNull(context.getInstanceContext()).existsInstances(declaringType, true)) {
            throw BusinessException.notNullFieldWithoutDefaultValue(this);
        }
    }

    public boolean isEnum() {
        return getType().isEnum();
    }

    public boolean isArray() {
        return getType().isArray();
    }

    public boolean isReference() {
        return getType().isReference();
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
        return getType().isPrimitive();
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
        if (asTitle) {
            Field titleField = declaringType.getTileField();
            if (titleField != null && !titleField.equals(this)) {
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
        if (value == null) {
            return "";
        }
        return value.getTitle();
    }

    public String getStrRawDefaultValue() {
        return DefaultValueUtil.convertToStr(defaultValue, getType().getCategory().code(), isArray());
    }

    public String getQualifiedName() {
        return declaringType.getName() + "." + getName();
    }

    public FieldDTO toDTO() {
        try (var context = SerializeContext.enter()) {
            return new FieldDTO(
                    context.getTmpId(this),
                    id,
                    getName(),
                    getCode(),
                    access.code(),
                    defaultValue.toFieldValueDTO(),
                    isUnique(),
                    asTitle,
                    declaringType.getId(),
                    context.getRef(getType()),
                    isChild,
                    isStatic(),
                    lazy,
                    NncUtils.get(staticValue, Instance::toDTO),
                    getState().code()
            );
        }
    }

    public boolean isTime() {
        return getType().isTime();
    }

    @Override
    protected String toString0() {
        return "Field " + getDesc();
    }

    private String getDesc() {
        return getQualifiedName() + ":" + getType().getName();
    }

    @Nullable
    public Field getTemplate() {
        return template;
    }

    @Override
    public void onUpdate(ClassInstance instance) {
        if (isStatic()) {
            var staticValueField = ModelDefRegistry.getField(Field.class, "staticValue");
            var value = instance.getField(staticValueField);
            if (!getType().isInstance(value)) {
                throw new BusinessException(ErrorCode.STATIC_FIELD_CAN_NOT_BE_NULL, getQualifiedName());
            }
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitField(this);
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return getDeclaringType().getKey(getJavaType) + "." + getCodeRequired();
    }
}
