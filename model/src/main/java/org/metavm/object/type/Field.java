package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.GenericElementDTO;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType
public class Field extends Element implements ChangeAware, GenericElement, Property, PostRemovalAware {

    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String code;
    private final Klass declaringType;
    private Access access;
    private boolean _static;
    private Value defaultValue;
    private boolean lazy;
    private final Column column;
    private boolean isChild;
    @Nullable
    private Expression initializer;
    @Nullable
    @CopyIgnore
    private Field copySource;
    private boolean readonly;
    private MetadataState state;
    private Type type;
    private int originalTag = -1;
    private int tag;
    private final int since;
    private final @Nullable Integer sourceCodeTag;
    private transient int offset;

    public Field(
            Long tmpId,
            String name,
            @Nullable String code,
            Klass declaringType,
            @NotNull Type type,
            Access access,
            boolean readonly,
            Boolean unique,
            Value defaultValue,
            boolean isChild,
            boolean isStatic,
            boolean lazy,
            Value staticValue,
            @Nullable Column column,
            int tag,
            @Nullable Integer sourceCodeTag,
            int since,
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
        this.type = type;
        this.tag = tag;
        this.sourceCodeTag = sourceCodeTag;
        this.readonly = readonly;
        this.since = since;
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
        declaringType.addField(this);
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
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
        if (update.type() != null && !Objects.equals(getType().toExpression(), update.type()))
            throw BusinessException.invalidField(this, "Can not change field type");
        setName(update.name());
        setCode(update.code());
        setAccess(Access.getByCode(update.access()));
        setUnique(update.unique());
    }

    public Field getEffectiveTemplate() {
        return declaringType.isParameterized() ? Objects.requireNonNull(copySource) : this;
    }

    public void setDefaultValue(Value defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setUnique(boolean unique) {
        if (unique && isArray()) {
            throw BusinessException.invalidField(this, "Array fields can not be unique");
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
        declaringType.resetFieldTransients();
        return cascades;
    }

    @Override
    public void postRemove(IEntityContext context) {
        if(isStatic()) {
            StaticFieldTable.getInstance(declaringType, context).remove(this);
        }
    }

    public LongValue getLong(@NotNull ClassInstance instance) {
            return NncUtils.requireNonNull(instance).getLongField(this);
    }

    public DoubleValue getDouble(@NotNull ClassInstance instance) {
        return NncUtils.requireNonNull(instance).getDoubleField(this);
    }

    public StringValue getString(@NotNull ClassInstance instance) {
        return NncUtils.requireNonNull((instance)).getStringField(this);
    }

    public Value get(@NotNull ClassInstance instance) {
        return NncUtils.requireNonNull((instance)).getField(this);
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

    public Value getDefaultValue() {
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

    public String getDisplayValue(Value value) {
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
                    serContext.getStringId(this),
                    getName(),
                    getCode(),
                    getAccess().code(),
                    defaultValue.toFieldValueDTO(),
                    isUnique(),
                    declaringType.getStringId(),
                    type.toExpression(serContext),
                    isChild,
                    isStatic(),
                    readonly,
                    lazy,
                    sourceCodeTag,
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
//        if (_static) {
//            var staticValueField = ModelDefRegistry.getField(Field.class, "staticValue");
//            var value = instance.getField(staticValueField);
//            if (!getType().isInstance(value)) {
//                throw new BusinessException(ErrorCode.STATIC_FIELD_CAN_NOT_BE_NULL, getQualifiedName());
//            }
//        }
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
        return getCodeNotNull();
    }

    public GenericElementDTO toGenericElementDTO(SerializeContext serializeContext) {
        return new GenericElementDTO(
                serializeContext.getStringId(Objects.requireNonNull(getCopySource())),
                serializeContext.getStringId(this)
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

    public void setTag(int tag) {
        originalTag = this.tag;
        this.tag = tag;
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

    public FieldRef getRef() {
        return new FieldRef(declaringType.getType(), this.getEffectiveTemplate());
    }

    public boolean isTagNotNull() {
        return getEffectiveTemplate().isIdNotNull();
    }

    public Id getTagId() {
        return getEffectiveTemplate().getId();
    }

    @Override
    public Field getUltimateTemplate() {
        return getEffectiveTemplate();
    }

    public String getStringTag() {
        return getTagId().toString();
    }

    public long getKlassTag() {
        return declaringType.getTag();
    }

    public int getTag() {
        return tag;
    }

    public @Nullable Integer getSourceCodeTag() {
        return sourceCodeTag;
    }

    public int getOriginalTag() {
        return originalTag;
    }

    public void clearOriginalTag() {
        originalTag = -1;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setMetadataRemoved() {
        this.state = MetadataState.REMOVED;
    }

    public boolean isMetadataRemoved() {
        return state == MetadataState.REMOVED;
    }

    public int getSince() {
        return since;
    }
}
