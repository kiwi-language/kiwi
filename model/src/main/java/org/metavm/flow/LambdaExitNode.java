package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LambdaExitNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

public class LambdaExitNode extends NodeRT {

    public static LambdaExitNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (LambdaExitNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new LambdaExitNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope);
        return node;
    }

    public LambdaExitNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaExitNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LambdaExitNodeParam();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lambda-exit");
    }
}
