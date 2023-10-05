package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.ExceptionParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;

@EntityType("异常节点")
public class RaiseNode extends NodeRT<ExceptionParamDTO> {

    public static RaiseNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ExceptionParamDTO param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, entityContext.getInstanceContext());
        var exception = ValueFactory.create(param.exception(), parsingContext);
        return new RaiseNode(nodeDTO.tmpId(), nodeDTO.name(), exception, prev, scope);
    }

    @ChildEntity("异常")
    private Value exception;

    public RaiseNode(Long tmpId, String name, Value exception, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
        this.exception = exception;
    }

    @Override
    protected void setParam(ExceptionParamDTO param, IEntityContext entityContext) {
        if (param != null) {
            exception = ValueFactory.create(param.exception(), getParsingContext(entityContext));
        }
    }

    @Override
    protected ExceptionParamDTO getParam(boolean persisting) {
        return new ExceptionParamDTO(exception.toDTO(persisting));
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.exception((ClassInstance) this.exception.evaluate(frame));
    }

    public Value getException() {
        return exception;
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
