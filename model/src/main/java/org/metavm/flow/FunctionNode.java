package org.metavm.flow;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.FunctionNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.type.FunctionType;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class FunctionNode extends NodeRT {

    public static FunctionNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        FunctionNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var func = ValueFactory.create(param.func(), parsingContext);
        var args = NncUtils.map(param.arguments(), arg -> ValueFactory.create(arg, parsingContext));
        var node = (FunctionNode) context.getNode(nodeDTO.id());
        if (node != null)
            node.update(func, args);
        else
            node = new FunctionNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, func, args);
        return node;
    }

    private Value func;
    @ChildEntity
    private final ReadWriteArray<Value> arguments = addChild(new ReadWriteArray<>(Value.class), "arguments");

    public FunctionNode(Long tmpId, String name, @Nullable String code,  NodeRT previous, ScopeRT scope, Value func, List<Value> arguments) {
        super(tmpId, name, code,
                ((FunctionType) Flows.getExpressionType(func.getExpression(), previous, scope)).getReturnType(), previous, scope);
        check(func, arguments);
        this.func = func;
        this.arguments.addAll(arguments);
    }

    @Override
    protected FunctionNodeParam getParam(SerializeContext serializeContext) {
        return new FunctionNodeParam(
                func.toDTO(),
                NncUtils.map(arguments, Value::toDTO)
        );
    }

    public void update(Value func, List<Value> arguments) {
        check(func, arguments);
        this.func = func;
        this.arguments.reset(arguments);
    }

    private void check(Value func, List<Value> arguments) throws BusinessException {
        var funcType = ((FunctionType) Flows.getExpressionType(func.getExpression(), getPredecessor(), getScope()));
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
    public NodeExecResult execute(MetaFrame frame) {
        var funcInst = (FunctionValue) func.evaluate(frame);
        var result = funcInst.execute(NncUtils.map(arguments, arg -> arg.evaluate(frame)), frame);
        if(result.exception() != null)
            return frame.catchException(this, result.exception());
        else
            return next(result.ret());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(func.getText() + "(" + NncUtils.join(arguments, Value::getText, ", ") + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionNode(this);
    }
}
