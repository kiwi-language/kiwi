package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionRefKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FunctionRef extends FlowRef {

    public FunctionRef(Function rawFlow, List<? extends Type> typeArguments) {
        super(rawFlow, typeArguments);
    }

    public static Element create(Function rawFlow, List<Type> typeArguments) {
        if(typeArguments.equals(rawFlow.getDefaultTypeArguments()))
            typeArguments = List.of();
        return new FunctionRef(rawFlow, typeArguments);
    }

    @Override
    public Function getRawFlow() {
        return (Function) super.getRawFlow();
    }

    @Override
    public FunctionRef getParameterized(List<? extends Type> typeArguments) {
        if (typeArguments.equals(getRawFlow().getDefaultTypeArguments()))
            typeArguments = List.of();
        return new FunctionRef(getRawFlow(), typeArguments);
    }

    @Override
    protected TypeMetadata getTypeMetadata0() {
        return getRawFlow().getTypeMetadata(getTypeArguments());
    }

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(java.util.function.Function<ITypeDef, Id> getTypeDefId) {
        try(var serContext = SerializeContext.enter()) {
            return toDTO(serContext, typeDef -> Constants.addIdPrefix(getTypeDefId.apply(typeDef).toString()));
        }
    }

    public String toExpression(@Nullable java.util.function.Function<ITypeDef, String> getTypeDefExpr) {
        try(var serContext = SerializeContext.enter()) {
            return toExpression(serContext, getTypeDefExpr);
        }
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable java.util.function.Function<ITypeDef, String> getTypeDefExpr) {
        return "func " +
                (getTypeDefExpr != null ?
                        getTypeDefExpr.apply(getRawFlow()) :
                        Constants.addIdPrefix(serializeContext.getStringId(getRawFlow()))
                )
                + (isParameterized() ?
                        "<" + NncUtils.join(getTypeArguments(), t -> t.toExpression(serializeContext, getTypeDefExpr)) + ">"
                        : ""
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionRef(this);
    }

    public FunctionRefKey toDTO(SerializeContext serializeContext, java.util.function.Function<ITypeDef, String> getTypeDefId) {
        return new FunctionRefKey(
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(getTypeArguments(), t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_REF);
        output.writeEntityId(getRawFlow());
        output.writeInt(typeArguments.size());
        for (Type typeArgument : typeArguments) {
            typeArgument.write(output);
        }
    }

    public static FunctionRef read(MvInput input) {
        var rawFunc = input.getFunction(input.readId());
        var typeArgsCount = input.readInt();
        var typeArgs = new ArrayList<Type>(typeArgsCount);
        for (int i = 0; i < typeArgsCount; i++) {
            typeArgs.add(Type.readType(input));
        }
        return new FunctionRef(rawFunc, typeArgs);
    }

    @Override
    public String getTypeDesc() {
        var name = getRawFlow().getName();
        return typeArguments.isEmpty() ? name : name + "<" + NncUtils.join(typeArguments, Type::getTypeDesc) + ">";
    }

    @Override
    protected String toString0() {
        return getTypeDesc();
    }

    public boolean isNative() {
        return getRawFlow().isNative();
    }
}
