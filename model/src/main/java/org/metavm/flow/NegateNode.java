package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NegateNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NumberValue;

import javax.annotation.Nullable;

public class NegateNode extends NodeRT {

    public static NegateNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NegateNode node = (NegateNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            NegateNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var operand = ValueFactory.create(param.operand(), parsingContext);
            node = new NegateNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, operand);
        }
        return node;
    }

    private final Value operand;

    public NegateNode(Long tmpId,
                      @NotNull String name,
                      @Nullable String code,
                      @Nullable NodeRT previous,
                      @NotNull ScopeRT scope,
                      Value operand) {
        super(tmpId, name, code, operand.getType(), previous, scope);
        this.operand = operand;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNegateNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new NegateNodeParam(operand.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v = (NumberValue) operand.evaluate(frame);
        return next(v.negate());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("-" + operand.getText());
    }
}
