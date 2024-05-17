package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType("类型")
public abstract class Type extends ValueElement {

    public boolean isViewType(Type type) {
        return this.equals(type);
    }

    public abstract String getName();

    public String getTypeDesc() {
        return getName();
    }

    @Nullable
    public abstract String getCode();

    public abstract TypeCategory getCategory();

    public abstract boolean isEphemeral();

    public TypeKey toTypeKey() {
       return toTypeKey(TypeDef::getStringId);
    }

    public abstract TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId);

    public void forEachTypeDef(Consumer<TypeDef> action) {}

    @SuppressWarnings("unused")
    public boolean isDurable() {
        return !isEphemeral();
    }

    public boolean isCaptured() {
        return false;
    }

    public Set<CapturedType> getCapturedTypes() {
        var capturedTypes = new HashSet<CapturedType>();
        getCapturedTypes(capturedTypes);
        return capturedTypes;
    }

    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
    }

    public Type getCertainUpperBound() {
        if (!(this instanceof UncertainType)) {
            return this;
        }
        var current = getUpperBound();
        Set<Type> visited = new IdentitySet<>();
        while (current instanceof UncertainType) {
            if (visited.contains(current))
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            visited.add(current);
            current = current.getUpperBound();
        }
        return current;
    }

    public boolean isNullable() {
        return isAssignableFrom(StandardTypes.getNullType());
    }

    public boolean isBinaryNullable() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public Type getConcreteType() {
        return this;
    }

    protected abstract boolean isAssignableFrom0(Type that);

    public abstract  <R, S> R accept(TypeVisitor<R, S> visitor, S s);

    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
    }

    public Type transform(TypeTransformer<?> transformer) {
        return transformer.visitType(this, null);
    }

    public List<? extends Type> getSuperTypes() {
        return List.of();
    }

    @Override
    public final boolean afterContextInitIds() {
        return afterContextInitIdsInternal();
    }

    protected boolean afterContextInitIdsInternal() {
        return false;
    }

    public boolean isAssignableFrom(Type that) {
        if (that instanceof NeverType)
            return true;
        return that instanceof IVariableType && isAssignableFrom1(that) || isAssignableFrom1(that.getUpperBound());
    }

    private boolean isAssignableFrom1(Type that) {
        return switch (that) {
            case UnionType unionType -> NncUtils.allMatch(unionType.getMembers(), this::isAssignableFrom);
            case IntersectionType intersectionType ->
                    NncUtils.anyMatch(intersectionType.getTypes(), this::isAssignableFrom);
            default -> isAssignableFrom0(that);
        };
    }

    public boolean isUncertain() {
        return false;
    }

    public boolean contains(Type that) {
        return equals(that);
    }

    public boolean isVariable() {
        return false;
    }

    public Set<TypeVariable> getVariables() {
        return Set.of();
    }

    public Type getLowerBound() {
        return this;
    }

    public Type getUpperBound() {
        return this;
    }

    public boolean isInstance(Instance value) {
        return isAssignableFrom(value.getType());
    }

    @NoProxy
    public boolean isLong() {
        return false;
    }

    @NoProxy
    public boolean isNumber() {
        return false;
    }

    @NoProxy
    public boolean isDouble() {
        return false;
    }

    @NoProxy
    public boolean isBoolean() {
        return false;
    }

    @NoProxy
    public boolean isString() {
        return false;
    }

    @NoProxy
    public boolean isTime() {
        return false;
    }

    @NoProxy
    public boolean isPassword() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    @NoProxy
    public boolean isArray() {
        return false;
    }

    @NoProxy
    public boolean isPrimitive() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isPojo() {
        return false;
    }

    @NoProxy
    public boolean isVoid() {
        return false;
    }

    public boolean isReference() {
        return isArray() || isPojo() || (isBinaryNullable() && getUnderlyingType().isReference());
    }

    public Type getUnderlyingType() {
        return this;
    }

    public ColumnKind getSQLType() {
        return getCategory().getSQLType();
    }

    protected TypeDTO toDTO(TypeParam param) {
        try (var ser = SerializeContext.enter()) {
            return new TypeDTO(
                    ser.getId(this),
                    getName(),
                    getCode(),
                    getCategory().code(),
                    isEphemeral(),
                    false,
                    param
            );
        }
    }

    @Override
    protected String toString0() {
        return getTypeDesc();
    }

    public abstract String getInternalName(@org.jetbrains.annotations.Nullable Flow current);

    public TypeTag getTag() {
        return TypeTag.fromCategory(getCategory());
    }

    public TypeId getTypeId() {
        throw new UnsupportedOperationException();
    }

    public abstract Type copy();

    public String toExpression () {
        try(var serContext = SerializeContext.enter()) {
            return toExpression(serContext, null);
        }
    }

    public String toExpression(SerializeContext serializeContext) {
        return toExpression(serializeContext, null);
    }

    public String toExpression(@Nullable Function<TypeDef, String> getTypeDefExpr) {
        try(var serCtx = SerializeContext.enter()) {
            return toExpression(serCtx, getTypeDefExpr);
        }
    }

    public abstract String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr);

    public  void write(InstanceOutput output) {
        output.write(getCategory().code());
        write0(output);
    }

    public abstract int getTypeKeyCode();

    public abstract void write0(InstanceOutput output);

    public int getTypeTag() {
        return TypeTags.DEFAULT;
    }

    public static Type readType(InstanceInput input, TypeDefProvider typeDefProvider) {
        var category = TypeCategory.fromCode(input.read());
        return switch (category) {
            case CLASS, INTERFACE, ENUM, VALUE -> ClassType.read(input, typeDefProvider);
            case VARIABLE -> VariableType.read(input, typeDefProvider);
            case CAPTURED -> CapturedType.read(input, typeDefProvider);
            case LONG, DOUBLE, NULL, VOID, TIME, PASSWORD, STRING, BOOLEAN -> PrimitiveType.read(input);
            case FUNCTION -> FunctionType.read(input, typeDefProvider);
            case UNCERTAIN -> UncertainType.read(input, typeDefProvider);
            case UNION ->  UnionType.read(input, typeDefProvider);
            case INTERSECTION -> IntersectionType.read(input, typeDefProvider);
            case READ_ONLY_ARRAY -> ArrayType.read(input, ArrayKind.READ_ONLY, typeDefProvider);
            case READ_WRITE_ARRAY -> ArrayType.read(input, ArrayKind.READ_WRITE, typeDefProvider);
            case CHILD_ARRAY -> ArrayType.read(input, ArrayKind.CHILD, typeDefProvider);
            case NEVER -> new NeverType();
            case ANY -> new AnyType();
        };
    }

}
