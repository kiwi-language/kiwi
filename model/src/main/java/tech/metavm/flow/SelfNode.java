package tech.metavm.flow;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.ContextUtil;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class SelfNode extends NodeRT {

    public static SelfNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
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
            return next(Objects.requireNonNull(frame.getSelf()));
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
