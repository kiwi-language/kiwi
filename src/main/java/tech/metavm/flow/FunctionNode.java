package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.FunctionNodeParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.FunctionInstance;
import tech.metavm.object.meta.FunctionType;
import tech.metavm.util.BusinessException;
import tech.metavm.entity.ChildArray;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType("函数节点")
public class FunctionNode extends NodeRT<FunctionNodeParamDTO> {

    public static FunctionNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        FunctionNodeParamDTO param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var func = ValueFactory.create(param.func(), parsingContext);
        var args = NncUtils.map(param.arguments(), arg -> ValueFactory.create(arg, parsingContext));
        return new FunctionNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, func, args);
    }

    @ChildEntity("函数")
    private Value func;
    @ChildEntity("实参列表")
    private final ChildArray<Value> arguments = addChild(new ChildArray<>(Value.class), "arguments");

    public FunctionNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value func, List<Value> arguments) {
        super(tmpId, name,
                ((FunctionType) FlowUtils.getExpressionType(func.getExpression(), previous, scope)).getReturnType(),
                previous, scope);
        check(func, arguments);
        this.func = func;
        this.arguments.addChildren(arguments);
    }

    @Override
    protected FunctionNodeParamDTO getParam(boolean persisting) {
        return new FunctionNodeParamDTO(
                func.toDTO(persisting),
                NncUtils.map(arguments, arg -> arg.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(FunctionNodeParamDTO param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        var func = this.func;
        List<Value> arguments = new ArrayList<>(this.arguments.toList());
        if (param.func() != null) {
            func = ValueFactory.create(param.func(), parsingContext);
        }
        if (param.arguments() != null) {
            arguments = NncUtils.map(param.arguments(), argDTO -> ValueFactory.create(argDTO, parsingContext));
        }
        check(func, arguments);
        this.func = func;
        this.arguments.resetChildren(arguments);
    }

    private void check(Value func, List<Value> arguments) throws BusinessException {
        var funcType = ((FunctionType) FlowUtils.getExpressionType(func.getExpression(), getPredecessor(), getScope()));
        if (funcType instanceof FunctionType functionType) {
            if (arguments.size() != functionType.getParameterTypes().size()) {
                throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
            }
            for (int i = 0; i < arguments.size(); i++) {
                if (!functionType.getParameterTypes().get(i).isAssignableFrom(arguments.get(i).getType())) {
                    throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
                }
            }
        } else {
            throw new BusinessException(ErrorCode.NOT_A_FUNCTION, func.getExpression().build(VarType.NAME));
        }
    }

    @Override
    public void execute(MetaFrame frame) {
        var funcInst = (FunctionInstance) func.evaluate(frame);
        var subFrame = funcInst.createFrame(frame.getStack(), NncUtils.map(arguments, arg -> arg.evaluate(frame)));
        frame.getStack().push(subFrame);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionNode(this);
    }
}
