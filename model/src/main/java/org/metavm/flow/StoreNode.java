package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.StoreNodeParam;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

public class StoreNode extends VariableAccessNode {

    public static StoreNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        StoreNodeParam param = nodeDTO.getParam();
        var node = (StoreNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var value = ValueFactory.create(param.value(), parsingContext);
            node = new StoreNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, param.index(), value);
        }
        return node;
    }

    private final Value value;

    public StoreNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable NodeRT previous, @NotNull ScopeRT scope, int index, Value value) {
        super(tmpId, name, code, null, previous, scope, index);
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStoreNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new StoreNodeParam(index, value.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        frame.store(index, value.evaluate(frame));
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("store " + index + " " + value.getText());
    }

    public int getIndex() {
        return index;
    }
}
