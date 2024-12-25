package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.flow.Flows;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Field extends Element implements ChangeAware, Property, ITypeDef {

    @EntityField(asTitle = true)
    private String name;
    private Klass declaringType;
    private Access access;
    private boolean _static;
    private Value defaultValue;
    private boolean lazy;
    private Column column;
    private boolean isChild;
    private boolean readonly;
    private boolean isTransient;
    private MetadataState state;
    private int typeIndex;
    private Type type;
    private int originalTag = -1;
    private int tag;
    private int since;
    private @Nullable Integer sourceTag;
    public transient int offset;
    private @Nullable Method initializer;
    private boolean isEnumConstant;
    private int ordinal;

    public Field(
            Long tmpId,
            String name,
            Klass declaringType,
            @NotNull Type type,
            Access access,
            boolean readonly,
            boolean isTransient,
            Boolean unique,
            Value defaultValue,
            boolean isChild,
            boolean isStatic,
            boolean lazy,
            boolean isEnumConstant,
            int ordinal,
            @Nullable Column column,
            int tag,
            @Nullable Integer sourceTag,
            int since,
            @Nullable Method initializer,
            MetadataState state
    ) {
        super(tmpId);
        if(isChild && type.isPrimitive())
            throw new BusinessException(ErrorCode.CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED);
        this.name = NamingUtils.ensureValidName(name);
        this.declaringType = Objects.requireNonNull(declaringType);
        this._static = isStatic;
        this.access = access;
        this.state = state;
        this.type = type;
        this.typeIndex = declaringType.addConstant(type);
        this.tag = tag;
        this.sourceTag = sourceTag;
        this.readonly = readonly;
        this.isTransient = isTransient;
        this.since = since;
        this.initializer = initializer;
        this.isEnumConstant = isEnumConstant;
        this.ordinal = ordinal;
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

    public Type getConcreteType() {
        return getType().getConcreteType();
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
        Index constraint = declaringType.findSelfUniqueConstraint(List.of(this));
        if (constraint != null && !unique) {
            declaringType.removeConstraint(constraint);
        }
        if (constraint == null && unique) {
            ConstraintFactory.newUniqueConstraint(getName(), List.of(this));
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
        if(isStatic()) {
            var sft = context.selectFirstByKey(StaticFieldTable.IDX_KLASS, declaringType);
            if(sft != null)
                sft.remove(this);
        }
        declaringType.resetFieldTransients();
        return cascades;
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

    public Type getType() {
        return type;
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
        return getConcreteType().isNumber();
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
        return declaringType.findSelfUniqueConstraint(List.of(this)) != null;
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

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean aTransient) {
        isTransient = aTransient;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
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

    @Override
    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public void setType(Type type) {
        this.type = type;
        resetTypeIndex();
    }

    public void resetTypeIndex() {
        typeIndex = declaringType.addConstant(type);
    }

    public void initTag(int tag) {
        this.tag = tag;
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
        return new FieldRef(declaringType.getType(), this);
    }

    public long getKlassTag() {
        return declaringType.getTag();
    }

    public int getTag() {
        return tag;
    }

    public @Nullable Integer getSourceTag() {
        return sourceTag;
    }

    public void setSourceTag(@Nullable Integer sourceTag) {
        this.sourceTag = sourceTag;
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

    public void setDeclaringType(Klass declaringType) {
        this.declaringType = declaringType;
    }

    public static final int FLAG_STATIC = 1;
    public static final int FLAG_CHILD = 2;
    public static final int FLAG_READONLY = 4;
    public static final int FLAG_TRANSIENT = 8;
    public static final int FLAG_LAZY = 16;
    private static final int FLAG_ENUM_CONSTANT = 32;

    public int getFlags() {
        int flags = 0;
        if(_static)
            flags |= FLAG_STATIC;
        if(isChild)
            flags |= FLAG_CHILD;
//        if(readonly)
//            flags |= FLAG_READONLY;
        if(isTransient)
            flags |= FLAG_TRANSIENT;
        if(lazy)
            flags |= FLAG_LAZY;
        if (isEnumConstant)
            flags |= FLAG_ENUM_CONSTANT;
        return flags;
    }

    public void setFlags(int flags) {
        setChild((flags & FLAG_CHILD) != 0);
        setStatic((flags & FLAG_STATIC) != 0);
        setTransient((flags & FLAG_TRANSIENT) != 0);
//        setReadonly((flags & FLAG_READONLY) != 0);
        setLazy((flags & FLAG_LAZY) != 0);
        setEnumConstant((flags & FLAG_ENUM_CONSTANT) != 0);
    }

    public void write(MvOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        output.write(access.code());
        output.writeShort(typeIndex);
        output.writeInt(getFlags());
        output.write(state.code());
        output.writeInt(tag);
        output.writeInt(sourceTag != null ? sourceTag : -1);
        output.writeInt(since);
        column.write(output);
        defaultValue.write(output);
        output.writeInt(ordinal);
        if (initializer != null)
            output.writeEntityId(initializer);
        else
            output.writeId(new NullId());
    }

    public void read(KlassInput input) {
        name = input.readUTF();
        access = Access.fromCode(input.read());
        typeIndex = input.readShort();
        type = declaringType.getConstantPool().getType(typeIndex);
        setFlags(input.readInt());
        state = MetadataState.fromCode(input.read());
        tag = input.readInt();
        sourceTag = input.readInt();
        if(sourceTag == -1)
            sourceTag = null;
        since = input.readInt();
        column = input.readColumn();
        defaultValue = input.readValue();
        ordinal = input.readInt();
        var initializerId = input.readId();
        initializer = initializerId instanceof NullId ? null : input.getMethod(initializerId);
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    @Nullable
    public Method getInitializer() {
        return initializer;
    }

    public void setInitializer(@Nullable Method initializer) {
        this.initializer = initializer;
    }

    public void initialize(@Nullable ClassInstance self,  IEntityContext context) {
        if (initializer != null) {
            if (isStatic()) {
                var value = getType().fromStackValue(
                        Flows.invoke(initializer.getRef(), null, List.of(), context)
                );
                StaticFieldTable.getInstance(getDeclaringType().getType(), context).set(this, value);
            }
            else {
                Objects.requireNonNull(self);
                var value = getType(self.getType().getTypeMetadata()).fromStackValue(
                        Flows.invoke(initializer.getRef(), self, List.of(), context)
                );
                self.setField(this, value);
            }
        }
    }

    public boolean isEnumConstant() {
        return isEnumConstant;
    }

    public void setEnumConstant(boolean enumConstant) {
        isEnumConstant = enumConstant;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Value getStatic(IEntityContext context) {
        assert _static;
        return  StaticFieldTable.getInstance(declaringType.getType(), context).get(this);
    }

    public void updateEnumConstant(IEntityContext context) {
        assert isEnumConstant;
        var value = getStatic(context).resolveObject();
        value.setField(StdField.enumName.get(), Instances.stringInstance(name));
        value.setField(StdField.enumOrdinal.get(), Instances.intInstance(ordinal));
    }
}
