package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.GetRelatedParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

public class GetRelatedNode extends NodeRT<GetRelatedParamDTO> {

    private Value objectId;
    private long fieldId;

    public GetRelatedNode(NodeDTO nodeDTO, GetRelatedParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getFieldFromContext(param.fieldId()).getType(), scope);
        setParam(param);
    }

    public GetRelatedNode(NodePO nodePO, GetRelatedParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    @Override
    protected GetRelatedParamDTO getParam(boolean persisting) {
        return new GetRelatedParamDTO(objectId.toDTO(persisting), fieldId);
    }

    @Override
    protected void setParam(GetRelatedParamDTO param) {
        objectId = ValueFactory.getValue(param.objectId(), getParsingContext());
        fieldId = param.fieldId();
    }

    @Override
    public void execute(FlowFrame frame) {
        Instance instance = (Instance) objectId.evaluate(frame);
        frame.setResult(
                NncUtils.get(instance, inst -> inst.getInstance(fieldId))
        );
    }
}
