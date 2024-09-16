package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.object.type.rest.dto.UncertainTypeKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType
public class UncertainType extends CompositeType {

    public static final UncertainType asterisk = new UncertainType(NeverType.instance, UnionType.nullableAnyType);

    public static UncertainType createLowerBounded(Type lowerBound) {
        return new UncertainType(lowerBound, UnionType.nullableAnyType);
    }

    public static UncertainType createUpperBounded(Type upperBound) {
        return new UncertainType(NeverType.instance, upperBound);
    }

    private Type upperBound;
    private Type lowerBound;

    public UncertainType(Type lowerBound, Type upperBound) {
        super();
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private static String getName(Type lowerBound, Type upperBound) {
        return "[" + lowerBound.getName() + "," + upperBound.getName() + "]";
    }


    private static @Nullable String getCode(Type lowerBound, Type upperBound) {
        if (lowerBound.getCode() != null && upperBound.getCode() != null)
            return "[" + lowerBound.getCode() + "," + upperBound.getCode() + "]";
        else
            return null;
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

    public void setUpperBound(Type upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return List.of(upperBound);
    }

    public void setLowerBound(Type lowerBound) {
        this.lowerBound = lowerBound;
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

    @Nullable
    @Override
    public String getCode() {
        if (lowerBound.getCode() == null || upperBound.getCode() == null)
            return null;
        return "[" + lowerBound.getCode() + "," + upperBound.getCode() + "]";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.UNCERTAIN;
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
        return TypeKeyCodes.UNCERTAIN;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.UNCERTAIN);
        lowerBound.write(output);
        upperBound.write(output);
    }

    public static UncertainType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new UncertainType(Type.readType(input, typeDefProvider), Type.readType(input, typeDefProvider));
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
