package org.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.NullTypeKey;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.function.Consumer;
import java.util.function.Function;

public class NullType extends Type {

    public final static NullType instance = new NullType();
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private NullType() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullType;
    }

    @Override
    public int hashCode() {
        return NullType.class.hashCode();
    }

    @Override
    public String getName() {
        return "null";
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.NULL;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public NullTypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new NullTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return that instanceof NullType;
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Null";
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return "null";
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.NULL_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NULL_TYPE);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNullType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitNullType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }
}
