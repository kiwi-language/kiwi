package org.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.NeverTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.function.Function;

@Entity
public class NeverType extends Type {

    public static final NeverType instance = new NeverType();

    private NeverType() {
        super();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeverType(this);
    }

    @Override
    public String getName() {
        return "Never";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.NEVER;
    }

    @Override
    public Type getType() {
        return StdKlass.neverType.type();
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
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
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return "never";
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.NEVER_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NEVER_TYPE);
    }

    @Override
    public int getPrecedence() {
        return 0;
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
