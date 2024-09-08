package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionRefDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.Constants;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType
public class FunctionRef extends FlowRef {

    public static FunctionRef create(FunctionRefDTO functionRefDTO, IEntityContext context) {
        return new FunctionRef(
                context.getFunction(functionRefDTO.rawFlowId()),
                NncUtils.map(functionRefDTO.typeArguments(), t -> TypeParser.parseType(t, context))
        );
    }

    public FunctionRef(Function rawFlow, List<Type> typeArguments) {
        super(rawFlow, typeArguments);
    }

    @Override
    public Function getRawFlow() {
        return (Function) super.getRawFlow();
    }

    @Override
    public Function resolve() {
        return (Function) super.resolve();
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.FUNCTION_REF);
        output.writeId(getRawFlow().getId());
        output.writeInt(getTypeArguments().size());
        for (Type typeArgument : getTypeArguments()) {
            typeArgument.write(output);
        }
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

    public FunctionRefDTO toDTO(SerializeContext serializeContext) {
        return toDTO(serializeContext, null);
    }

    public FunctionRefDTO toDTO(SerializeContext serializeContext, java.util.function.Function<ITypeDef, String> getTypeDefId) {
        return new FunctionRefDTO(
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(getTypeArguments(), t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    public static FunctionRef read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var rawFunc = (Function) typeDefProvider.getTypeDef(input.readId());
        var typeArgsCount = input.readInt();
        var typeArgs = new ArrayList<Type>(typeArgsCount);
        for (int i = 0; i < typeArgsCount; i++) {
            typeArgs.add(Type.readType(input, typeDefProvider));
        }
        return new FunctionRef(rawFunc, typeArgs);
    }

}
