package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.NullPointerExceptionNative;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.NonNullNodeParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NonNullNode extends NodeRT {

    public static NonNullNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NonNullNodeParam param = nodeDTO.getParam();
        var node = (NonNullNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var value = ValueFactory.create(param.value(), parsingContext);
        var outputType = Types.getNonNullType(parsingContext.getExpressionType(value.getExpression()).getCertainUpperBound());
        if (node == null)
            node = new NonNullNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, value);
        return node;
    }

    private final Value value;

    public NonNullNode(Long tmpId, @NotNull String name, @Nullable String code, Type outputType, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                       @NotNull Value value) {
        super(tmpId, name, code, outputType, previous, scope);
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNonNullNode(this);
    }

    @Override
    protected NonNullNodeParam getParam(SerializeContext serializeContext) {
        return new NonNullNodeParam(value.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var inst = value.evaluate(frame);
        if(inst.isNull()) {
            var npe = ClassInstance.allocate(StdKlass.nullPointerException.type());
            var nat = new NullPointerExceptionNative(npe);
            nat.NullPointerException(frame);
            return frame.catchException(this, npe);
        }
        else
            return next(inst);
    }

    public Value getValue() {
        return value;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getNonNullType(value.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(value.getText() + "!");
    }
}
