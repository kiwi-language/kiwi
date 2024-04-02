package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.ObjectTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;

@EntityType("任意类型")
public class AnyType extends Type {

    public AnyType() {
        super("对象", "Any", false, false, TypeCategory.OBJECT);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitObjectType(this);
    }

    @Override
    public TypeKey getTypeKey() {
        return new ObjectTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return true;
    }

    @Override
    protected TypeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return "Any";
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Any";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyType;
    }

    @Override
    public int hashCode() {
        return AnyType.class.hashCode();
    }

}
