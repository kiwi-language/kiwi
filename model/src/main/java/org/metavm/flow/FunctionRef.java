package org.metavm.flow;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionRefDTO;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.NncUtils;

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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionRef(this);
    }

    public FunctionRefDTO toDTO(SerializeContext serializeContext) {
        return new FunctionRefDTO(
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(getTypeArguments(), t -> t.toExpression(serializeContext))
        );
    }

}
