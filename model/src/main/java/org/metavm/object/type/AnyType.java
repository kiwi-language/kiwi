package org.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.AnyTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class AnyType extends Type {

    public static final AnyType instance = new AnyType();
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private AnyType() {
        super();
    }

    @Override
    public String getName() {
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
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new AnyTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return !that.isNull();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return "Any";
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return "any";
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.ANY_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.ANY_TYPE);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyType;
    }

    @Override
    public int hashCode() {
        return AnyType.class.hashCode();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAnyType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitAnyType(this, s);
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
