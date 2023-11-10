package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.DeleteObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;

@EntityType("删除对象节点")
public class DeleteObjectNode extends NodeRT<DeleteObjectParamDTO> {

    public static DeleteObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        DeleteObjectNode node = new DeleteObjectNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @ChildEntity("对象")
    private Value objectId;

    public DeleteObjectNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
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
        this.objectId = ValueFactory.create(objectId, getParsingContext(entityContext));
    }

    public void setObjectId(Value objectId) {
        this.objectId = objectId;
    }

    @Override
    public void execute(MetaFrame frame) {
        frame.deleteInstance(objectId.evaluate(frame));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteObjectNode(this);
    }
}
