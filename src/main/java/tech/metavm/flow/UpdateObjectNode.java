package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateObjectParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

public class UpdateObjectNode extends NodeRT<UpdateObjectParamDTO> {

    private Value objectId;
    private List<FieldParam> fieldParams;

    public UpdateObjectNode(NodeDTO nodeDTO, UpdateObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, null, scope);
        setParam(param);
    }


    public UpdateObjectNode(NodePO nodePO, UpdateObjectParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    public Value getObjectId() {
        return objectId;
    }

    public List<FieldParam> getFieldParams() {
        return fieldParams;
    }

    @Override
    protected void setParam(UpdateObjectParamDTO param) {
        objectId = ValueFactory.getValue(param.objectId(), getParsingContext());
        fieldParams = NncUtils.map(
                param.fields(),
                fieldParamDTO -> new FieldParam(fieldParamDTO, getFlow().getContext(), getParsingContext())
        );
    }

    @Override
    protected UpdateObjectParamDTO getParam(boolean persisting) {
        return new UpdateObjectParamDTO(
                objectId.toDTO(persisting),
                NncUtils.map(fieldParams, fp -> fp.toDTO(persisting))
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        Instance instance = (Instance) objectId.evaluate(frame);
        if(instance != null) {
            for (FieldParam fieldParam : fieldParams) {
                InstanceFieldDTO instanceFieldDTO = fieldParam.evaluate(frame);
                instance.setRawFieldValue(fieldParam.getField().getId(), instanceFieldDTO);
            }
        }
    }
}
