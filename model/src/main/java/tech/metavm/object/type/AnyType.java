package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.AnyTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.util.InstanceOutput;

import java.util.function.Function;

@EntityType("任意类型")
public class AnyType extends Type {

    public AnyType() {
        super();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitObjectType(this);
    }

    @Override
    public String getName() {
        return "对象";
    }

    @Override
    public @NotNull String getCode() {
        return "Any";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.ANY;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
        return new AnyTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return true;
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitAnyType(this, s);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Any";
    }

    @Override
    public AnyType copy() {
        return new AnyType();
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<TypeDef, String> getTypeDefExpr) {
        return "any";
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.ANY;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.ANY);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof AnyType;
    }

    @Override
    public int hashCode() {
        return AnyType.class.hashCode();
    }

}
