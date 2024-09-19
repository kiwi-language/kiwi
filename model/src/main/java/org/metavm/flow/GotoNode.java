package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.GotoNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.Objects;

public class GotoNode extends NodeRT {

    public static GotoNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (GotoNodeParam) nodeDTO.getParam();
        var node = (GotoNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new GotoNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, null);
        var target = context.getEntity(NodeRT.class, param.targetId());
        if(target != null)
            node.setTarget(target);
        return node;
    }

    private @NotNull NodeRT target;

    public GotoNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                    @Nullable NodeRT target) {
        super(tmpId, name, code, null, previous, scope);
        this.target = Objects.requireNonNullElse(target, this);
    }

    public GotoNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
        this.target = this;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGotoNode(this);
    }

    @Override
    protected GotoNodeParam getParam(SerializeContext serializeContext) {
        return new GotoNodeParam(serializeContext.getStringId(target));
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var s = target.getScope();
        while (s != null) {
            if(s.getBranch() != null)
                frame.setSelectedBranch(s.getBranch().getOwner(), s.getBranch());
            var owner = s.getOwner();
            if(owner != null && !(owner instanceof LambdaNode))
                s = owner.getScope();
            else
                s = null;
        }
        return NodeExecResult.jump(target);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.writeNewLine("goto " + target.getName());
    }

    public void setTarget(@NotNull NodeRT target) {
        this.target = target;
        if (target instanceof TargetNode labelNode)
            labelNode.addSource(this);
    }

    @Override
    public boolean isUnconditionalJump() {
        return true;
    }
}
