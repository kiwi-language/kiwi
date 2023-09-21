package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.GetRelatedParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

@EntityType("查询关联对象节点")
public class GetRelatedNode extends NodeRT<GetRelatedParamDTO> {

    public static GetRelatedNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        FlowParsingContext parsingContext = FlowParsingContext.create(scope, prev, context.getInstanceContext());
        GetRelatedParamDTO param = nodeDTO.getParam();
        Value objectId = ValueFactory.create(param.objectId(), parsingContext);
        ClassType objectType = (ClassType) objectId.getType();
        Field field = objectType.getField(param.fieldId());
        GetRelatedNode node = new GetRelatedNode(nodeDTO.tmpId(), nodeDTO.name(), objectId, field, prev, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("对象")
    private Value objectId;
    @EntityField("字段")
    private Field field;

    public GetRelatedNode(Long tmpId, String name, Value objectId, Field field, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, field.getType(), previous, scope);
        this.objectId = objectId;
        this.field = field;
    }

    @Override
    protected GetRelatedParamDTO getParam(boolean persisting) {
        return new GetRelatedParamDTO(objectId.toDTO(persisting), field.getIdRequired());
    }

    @Override
    protected void setParam(GetRelatedParamDTO param, IEntityContext entityContext) {
        objectId = ValueFactory.create(param.objectId(), getParsingContext(entityContext));
        field = getType().getField(param.fieldId());
    }

    @Override
    public void execute(FlowFrame frame) {
        ClassInstance instance = (ClassInstance) objectId.evaluate(frame);
        frame.setResult(
                NncUtils.get(instance, inst -> inst.getInstance(field.getIdRequired()))
        );
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }
}
