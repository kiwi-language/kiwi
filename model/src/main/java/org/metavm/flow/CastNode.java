package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.CastNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class CastNode extends NodeRT {

    public static CastNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (CastNodeParam) nodeDTO.param();
        var node = (CastNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var object = ValueFactory.create(param.object(), parsingContext);
        var type = TypeParser.parseType(nodeDTO.outputType(), context);
        if (node == null)
            node = new CastNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope, object);
        else {
            node.setOutputType(type);
            node.setValue(object);
        }
        return node;
    }

    private @NotNull Value object;

    public CastNode(Long tmpId, String name, @Nullable String code, @NotNull Type outputType,
                    NodeRT previous, ScopeRT scope, @NotNull Value object) {
        super(tmpId, name, code, outputType, previous, scope);
        this.object = object;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCastNode(this);
    }

    @Override
    protected CastNodeParam getParam(SerializeContext serializeContext) {
        return new CastNodeParam(object.toDTO());
    }

    public @NotNull Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    public void setValue(@NotNull Value object) {
        this.object = object;
    }

    @Override
    protected void setOutputType(Type outputType) {
        super.setOutputType(Objects.requireNonNull(outputType));
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var inst = object.evaluate(frame);
        var type = getType();
        if (type.isInstance(inst))
            return next(inst);
        else {
            var exception = ClassInstance.allocate(StandardTypes.getExceptionKlass().getType());
            var exceptionNative = new ExceptionNative(exception);
            exceptionNative.Exception(Instances.stringInstance(
                    String.format("Can not cast instance '%s' to type '%s'", inst.getTitle(), type.getName())
            ), frame);
            return NodeExecResult.exception(exception);
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write( object.getText() + " as " + getType().getName());
    }
}
