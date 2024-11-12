package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.BusinessException;
import org.metavm.util.LinkedList;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class FunctionNode extends NodeRT {

    public static FunctionNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (FunctionNode) context.getNode(nodeDTO.id());
        if (node == null) {
            var param = (FunctionNodeParam) nodeDTO.param();
            var funcType = (FunctionType) TypeParser.parseType(param.functionType(), context);
            node = new FunctionNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, funcType);
        }
        return node;
    }

    private final FunctionType functionType;

    public FunctionNode(Long tmpId, String name, NodeRT previous, ScopeRT scope, FunctionType functionType) {
        super(tmpId, name, null, previous, scope);
        this.functionType = functionType;
    }

    @Override
    protected FunctionNodeParam getParam(SerializeContext serializeContext) {
        return new FunctionNodeParam(functionType.toExpression(serializeContext));
    }

    private void check(Value func, List<Value> arguments) throws BusinessException {
        var funcType = ((FunctionType) Flows.getExpressionType(func.getExpression(), getPredecessor()));
        if (funcType instanceof FunctionType functionType) {
            if (arguments.size() != functionType.getParameterTypes().size()) {
                throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT1,
                        "expecting " + functionType.getParameterTypes().size() + " arguments but got " + arguments.size());
            }
            for (int i = 0; i < arguments.size(); i++) {
                if (!functionType.getParameterTypes().get(i).isAssignableFrom(arguments.get(i).getType())) {
                    throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT1,
                            "expecting " + functionType.getParameterTypes().get(i).getTypeDesc() + " for argument " + i + " but "
                                    + "got " + arguments.get(i).getType().getTypeDesc()
                     );
                }
            }
        } else {
            throw new BusinessException(ErrorCode.NOT_A_FUNCTION, func.getExpression());
        }
    }

    @Override
    public int execute(MetaFrame frame) {
        var args = new LinkedList<org.metavm.object.instance.core.Value>();
        var numArgs = functionType.getParameterTypes().size();
        for (int i = 0; i < numArgs; i++) {
            args.addFirst(frame.pop());
        }
        var funcInst = (FunctionValue) frame.pop();
        var result = funcInst.execute(args, frame);
        if(result.exception() != null)
            frame.catchException(this, result.exception());
        else {
            if(result.ret() != null)
                frame.push(result.ret());
        }
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("call " + functionType.toExpression());
    }

    @Override
    public int getStackChange() {
        var paramCount = functionType.getParameterTypes().size();
        return functionType.isVoid() ? -paramCount - 1 : paramCount;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionNode(this);
    }

    @Nullable
    @Override
    public Type getType() {
        var type = functionType.getReturnType();
        return type.isVoid() ? null : type;
    }
}
