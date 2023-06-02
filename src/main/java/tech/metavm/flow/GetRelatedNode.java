package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.GetRelatedParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

@EntityType("查询关联对象节点")
public class GetRelatedNode extends NodeRT<GetRelatedParamDTO> {

    public static GetRelatedNode create(NodeDTO nodeDTO, IEntityContext context) {
        ScopeRT scope = context.getScope(nodeDTO.scopeId());
        NodeRT<?> prev = context.getNode(nodeDTO.prevId());
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context.getInstanceContext());
        GetRelatedParamDTO param = nodeDTO.getParam();
        Value objectId = ValueFactory.getValue(param.objectId(), parsingContext);
        GetRelatedNode node = new GetRelatedNode(nodeDTO, objectId, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("对象")
    private Value objectId;
    @EntityField("字段")
    private Field field;

    public GetRelatedNode(NodeDTO nodeDTO, Value objectId, ScopeRT scope) {
        super(nodeDTO, objectId.getType(), scope);
        this.objectId = objectId;
    }

    @Override
    protected GetRelatedParamDTO getParam(boolean persisting) {
        return new GetRelatedParamDTO(objectId.toDTO(persisting), field.getId());
    }

    @Override
    protected void setParam(GetRelatedParamDTO param, IEntityContext entityContext) {
        objectId = ValueFactory.getValue(param.objectId(), getParsingContext(entityContext));
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
