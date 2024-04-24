package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.GenericElementDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("字段")
public class Field extends Element implements ChangeAware, GenericElement, Property {

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("所属类型")
    private final Klass declaringType;
    @EntityField("可见范围")
    private Access access;
    @EntityField("是否静态")
    private boolean _static;
    @EntityField("默认值")
    private Instance defaultValue;
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
    @EntityField("模板")
    @Nullable
    @CopyIgnore
    private Field copySource;
    @EntityField("只读")
    private boolean readonly;
    @EntityField("状态")
    private MetadataState state;
    @EntityField("类型")
    private Type type;

    public Field(
            Long tmpId,
            String name,
            @Nullable String code,
            Klass declaringType,
            Type type,
            Access access,
            boolean readonly,
            Boolean unique,
            Instance defaultValue,
            boolean isChild,
            boolean isStatic,
            boolean lazy,
            Instance staticValue,
            @Nullable Column column,
            MetadataState state
    ) {
        super(tmpId);
        if(isChild && type.isPrimitive())
            throw new BusinessException(ErrorCode.CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED);
        this.name = NamingUtils.ensureValidName(name);
        this.code = NamingUtils.ensureValidCode(code);
        this.declaringType = Objects.requireNonNull(declaringType);
        this._static = isStatic;
        this.access = access;
        this.state = state;
        this.type = Objects.requireNonNull(type, () -> "type is missing for field: " + declaringType.getTypeDesc() + "." + name);
        this.readonly = readonly;
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
//        this.template = template;
        declaringType.addField(this);
    }

    public boolean isChild() {
        return isChild;
    }

    public boolean isTitle() {
        return declaringType.getTitleField() == this;
    }

    public TypeCategory getConcreteTypeCategory() {
        return getConcreteType().getCategory();
    }

    public Type getConcreteType() {
        return getType().getConcreteType();
    }

    public void update(FieldDTO update) {
        if (update.typeId() != null && !Objects.equals(getType().getStringId(), update.typeId()))
            throw BusinessException.invalidField(this, "类型不支持修改");
        setName(update.name());
        setCode(update.code());
        setAccess(Access.getByCode(update.access()));
        setUnique(update.unique());
    }

    public Field getEffectiveTemplate() {
        return getCopySource() != null ? getCopySource() : this;
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
        List<Object> cascades = new ArrayList<>(super.beforeRemove(context));
        List<Index> fieldIndices = declaringType.getFieldIndices(this);
        for (Index fieldIndex : fieldIndices) {
            declaringType.removeConstraint(fieldIndex);
            cascades.add(fieldIndex);
        }
        if (declaringType.isEnumConstantField(this))
            cascades.add(staticValue);
        declaringType.resetFieldsMemoryDataStructures();
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
        if (declaringType.isDeployed()) {
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
        try (var serContext = SerializeContext.enter()) {
            return new FieldDTO(
                    serContext.getId(this),
                    getName(),
                    getCode(),
                    getAccess().code(),
                    defaultValue.toFieldValueDTO(),
                    isUnique(),
                    declaringType.getStringId(),
                    serContext.getId(getType()),
                    isChild,
                    isStatic(),
                    readonly,
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
    public Field getCopySource() {
        return this.copySource;
    }

    @Override
    public void setCopySource(@Nullable Object copySource) {
        this.copySource = (Field) copySource;
    }

    @Override
    public void onChange(ClassInstance instance, IEntityContext context) {
        if (_static) {
            var staticValueField = ModelDefRegistry.getField(Field.class, "staticValue");
            var value = instance.getField(staticValueField);
            if (!getType().isInstance(value)) {
                throw new BusinessException(ErrorCode.STATIC_FIELD_CAN_NOT_BE_NULL, getQualifiedName());
            }
        }
    }

    @Override
    public boolean isChangeAware() {
        return _static;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitField(this);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getCodeRequired();
    }

    public GenericElementDTO toGenericElementDTO(SerializeContext serializeContext) {
        return new GenericElementDTO(
                serializeContext.getId(Objects.requireNonNull(getCopySource())),
                serializeContext.getId(this)
        );
    }

    @Override
    public Klass getDeclaringType() {
        return declaringType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = NamingUtils.ensureValidName(name);
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public void setAccess(Access access) {
        this.access = access;
    }

    @Nullable
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(@Nullable String code) {
        this.code = NamingUtils.ensureValidCode(code);
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isStatic() {
        return _static;
    }

    @Override
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    @Override
    public MetadataState getState() {
        return state;
    }

    @Override
    public void setState(MetadataState state) {
        this.state = state;
    }
}
