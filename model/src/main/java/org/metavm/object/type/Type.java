package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Writable;
import org.metavm.entity.NoProxy;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueElement;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TypeId;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType
public abstract class Type extends ValueElement implements TypeOrTypeKey, Writable {

    public abstract String getName();

    public String getTypeDesc() {
        return getName();
    }

    public abstract TypeCategory getCategory();

    public abstract boolean isEphemeral();

    public TypeKey toTypeKey() {
        return toTypeKey(ITypeDef::getId);
    }

    public abstract TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId);

    public void forEachTypeDef(Consumer<TypeDef> action) {
    }

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
        return isAssignableFrom(Types.getNullType());
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

    public abstract <R, S> R accept(TypeVisitor<R, S> visitor, S s);

    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
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

    public Type getUpperBound2() {
        return this;
    }

    public boolean isInstance(Value value) {
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

    public String toExpression() {
        try (var serContext = SerializeContext.enter()) {
            return toExpression(serContext, null);
        }
    }

    public String toExpression(SerializeContext serializeContext) {
        return toExpression(serializeContext, null);
    }

    public String toExpression(@Nullable Function<ITypeDef, String> getTypeDefExpr) {
        try (var serCtx = SerializeContext.enter()) {
            return toExpression(serCtx, getTypeDefExpr);
        }
    }

    public abstract String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr);

    public abstract int getTypeKeyCode();

    public abstract void write(MvOutput output);

    public int getTypeTag() {
        return TypeTags.DEFAULT;
    }

    public static Type readType(MvInput input) {
        return readType(input.read(), input);
    }

    public static Type readType(int code, MvInput input) {
        return switch (code) {
            case WireTypes.CLASS_TYPE -> ClassType.read(input);
            case WireTypes.TAGGED_CLASS_TYPE -> ClassType.readTagged(input);
            case WireTypes.PARAMETERIZED_TYPE -> ClassType.readParameterized(input);
            case WireTypes.VARIABLE_TYPE -> VariableType.read(input);
            case WireTypes.CAPTURED_TYPE -> CapturedType.read(input);
            case WireTypes.LONG_TYPE -> PrimitiveType.longType;
            case WireTypes.CHAR_TYPE -> PrimitiveType.charType;
            case WireTypes.DOUBLE_TYPE -> PrimitiveType.doubleType;
            case WireTypes.NULL_TYPE -> PrimitiveType.nullType;
            case WireTypes.VOID_TYPE -> PrimitiveType.voidType;
            case WireTypes.TIME_TYPE -> PrimitiveType.timeType;
            case WireTypes.PASSWORD_TYPE -> PrimitiveType.passwordType;
            case WireTypes.STRING_TYPE -> PrimitiveType.stringType;
            case WireTypes.BOOLEAN_TYPE -> PrimitiveType.booleanType;
            case WireTypes.FUNCTION_TYPE -> FunctionType.read(input);
            case WireTypes.UNCERTAIN_TYPE -> UncertainType.read(input);
            case WireTypes.UNION_TYPE -> UnionType.read(input);
            case WireTypes.INTERSECTION_TYPE -> IntersectionType.read(input);
            case WireTypes.READ_ONLY_ARRAY_TYPE -> ArrayType.read(input, ArrayKind.READ_ONLY);
            case WireTypes.READ_WRITE_ARRAY_TYPE -> ArrayType.read(input, ArrayKind.READ_WRITE);
            case WireTypes.CHILD_ARRAY_TYPE -> ArrayType.read(input, ArrayKind.CHILD);
            case WireTypes.VALUE_ARRAY_TYPE -> ArrayType.read(input, ArrayKind.VALUE);
            case WireTypes.NEVER_TYPE -> NeverType.instance;
            case WireTypes.ANY_TYPE -> AnyType.instance;
            default -> throw new InternalException("Invalid type key code: " + code);
        };
    }

    public abstract int getPrecedence();

}
