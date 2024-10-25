package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.util.ContextUtil;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class SelfNode extends NodeRT {

    public static SelfNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (SelfNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var method = (Method) scope.getFlow();
            node = new SelfNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), method.getDeclaringType().getType(), prev, scope);
        }
        return node;
    }

    public SelfNode(Long tmpId, String name, @Nullable String code, ClassType type, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, code, type, prev, scope);
    }

    @Override
    protected Void getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        try(var ignored = ContextUtil.getProfiler().enter("SelfNode.execute")) {
            return next(Objects.requireNonNull(frame.getSelf()).getReference());
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("self");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSelfNode(this);
    }
}
