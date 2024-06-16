package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.NeverTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.InstanceOutput;

import java.util.function.Function;

@EntityType
public class NeverType extends Type {

    public NeverType() {
        super();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNothingType(this);
    }

    @Override
    public String getName() {
        return "Never";
    }

    @Override
    public @NotNull String getCode() {
        return "Never";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.NEVER;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
        return new NeverTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return false;
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitNeverType(this, s);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Never";
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<TypeDef, String> getTypeDefExpr) {
        return "never";
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.NEVER;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.NEVER);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof NeverType;
    }

    @Override
    public int hashCode() {
        return NeverType.class.hashCode();
    }

}
