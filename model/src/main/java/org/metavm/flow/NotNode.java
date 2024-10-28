package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.NotNodeParam;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NotNode extends NodeRT {

    public static NotNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NotNode node = (NotNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            NotNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var operand = ValueFactory.create(param.operand(), parsingContext);
            node = new NotNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, operand);
        }
        return node;
    }

    private final Value operand;

    public NotNode(Long tmpId,
                   @NotNull String name,
                   @Nullable String code,
                   @Nullable NodeRT previous,
                   @NotNull ScopeRT scope,
                   Value operand) {
        super(tmpId, name, code, Types.getBooleanType(), previous, scope);
        this.operand = operand;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNodeNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new NotNodeParam(operand.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v = (BooleanValue) operand.evaluate(frame);
        return next(v.not());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("!" + operand.getText());
    }

    public Value getOperand() {
        return operand;
    }

}
