package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.LoadNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;

import javax.annotation.Nullable;
import java.util.Objects;

public class LoadNode extends VariableAccessNode {

    public static LoadNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        LoadNodeParam param = nodeDTO.getParam();
        var node = (LoadNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var type = TypeParser.parseType(nodeDTO.outputType(), context);
            node = new LoadNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope, param.index());
        }
        return node;
    }

    public LoadNode(Long tmpId, @NotNull String name, @Nullable String code, Type outputType,
                    @Nullable NodeRT previous, @NotNull ScopeRT scope, int index) {
        super(tmpId, name, code, outputType, previous, scope, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LoadNodeParam(index, getType().toExpression(serializeContext));
    }

    @Override
    @NotNull
    public Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next(frame.load(index));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("load " + index);
    }

    public int getIndex() {
        return index;
    }
}
