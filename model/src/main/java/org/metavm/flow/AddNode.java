package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.AddNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class AddNode extends NodeRT {

    public static AddNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        AddNode node = (AddNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            AddNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.first(), parsingContext);
            var second = ValueFactory.create(param.second(), parsingContext);
            node = new AddNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, second);
        }
        return node;
    }

    private final Value first;
    private final Value second;

    public AddNode(Long tmpId,
                   @NotNull String name,
                   @Nullable String code,
                   @Nullable NodeRT previous,
                   @NotNull ScopeRT scope,
                   Value first,
                   Value second) {
        super(tmpId, name, code, Types.getCompatibleType(first.getType(), second.getType()), previous, scope);
        this.first = first;
        this.second = second;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new AddNodeParam(first.toDTO(), second.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var v1 = (NumberValue) first.evaluate(frame);
        var v2 = (NumberValue) second.evaluate(frame);
        return next(v1.add(v2));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(first.getText() + " + " + second.getText());
    }
}
