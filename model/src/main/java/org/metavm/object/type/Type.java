package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.IdentitySet;
import org.metavm.util.InternalException;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public abstract class Type implements TypeOrTypeKey, Writable, ValueObject, Element, NativeValue {

    @SuppressWarnings("unused")
    private static Klass __klass__;

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
        return false;
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

    public boolean isAssignableFrom(Type that) {
        if (that instanceof NeverType)
            return true;
        return that instanceof IVariableType && isAssignableFrom1(that) || isAssignableFrom1(that.getUpperBound());
    }

    private boolean isAssignableFrom1(Type that) {
        return switch (that) {
            case UnionType unionType -> Utils.allMatch(unionType.getMembers(), this::isAssignableFrom);
            case IntersectionType intersectionType ->
                    Utils.anyMatch(intersectionType.getTypes(), this::isAssignableFrom);
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
        return isAssignableFrom(value.getValueType());
    }

    public boolean isInt() {
        return false;
    }

    public boolean isStackInt() {
        return false;
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
    public boolean isChar() {
        return false;
    }

    @NoProxy
    public boolean isShort() {
        return false;
    }

    @NoProxy
    public boolean isByte() {
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

    public boolean isValueType() {
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
    public String toString() {
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

    public abstract int getPrecedence();

    public boolean isFloat() {
        return false;
    }

    public Value fromStackValue(Value value) {
        return value;
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitType(this);
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write(getTypeDesc());
    }

    @Override
    public void writeInstance(MvOutput output) {
        write(output);
    }

    @Override
    public Object toJson() {
        return getTypeDesc();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    public void forEachReference(Consumer<Reference> action) {
    }
}
