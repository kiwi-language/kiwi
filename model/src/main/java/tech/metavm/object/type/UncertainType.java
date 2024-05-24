package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.object.type.rest.dto.UncertainTypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType("不确定类型")
public class UncertainType extends CompositeType {

    @ChildEntity("上限")
    private Type upperBound;
    @ChildEntity("下限")
    private Type lowerBound;

    public UncertainType(Type lowerBound, Type upperBound) {
        super(getName(lowerBound, upperBound), getCode(lowerBound, upperBound), true, true, TypeCategory.UNCERTAIN);
        this.lowerBound = addChild(lowerBound.copy(), "lowerBound");
        this.upperBound = addChild(upperBound.copy(), "upperBound");
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
    public UncertainTypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
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
    protected UncertainTypeParam getParamInternal() {
        try (var serContext = SerializeContext.enter()) {
            return new UncertainTypeParam(
                    serContext.getStringId(lowerBound),
                    serContext.getStringId(upperBound)
            );
        }
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

    public UncertainType copy() {
        return new UncertainType(lowerBound.copy(), upperBound.copy());
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
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
}
