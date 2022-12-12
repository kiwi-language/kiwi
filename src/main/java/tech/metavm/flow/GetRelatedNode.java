package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.GetRelatedParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

@EntityType("查询关联对象节点")
public class GetRelatedNode extends NodeRT<GetRelatedParamDTO> {

    @EntityField("对象")
    private Value objectId;
    @EntityField("字段")
    private Field field;

    public GetRelatedNode(NodeDTO nodeDTO, ClassType type, ScopeRT scope) {
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
        field = getType().getField(param.fieldId());
    }

    @Override
    public void execute(FlowFrame frame) {
        ClassInstance instance = (ClassInstance) objectId.evaluate(frame);
        frame.setResult(
                NncUtils.get(instance, inst -> inst.getInstance(field.getId()))
        );
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }
}
