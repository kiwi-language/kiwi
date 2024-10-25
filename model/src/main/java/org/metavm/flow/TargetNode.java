package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.TargetNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class TargetNode extends NodeRT {

    public static TargetNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var param = (TargetNodeParam) nodeDTO.getParam();
        var node = (TargetNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new TargetNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope);
        if(stage == NodeSavingStage.FINALIZE) {
            node.setSources(
                    NncUtils.map(
                            param.sourceIds(),
                            sourceId -> Objects.requireNonNull(context.getEntity(GotoNode.class, sourceId))
                    )
            );
        }
        return node;
    }

    @ChildEntity
    private final ReadWriteArray<GotoNode> sources = addChild(new ReadWriteArray<>(GotoNode.class), "sources");

    public TargetNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, code, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTargetNode(this);
    }

    @Override
    protected TargetNodeParam getParam(SerializeContext serializeContext) {
        return new TargetNodeParam(NncUtils.map(sources, serializeContext::getStringId));
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("LabelNode");
    }

    public void setSources(List<GotoNode> sources) {
        this.sources.reset(sources);
    }

    public void addSource(GotoNode source) {
        this.sources.add(source);
    }

}
