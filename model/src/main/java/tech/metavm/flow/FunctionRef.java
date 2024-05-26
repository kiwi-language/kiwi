package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.FunctionRefDTO;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("FunctionRef")
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

    public FunctionRef copy() {
        var copy = new FunctionRef(getRawFlow(), getTypeArguments());
        copy.resolved = resolved;
        return copy;
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
