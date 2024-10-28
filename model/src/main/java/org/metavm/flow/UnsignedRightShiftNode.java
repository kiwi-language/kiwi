package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.UnsignedRightShiftNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class UnsignedRightShiftNode extends NodeRT {

    public static UnsignedRightShiftNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        UnsignedRightShiftNode node = (UnsignedRightShiftNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            UnsignedRightShiftNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.first(), parsingContext);
            var second = ValueFactory.create(param.second(), parsingContext);
            node = new UnsignedRightShiftNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, second);
        }
        return node;
    }

    private final Value first;
    private final Value second;

    public UnsignedRightShiftNode(Long tmpId,
                                  @NotNull String name,
                                  @Nullable String code,
                                  @Nullable NodeRT previous,
                                  @NotNull ScopeRT scope,
                                  Value first,
                                  Value second) {
        super(tmpId, name, code, Types.getLongType(), previous, scope);
        this.first = first;
        this.second = second;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnsignedRightShift(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new UnsignedRightShiftNodeParam(first.toDTO(), second.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v1 = (LongValue) first.evaluate(frame);
        var v2 = (LongValue) second.evaluate(frame);
        return next(v1.unsignedRightShift(v2));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(first.getText() + " >>> " + second.getText());
    }
}
