package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.GetObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;

public class GetObjectNode extends NodeRT<GetObjectParamDTO> {

    private Type objectType;
    private Value objectId;

    public GetObjectNode(NodeDTO nodeDTO, GetObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getTypeFromContext(param.typeId()), scope);
        setParam(param);
    }

    public GetObjectNode(NodePO nodePO, GetObjectParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    public Type getObjectType() {
        return objectType;
    }

    public Value getObjectId() {
        return objectId;
    }

    @Override
    protected void setParam(GetObjectParamDTO param) {
        objectType = context.getType(param.typeId());
        objectId = ValueFactory.getValue(param.id());
    }

    @Override
    protected GetObjectParamDTO getParam(boolean forPersistence) {
        return new GetObjectParamDTO(objectType.getId(), objectId.toDTO());
    }

    @Override
    public void execute(FlowFrame frame) {
        long id = (long) objectId.evaluate(frame);
        Instance instance = frame.getInstance(id);
        frame.setResult(instance);
    }
}
