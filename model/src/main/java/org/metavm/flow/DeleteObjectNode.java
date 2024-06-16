package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.DeleteObjectNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

@EntityType
public class DeleteObjectNode extends NodeRT {

    public static DeleteObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        DeleteObjectNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var objectId = ValueFactory.create(param.objectId(), parsingContext);
        DeleteObjectNode node = (DeleteObjectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null)
            node.setObject(objectId);
        else
            node = new DeleteObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, objectId);
        return node;
    }

    private Value object;

    public DeleteObjectNode(Long tmpId, String name, @Nullable String code,  NodeRT prev, ScopeRT scope, Value object) {
        super(tmpId, name, code, null, prev, scope);
        this.object = object;
    }

    public Value getObject() {
        return object;
    }

    @Override
    protected DeleteObjectNodeParam getParam(SerializeContext serializeContext) {
        return new DeleteObjectNodeParam(object.toDTO());
    }

    public void setObject(Value object) {
        this.object = object;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        frame.deleteInstance((DurableInstance) object.evaluate(frame));
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("delete " + object.getText());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteObjectNode(this);
    }
}
