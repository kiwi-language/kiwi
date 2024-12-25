package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.UncertainTypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Entity
public class UncertainType extends CompositeType {

    public static final UncertainType asterisk = new UncertainType(NeverType.instance, UnionType.nullableAnyType);

    public static UncertainType createLowerBounded(Type lowerBound) {
        return new UncertainType(lowerBound, UnionType.nullableAnyType);
    }

    public static UncertainType createUpperBounded(Type upperBound) {
        return new UncertainType(NeverType.instance, upperBound);
    }

    private final Type upperBound;
    private final Type lowerBound;

    public UncertainType(Type lowerBound, Type upperBound) {
        super();
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public UncertainTypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new UncertainTypeKey(lowerBound.toTypeKey(getTypeDefId), upperBound.toTypeKey(getTypeDefId));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return getLowerBound().isAssignableFrom0(that);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitUncertainType(this, s);
    }

    @Override
    public boolean contains(Type that) {
        return upperBound.isAssignableFrom(that) && that.isAssignableFrom(lowerBound);
    }

    @Override
    public boolean isUncertain() {
        return true;
    }

    @Override
    public Type getUpperBound() {
        return upperBound;
    }

    @Override
    public Type getLowerBound() {
        return lowerBound;
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return List.of(upperBound);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return "[" + lowerBound.getInternalName(current) + "," + upperBound.getInternalName(current) + "]";
    }

    @Override
    public String getName() {
        return "[" + lowerBound.getName() + "," + upperBound.getName() + "]";
    }

    @Override
    public String getTypeDesc() {
        return "[" + lowerBound.getTypeDesc() + "," + upperBound.getTypeDesc() + "]";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.UNCERTAIN;
    }

    @Override
    public Type getType() {
        return StdKlass.uncertainType.type();
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public List<Type> getComponentTypes() {
        return List.of(lowerBound, upperBound);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUncertainType(this);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return "[" + lowerBound.toExpression(serializeContext, getTypeDefExpr) + "," + upperBound.toExpression(serializeContext, getTypeDefExpr) + "]";
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.UNCERTAIN_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNCERTAIN_TYPE);
        lowerBound.write(output);
        upperBound.write(output);
    }

    @Override
    public int getPrecedence() {
        return 1;
    }

    public static UncertainType read(MvInput input) {
        return new UncertainType(input.readType(), input.readType());
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof UncertainType that && lowerBound.equals(that.lowerBound) && upperBound.equals(that.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public Type getUpperBound2() {
        return upperBound;
    }
}
