package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.DeleteObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;

public class DeleteObjectNode extends NodeRT<DeleteObjectParamDTO> {

    private Value objectId;

    public DeleteObjectNode(NodeDTO nodeDTO, DeleteObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        setParam(param);
    }

    public DeleteObjectNode(NodePO nodePO, DeleteObjectParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    public Value getObjectId() {
        return objectId;
    }

    @Override
    protected DeleteObjectParamDTO getParam(boolean persisting) {
        return new DeleteObjectParamDTO(objectId.toDTO(persisting));
    }

    @Override
    protected void setParam(DeleteObjectParamDTO param) {
        setObjectId(param.objectId());
    }

    public void setObjectId(ValueDTO objectId) {
        this.objectId = ValueFactory.getValue(objectId, getParsingContext());
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.deleteInstance((long) objectId.evaluate(frame));
    }
}
