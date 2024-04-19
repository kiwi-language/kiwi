package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.NothingTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;

import java.util.Map;

@EntityType("不可能类型")
public class NeverType extends Type {

    public NeverType() {
        super("不可能", "Never", false, true, TypeCategory.NOTHING);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNothingType(this);
    }

    @Override
    public TypeKey getTypeKey() {
        return new NothingTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @javax.annotation.Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return false;
    }

    @Override
    public boolean equals(Type that, @javax.annotation.Nullable Map<TypeVariable, ? extends Type> mapping) {
        return equals(that);
    }

    @Override
    protected TypeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return "Never";
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Never";
    }
}
