package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.OrNodeParam;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class OrNode extends NodeRT {

    public static OrNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        OrNode node = (OrNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            OrNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.first(), parsingContext);
            var second = ValueFactory.create(param.second(), parsingContext);
            node = new OrNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, second);
        }
        return node;
    }

    private final Value first;
    private final Value second;

    public OrNode(Long tmpId,
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
        return visitor.visitOrNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new OrNodeParam(first.toDTO(), second.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v1 = (BooleanValue) first.evaluate(frame);
        var v2 = (BooleanValue) second.evaluate(frame);
        return next(v1.or(v2));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(first.getText() + " || " + second.getText());
    }

    public Value getFirst() {
        return first;
    }

    public Value getSecond() {
        return second;
    }
}
