package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeParam;

import javax.annotation.Nullable;
import java.util.List;

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
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
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
    public TypeKey getTypeKey() {
        return new UncertainTypeKey(lowerBound.getStringId(), upperBound.getStringId());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return getLowerBound().isAssignableFrom0(that);
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
        try(var serContext = SerializeContext.enter()) {
            return new UncertainTypeParam(
                    serContext.getRef(lowerBound),
                    serContext.getRef(upperBound)
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
    public List<Type> getComponentTypes() {
        return List.of(lowerBound, upperBound);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUncertainType(this);
    }
}
