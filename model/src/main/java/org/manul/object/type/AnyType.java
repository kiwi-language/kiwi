package org.manul.object.type;

import org.jetbrains.annotations.Nullable;
import org.manul.api.Entity;
import org.manul.entity.ElementVisitor;
import org.manul.entity.SerializeContext;
import org.manul.flow.Flow;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.rest.dto.AnyTypeKey;
import org.manul.object.type.rest.dto.TypeKey;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class AnyType extends Type {

    public static final AnyType instance = new AnyType();

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
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }
}
