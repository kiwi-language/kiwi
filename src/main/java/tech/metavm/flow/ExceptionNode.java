package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.ExceptionParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.NncUtils;

public class ExceptionNode extends NodeRT<ExceptionParamDTO> {

    private Value message;

    public ExceptionNode(NodeDTO nodeDTO, ExceptionParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        setParam(param);
    }

    public ExceptionNode(NodePO nodePO, ExceptionParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    @Override
    protected void setParam(ExceptionParamDTO param) {
        if(param != null) {
            message = ValueFactory.getValue(param.message(), getParsingContext());
        }
    }

    @Override
    protected ExceptionParamDTO getParam(boolean persisting) {
        return new ExceptionParamDTO(message.toDTO(persisting));
    }

    @Override
    public void execute(FlowFrame frame) {
        String messageStr = message != null ? (String) message.evaluate(frame) : null;
        frame.exception(messageStr);
    }

}
