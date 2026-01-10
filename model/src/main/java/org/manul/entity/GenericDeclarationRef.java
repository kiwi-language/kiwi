package org.manul.entity;

import org.manul.api.Entity;
import org.manul.api.ValueObject;
import org.manul.flow.FunctionRef;
import org.manul.flow.MethodRef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;
import org.manul.object.type.ITypeDef;
import org.manul.object.type.KlassType;
import org.manul.object.type.Type;
import org.manul.object.type.TypeDef;
import org.manul.object.type.rest.dto.GenericDeclarationRefKey;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public interface GenericDeclarationRef extends ValueObject, Element, Value {

    void write(MvOutput output);

    default @Nullable GenericDeclarationRef getOwner() {
        return null;
    }

    GenericDeclarationRefKey toGenericDeclarationKey(Function<ITypeDef, Id> getTypeDefId);

    default GenericDeclarationRefKey toGenericDeclarationKey() {
        return toGenericDeclarationKey(ITypeDef::getId);
    }

    String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr);

    List<Type> getTypeArguments();

    default List<Type> getAllTypeArguments() {
        var typeArgs = new ArrayList<Type>();
        foreachEnclosing(t -> typeArgs.addAll(t.getTypeArguments()));
        return typeArgs;
    }

    default void foreachEnclosing(Consumer<GenericDeclarationRef> action) {
        var owner = getOwner();
        if(owner != null)
            owner.foreachEnclosing(action);
        action.accept(this);
    }

    static GenericDeclarationRef read(MvInput input) {
        var kind = input.read();
        return switch (kind) {
            case WireTypes.CLASS_TYPE -> KlassType.read(input);
            case WireTypes.PARAMETERIZED_TYPE -> KlassType.readParameterized(input);
            case WireTypes.METHOD_REF -> MethodRef.read(input);
            case WireTypes.FUNCTION_REF -> FunctionRef.read(input);
            default -> throw new IllegalStateException("Unrecognized generic declaration ref kind " + kind);
        };
    }

    default void forEachTypeDef(Consumer<TypeDef> action) {
        getTypeArguments().forEach(t -> t.forEachTypeDef(action));
    }

    String getTypeDesc();

    <R> R accept(ElementVisitor<R> visitor);

    void forEachReference(Consumer<Reference> action);

}
