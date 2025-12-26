package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.*;
import org.metavm.flow.CodeWriter;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;
import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(72)
@Entity
public class Field extends AttributedElement implements Property, ITypeDef {

    private @Nullable Integer sourceTag;
    @EntityField(asTitle = true)
    private String name;
    @Setter
    @Parent
    private Klass declaringType;
    private Access access;
    private boolean _static;
    @Getter
    @Setter
    private Value defaultValue;
    @Setter
    @Getter
    private boolean lazy;
    @Getter
    private Column column;
    @Setter
    @Getter
    private boolean readonly;
    private boolean isTransient;
    private MetadataState state;
    @Getter
    private int typeIndex;
    @Getter
    private Type type;
    private final int originalTag = -1;
    @Getter
    @Setter
    private int tag;
    @Setter
    @Getter
    private int since;
    @Setter
    @Getter
    public transient int offset;
    private @Nullable Reference initializerReference;
    private boolean isEnumConstant;
    @Setter
    @Getter
    private int ordinal;

    public Field(
            @NotNull Id id,
            String name,
            Klass declaringType,
            int typeIndex,
            Access access,
            boolean readonly,
            boolean isTransient,
            Value defaultValue,
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
        super(id);
        this.name = NamingUtils.ensureValidName(name);
        this.declaringType = Objects.requireNonNull(declaringType);
        this._static = isStatic;
        this.access = access;
        this.state = state;
        this.type = typeIndex == -1 ? Types.getAnyType() : declaringType.getConstantPool().getType(typeIndex);
        this.typeIndex = typeIndex;
        this.tag = tag;
        this.sourceTag = sourceTag;
        this.readonly = readonly;
        this.isTransient = isTransient;
        this.since = since;
        this.initializerReference = Utils.safeCall(initializer, Instance::getReference);
        this.isEnumConstant = isEnumConstant;
        this.ordinal = ordinal;
        if (column != null) {
            Utils.require(declaringType.checkColumnAvailable(column));
            this.column = column;
        } else
            this.column = allocteColumn();
        setDefaultValue(defaultValue);
        this.lazy = lazy;
        declaringType.addField(this);
    }

    public boolean isTitle() {
        return declaringType.getTitleField() == this;
    }

    @JsonIgnore
    public Type getConcreteType() {
        return getType().getConcreteType();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return declaringType;
    }

    public LongValue getLong(@NotNull ClassInstance instance) {
            return Objects.requireNonNull(instance).getLongField(this);
    }

    public DoubleValue getDouble(@NotNull ClassInstance instance) {
        return Objects.requireNonNull(instance).getDoubleField(this);
    }

    public Value get(@NotNull ClassInstance instance) {
        return Objects.requireNonNull((instance)).getField(this);
    }

    public boolean isEnum() {
        return getType().isEnum();
    }

