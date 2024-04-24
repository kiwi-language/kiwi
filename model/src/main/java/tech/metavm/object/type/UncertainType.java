package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.UncertainTypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType("不确定类型")
public class UncertainType extends CompositeType implements LoadAware  {

    public static final IndexDef<UncertainType> KEY_IDX = IndexDef.createUnique(UncertainType.class, "key");

    public static final IndexDef<UncertainType> LOWER_BOUND_IDX = IndexDef.create(UncertainType.class, "lowerBound");

    public static final IndexDef<UncertainType> UPPER_BOUND_IDX = IndexDef.create(UncertainType.class, "upperBound");

    @EntityField("上限")
    private Type upperBound;
    @EntityField("下限")
    private Type lowerBound;

    public UncertainType(Long tmpId, Type lowerBound, Type upperBound) {
        super(getName(lowerBound, upperBound), getCode(lowerBound, upperBound), true, true, TypeCategory.UNCERTAIN);
        setTmpId(tmpId);
        this.lowerBound = addChild(lowerBound.copy(), "lowerBound");
        this.upperBound = addChild(upperBound.copy(), "upperBound");
    }

    private static String getName(Type lowerBound, Type upperBound) {
        return "[" + lowerBound.getName() + "," + upperBound.getName() + "]";
    }


    private static @Nullable String getCode(Type lowerBound, Type upperBound) {
        if(lowerBound.getCode() != null && upperBound.getCode() != null)
            return "[" + lowerBound.getCode() + "," + upperBound.getCode() + "]";
        else
            return null;
    }

    @Override
    public UncertainTypeKey getTypeKey() {
        return new UncertainTypeKey(lowerBound.getTypeKey(), upperBound.getTypeKey());
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return getLowerBound().isAssignableFrom0(that, typeMapping);
    }

    @Override
    public boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping) {
        if(that instanceof UncertainType thatUncertainType) {
           return lowerBound.equals(thatUncertainType.lowerBound, mapping)
                   && upperBound.equals(thatUncertainType.upperBound, mapping);
        }
        else
            return false;
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
        try(var serContext = SerializeContext.enter()) {
            return new UncertainTypeParam(
                    serContext.getId(lowerBound),
                    serContext.getId(upperBound)
            );
        }
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return "[" + context.getModelName(lowerBound, this) +
                "," + context.getModelName(upperBound, this) + "]";
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return "[" + lowerBound.getInternalName(current) + "," + upperBound.getInternalName(current) + "]";
    }

    @Override
    public String getTypeDesc() {
        return "[" + lowerBound.getTypeDesc() + "," + upperBound.getTypeDesc() + "]";
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
        return new UncertainType(null, lowerBound.copy(), upperBound.copy());
    }

    @Override
    public String toTypeExpression(SerializeContext serializeContext) {
        return "[" + lowerBound.toTypeExpression(serializeContext) + "," + upperBound.toTypeExpression(serializeContext) + "]";
    }

    @Override
    public void write0(InstanceOutput output) {
       lowerBound.write(output);
       upperBound.write(output);
    }

    public static UncertainType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new UncertainType(null, Type.readType(input, typeDefProvider), Type.readType(input, typeDefProvider));
    }

}
