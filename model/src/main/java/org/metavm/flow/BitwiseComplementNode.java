package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.BitwiseComplementNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class BitwiseComplementNode extends NodeRT {

    public static BitwiseComplementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        BitwiseComplementNode node = (BitwiseComplementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            BitwiseComplementNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var operand = ValueFactory.create(param.operand(), parsingContext);
            node = new BitwiseComplementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, operand);
        }
        return node;
    }

    private final Value operand;

    public BitwiseComplementNode(Long tmpId,
                                 @NotNull String name,
                                 @Nullable String code,
                                 @Nullable NodeRT previous,
                                 @NotNull ScopeRT scope,
                                 Value operand) {
        super(tmpId, name, code, Types.getLongType(), previous, scope);
        this.operand = operand;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitBitwiseComplementNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new BitwiseComplementNodeParam(operand.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v = (LongValue) operand.evaluate(frame);
        return next(v.bitwiseComplement());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("~" + operand.getText());
    }
}