    public boolean isArray() {
        return getType().isArray();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @JsonIgnore
    public boolean isNullable() {
        return !isNotNull();
    }

    @JsonIgnore
    public boolean isSingleValued() {
        return !isArray();
    }

    @JsonIgnore
    public boolean isInt64() {
        return getConcreteType().isLong();
    }

    @JsonIgnore
    public boolean isNumber() {
        return getConcreteType().isNumber();
    }

    @JsonIgnore
    public boolean isBool() {
        return getConcreteType().isBoolean();
    }

    @JsonIgnore
    public boolean isString() {
        return getConcreteType().isString();
    }

    @JsonIgnore
    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    @JsonIgnore
    public boolean isNotNull() {
        return getType().isNotNull();
    }

    @JsonIgnore
    public String getColumnName() {
        return Utils.safeCall(column, Column::name);
    }

    @JsonIgnore
    public String getEsField() {
        return "l" + getDeclaringType().getLevel() + "." + getColumnName();
    }

//    public Object preprocessValue(Object rawValue) {
//        return ValueFormatter.parse(rawValue, type);
//    }

    @JsonIgnore
    public String getDisplayValue(Value value) {
        if (value == null) {
            return "";
        }
        return value.getTitle();
    }

    @JsonIgnore
    @Override
    public String getQualifiedName() {
        return declaringType.getName() + "." + getName();
    }

    @JsonIgnore
    public boolean isTime() {
        return getType().isTime();
    }

    @Override
    public String toString() {
        return "Field " + getDesc();
    }

    private String getDesc() {
        return getQualifiedName() + ":" + getType().getName();
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
        if (column == Column.NIL)
            this.column = allocteColumn();
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
        this.type = declaringType.getConstantPool().getType(typeIndex);
        if (column == Column.NIL)
            this.column = allocteColumn();
    }

    private Column allocteColumn() {
        return Objects.requireNonNull(declaringType.allocateColumn(this),
                () -> "Fail to allocate a column for field " + name);
    }

    public void resetTypeIndex() {
        typeIndex = declaringType.addConstant(type);
    }

    public void initTag(int tag) {
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

    public @Nullable Integer getSourceTag() {
        return sourceTag;
    }

    public void setSourceTag(@Nullable Integer sourceTag) {
        this.sourceTag = sourceTag;
    }

    public int getOriginalTag() {
        return tag;
    }

    public void setMetadataRemoved() {
        this.state = MetadataState.REMOVED;
    }

    @JsonIgnore
    public boolean isMetadataRemoved() {
        return state == MetadataState.REMOVED;
    }

    public int getFlags() {
        int flags = 0;
        if (_static) flags |= FieldFlags.FLAG_STATIC;
//        if(readonly)
//            flags |= FLAG_READONLY;
        if (isTransient) flags |= FieldFlags.FLAG_TRANSIENT;
        if (lazy) flags |= FieldFlags.FLAG_LAZY;
        if (isEnumConstant) flags |= FieldFlags.FLAG_ENUM_CONSTANT;
        return flags;
    }

    public void setFlags(int flags) {
        setStatic((flags & FieldFlags.FLAG_STATIC) != 0);
        setTransient((flags & FieldFlags.FLAG_TRANSIENT) != 0);
//        setReadonly((flags & FLAG_READONLY) != 0);
        setLazy((flags & FieldFlags.FLAG_LAZY) != 0);
        setEnumConstant((flags & FieldFlags.FLAG_ENUM_CONSTANT) != 0);
    }

    @Nullable
    public Method getInitializer() {
        return initializerReference != null ? (Method) initializerReference.get() : null;
    }

    @Nullable
    public Reference getInitializerReference() {
        return initializerReference;
    }

    public void setInitializer(@Nullable Method initializer) {
        this.initializerReference = Utils.safeCall(initializer, Instance::getReference);
    }

    public void setInitializerReference(@Nullable Reference initializerReference) {
        this.initializerReference = initializerReference;
    }

    public void initialize(@Nullable ClassInstance self, IInstanceContext context) {
        if (initializerReference != null) {
            var initializer = (Method) initializerReference.get();
            if (isStatic()) {
                var value = getType().fromStackValue(
                        Flows.invoke(initializer.getRef(), null, List.of(), context)
                );
                StaticFieldTable.getInstance(getDeclaringType().getType(), context).set(this, value);
            }
            else {
                Objects.requireNonNull(self);
                var value = getType(self.getInstanceType().getTypeMetadata()).fromStackValue(
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

    public Value getStatic(IInstanceContext context) {
        assert _static;
        return  StaticFieldTable.getInstance(declaringType.getType(), context).get(this);
    }

    public void updateEnumConstant(IInstanceContext context) {
        assert isEnumConstant;
        var value = getStatic(context).resolveObject();
        value.setFieldForce(StdField.enumName.get(), Instances.stringInstance(name));
        value.setFieldForce(StdField.enumOrdinal.get(), Instances.intInstance(ordinal));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitField(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        type.accept(visitor);
    }

    public void writeCode(CodeWriter writer) {
        var modifiers = new ArrayList<String>();
        if (isMetadataRemoved()) modifiers.add("<removed>");
        if (access != Access.PACKAGE) modifiers.add(access.name().toLowerCase());
        if (isStatic()) modifiers.add("static");
        if (isReadonly()) modifiers.add("readonly");
        if (isTransient) modifiers.add("transient");
        writer.writeln(String.join(" ", modifiers) + " var " + name + ": " + getType().getTypeDesc());
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (defaultValue instanceof Reference r) action.accept(r);
        else if (defaultValue instanceof NativeValue t) t.forEachReference(action);
        column.forEachReference(action);
        type.forEachReference(action);
        if (initializerReference != null) action.accept(initializerReference);
    }

}
