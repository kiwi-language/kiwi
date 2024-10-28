package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.LtNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class LtNode extends NodeRT {

    public static LtNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        LtNode node = (LtNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            LtNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.first(), parsingContext);
            var second = ValueFactory.create(param.second(), parsingContext);
            node = new LtNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, second);
        }
        return node;
    }

    private final Value first;
    private final Value second;

    public LtNode(Long tmpId,
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
        return visitor.visitLtNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LtNodeParam(first.toDTO(), second.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v1 = (NumberValue) first.evaluate(frame);
        var v2 = (NumberValue) second.evaluate(frame);
        return next((v1.lt(v2)));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(first.getText() + " < " + second.getText());
    }
}
