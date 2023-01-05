package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.DeleteObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;

@EntityType("删除对象节点")
public class DeleteObjectNode extends NodeRT<DeleteObjectParamDTO> {

    public static DeleteObjectNode create(NodeDTO nodeDTO, IEntityContext entityContext) {
        DeleteObjectNode node = new DeleteObjectNode(nodeDTO, entityContext.getScope(nodeDTO.scopeId()));
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @EntityField("对象")
    private Value objectId;

    public DeleteObjectNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, null, scope);
    }

    public Value getObjectId() {
        return objectId;
    }

    @Override
    protected DeleteObjectParamDTO getParam(boolean persisting) {
        return new DeleteObjectParamDTO(objectId.toDTO(persisting));
    }

    @Override
    protected void setParam(DeleteObjectParamDTO param, IEntityContext entityContext) {
        setObjectId(param.objectId(), entityContext);
    }

    public void setObjectId(ValueDTO objectId, IEntityContext entityContext) {
        this.objectId = ValueFactory.getValue(objectId, getParsingContext(entityContext));
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.deleteInstance(objectId.evaluate(frame));
    }
}
