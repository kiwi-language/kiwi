package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NeNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class NeNode extends NodeRT {

    public static NeNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NeNode node = (NeNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            NeNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.first(), parsingContext);
            var second = ValueFactory.create(param.second(), parsingContext);
            node = new NeNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, second);
        }
        return node;
    }

    private final Value first;
    private final Value second;

    public NeNode(Long tmpId,
                  @NotNull String name,
                  @Nullable String code,
                  @Nullable NodeRT previous,
                  @NotNull ScopeRT scope,
                  Value first,
                  Value second) {
        super(tmpId, name, code, Types.getBooleanType(), previous, scope);
        this.first = first;
        this.second = second;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new NeNodeParam(first.toDTO(), second.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v1 = first.evaluate(frame);
        var v2 = second.evaluate(frame);
        return next(Instances.notEquals(v1, v2));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(first.getText() + " != " + second.getText());
    }

    public Value getFirst() {
        return first;
    }

    public Value getSecond() {
        return second;
    }
}
