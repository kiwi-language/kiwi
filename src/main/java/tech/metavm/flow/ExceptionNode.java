package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.ExceptionParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.StringInstance;

@EntityType("异常节点")
public class ExceptionNode extends NodeRT<ExceptionParamDTO> {

    public static ExceptionNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ExceptionParamDTO param = nodeDTO.getParam();
        var parsingContext = new FlowParsingContext(prev, entityContext.getInstanceContext());
        var message = ValueFactory.create(param.message(), parsingContext);
        return new ExceptionNode(nodeDTO.tmpId(), nodeDTO.name(), message, prev, scope);
    }

    @ChildEntity("错误提示")
    private Value message;

    public ExceptionNode(Long tmpId, String name, Value message, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
        this.message = message;
    }

    @Override
    protected void setParam(ExceptionParamDTO param, IEntityContext entityContext) {
        if(param != null) {
            message = ValueFactory.create(param.message(), getParsingContext(entityContext));
        }
    }

    @Override
    protected ExceptionParamDTO getParam(boolean persisting) {
        return new ExceptionParamDTO(message.toDTO(persisting));
    }

    @Override
    public void execute(FlowFrame frame) {
        String messageStr = message != null ? ((StringInstance)message.evaluate(frame)).getValue() : null;
        frame.exception(messageStr);
    }

    public Value getMessage() {
        return message;
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
