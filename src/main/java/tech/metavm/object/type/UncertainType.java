package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeParam;

import java.util.List;
import java.util.function.Function;

@EntityType("不确定类型")
public class UncertainType extends CompositeType implements LoadAware  {

    public static final IndexDef<UncertainType> KEY_IDX = IndexDef.uniqueKey(UncertainType.class, "key");

    public static final IndexDef<UncertainType> LOWER_BOUND_IDX = IndexDef.normalKey(UncertainType.class, "lowerBound");

    public static final IndexDef<UncertainType> UPPER_BOUND_IDX = IndexDef.normalKey(UncertainType.class, "upperBound");

    @EntityField("上限")
    private Type upperBound;
    @EntityField("下限")
    private Type lowerBound;

    public UncertainType(Long tmpId, Type lowerBound, Type upperBound) {
        super(createName(lowerBound, upperBound), true, true, TypeCategory.UNCERTAIN);
        setTmpId(tmpId);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private static String createName(Type lowerBound, Type upperBound) {
        return "[" + lowerBound.getName() + "," + upperBound.getName() + "]";
    }

    @Override
    public TypeKey getTypeKey() {
        return new UncertainTypeKey(lowerBound.getRef(), upperBound.getRef());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
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
        onSuperTypesChanged();
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
        try(var context = SerializeContext.enter()) {
            return new UncertainTypeParam(
                    context.getRef(lowerBound),
                    context.getRef(upperBound)
            );
        }
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "[" + lowerBound.getKey(getJavaType) +
                "," + upperBound.getKey(getJavaType) + "]";
    }

    @Override
    public List<Type> getComponentTypes() {
        return List.of(lowerBound, upperBound);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUncertainType(this);
    }
}
