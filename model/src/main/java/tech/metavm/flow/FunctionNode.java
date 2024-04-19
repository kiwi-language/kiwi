package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.FunctionNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.FunctionType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("函数节点")
public class FunctionNode extends NodeRT {

    public static FunctionNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        FunctionNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var func = ValueFactory.create(param.func(), parsingContext);
        var args = NncUtils.map(param.arguments(), arg -> ValueFactory.create(arg, parsingContext));
        FunctionNode node = (FunctionNode) context.getNode(Id.parse(nodeDTO.id()));
        if (nodeDTO.id() != null)
            node.update(func, args);
        else
            node = new FunctionNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, func, args);
        return node;
    }

    @ChildEntity("函数")
    private Value func;
    @ChildEntity("参数列表")
    private final ChildArray<Value> arguments = addChild(new ChildArray<>(Value.class), "arguments");

    public FunctionNode(Long tmpId, String name, @Nullable String code,  NodeRT previous, ScopeRT scope, Value func, List<Value> arguments) {
        super(tmpId, name, code,
                ((FunctionType) Flows.getExpressionType(func.getExpression(), previous, scope)).getReturnType(), previous, scope);
        check(func, arguments);
        this.func = addChild(func, "func");
        this.arguments.addChildren(arguments);
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
        this.func = addChild(func, "func");
        this.arguments.resetChildren(arguments);
    }

    private void check(Value func, List<Value> arguments) throws BusinessException {
        var funcType = ((FunctionType) Flows.getExpressionType(func.getExpression(), getPredecessor(), getScope()));
        if (funcType instanceof FunctionType functionType) {
            if (arguments.size() != functionType.getParameterTypes().size()) {
                throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
            }
            for (int i = 0; i < arguments.size(); i++) {
                if (!functionType.getParameterTypes().get(i).isAssignableFrom(arguments.get(i).getType(), null)) {
                    throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
                }
            }
        } else {
            throw new BusinessException(ErrorCode.NOT_A_FUNCTION, func.getExpression());
        }
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var funcInst = (FunctionInstance) func.evaluate(frame);
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
