package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.ExceptionParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.StringInstance;

@EntityType("异常节点")
public class ExceptionNode extends NodeRT<ExceptionParamDTO> {

    public static ExceptionNode create(NodeDTO nodeDTO, IEntityContext entityContext) {
        ExceptionNode node = new ExceptionNode(nodeDTO, entityContext.getScope(nodeDTO.scopeId()));
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @ChildEntity("错误提示")
    private Value message;

    public ExceptionNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
    }

    @Override
    protected void setParam(ExceptionParamDTO param, IEntityContext entityContext) {
        if(param != null) {
            message = ValueFactory.getValue(param.message(), getParsingContext(entityContext));
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

}
