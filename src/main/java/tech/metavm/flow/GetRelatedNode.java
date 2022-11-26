package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.GetRelatedParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

@EntityType("查询关联对象节点")
public class GetRelatedNode extends NodeRT<GetRelatedParamDTO> {

    @EntityField("对象")
    private Value objectId;
    @EntityField("字段")
    private Field field;

    public GetRelatedNode(NodeDTO nodeDTO, Type type, ScopeRT scope) {
        super(nodeDTO, type, scope);
        setParam(nodeDTO.getParam());
    }

    @Override
    protected GetRelatedParamDTO getParam(boolean persisting) {
        return new GetRelatedParamDTO(objectId.toDTO(persisting), field.getId());
    }

    @Override
    protected void setParam(GetRelatedParamDTO param) {
        objectId = ValueFactory.getValue(param.objectId(), getParsingContext());
//        field = context.getField(param.fieldId());
        field = getOutputType().getField(param.fieldId());
    }

    @Override
    public void execute(FlowFrame frame) {
        Instance instance = (Instance) objectId.evaluate(frame);
        frame.setResult(
                NncUtils.get(instance, inst -> inst.getInstance(field.getId()))
        );
    }
}
