package org.metavm.object.type;

import org.metavm.api.EntityType;
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
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType
public abstract class Type extends ValueElement implements TypeOrTypeKey {

    public boolean isViewType(Type type) {
        return this.equals(type);
    }

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

    public boolean isConvertibleFrom(Type that) {
        if(isAssignableFrom(that))
            return true;
        return switch (that) {
            case UnionType unionType -> NncUtils.allMatch(unionType.getMembers(), this::isConvertibleFrom);
            case IntersectionType intersectionType ->
                    NncUtils.anyMatch(intersectionType.getTypes(), this::isConvertibleFrom);
            default -> isConvertibleFrom0(that);
        };
    }

    protected boolean isConvertibleFrom0(Type that) {
        return false;
    }

    public Value convert(Value instance) {
        throw new UnsupportedOperationException();
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

    public abstract void write(InstanceOutput output);

    public int getTypeTag() {
        return TypeTags.DEFAULT;
    }

    public static Type readType(InstanceInput input, TypeDefProvider typeDefProvider) {
        var code = input.read();
        return switch (code) {
            case TypeKeyCodes.CLASS -> ClassType.read(input, typeDefProvider);
            case TypeKeyCodes.TAGGED_CLASS -> ClassType.readTagged(input, typeDefProvider);
            case TypeKeyCodes.PARAMETERIZED -> ClassType.readParameterized(input, typeDefProvider);
            case TypeKeyCodes.VARIABLE -> VariableType.read(input, typeDefProvider);
            case TypeKeyCodes.CAPTURED -> CapturedType.read(input, typeDefProvider);
            case TypeKeyCodes.LONG -> PrimitiveType.longType;
            case TypeKeyCodes.CHAR -> PrimitiveType.charType;
            case TypeKeyCodes.DOUBLE -> PrimitiveType.doubleType;
            case TypeKeyCodes.NULL -> PrimitiveType.nullType;
            case TypeKeyCodes.VOID -> PrimitiveType.voidType;
            case TypeKeyCodes.TIME -> PrimitiveType.timeType;
            case TypeKeyCodes.PASSWORD -> PrimitiveType.passwordType;
            case TypeKeyCodes.STRING -> PrimitiveType.stringType;
            case TypeKeyCodes.BOOLEAN -> PrimitiveType.booleanType;
            case TypeKeyCodes.FUNCTION -> FunctionType.read(input, typeDefProvider);
            case TypeKeyCodes.UNCERTAIN -> UncertainType.read(input, typeDefProvider);
            case TypeKeyCodes.UNION -> UnionType.read(input, typeDefProvider);
            case TypeKeyCodes.INTERSECTION -> IntersectionType.read(input, typeDefProvider);
            case TypeKeyCodes.READ_ONLY_ARRAY -> ArrayType.read(input, ArrayKind.READ_ONLY, typeDefProvider);
            case TypeKeyCodes.READ_WRITE_ARRAY -> ArrayType.read(input, ArrayKind.READ_WRITE, typeDefProvider);
            case TypeKeyCodes.CHILD_ARRAY -> ArrayType.read(input, ArrayKind.CHILD, typeDefProvider);
            case TypeKeyCodes.VALUE_ARRAY -> ArrayType.read(input, ArrayKind.VALUE, typeDefProvider);
            case TypeKeyCodes.NEVER -> NeverType.instance;
            case TypeKeyCodes.ANY -> AnyType.instance;
            default -> throw new InternalException("Invalid type key code: " + code);
        };
    }

    public abstract int getPrecedence();

}
