package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.NoopNodeParam;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

@EntityType
public class NoopNode extends NodeRT {

    public static NoopNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (NoopNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NoopNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope);
        return node;
    }

    public NoopNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
    }

    @Override
    protected NoopNodeParam getParam(SerializeContext serializeContext) {
        return new NoopNodeParam();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("noop");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNoopNode(this);
    }
}
