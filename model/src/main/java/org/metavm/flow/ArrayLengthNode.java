package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.ArrayLengthNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class ArrayLengthNode extends NodeRT {

    public static ArrayLengthNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        ArrayLengthNode node = (ArrayLengthNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            ArrayLengthNodeParam param = nodeDTO.getParam();
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var array = ValueFactory.create(param.array(), parsingContext);
            node = new ArrayLengthNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, array);
        }
        return node;
    }

    private final Value array;

    public ArrayLengthNode(Long tmpId,
                           @NotNull String name,
                           @Nullable String code,
                           @Nullable NodeRT previous,
                           @NotNull ScopeRT scope,
                           Value array) {
        super(tmpId, name, code, Types.getLongType(), previous, scope);
        this.array = array;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayLengthNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new ArrayLengthNodeParam(array.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var a = array.evaluate(frame).resolveArray();
        return next(Instances.longInstance(a.length()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(array.getText() + ".length");
    }
}
