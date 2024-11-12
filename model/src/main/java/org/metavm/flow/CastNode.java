package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.Instances;

import java.util.Objects;

@EntityType
public class CastNode extends NodeRT {

    public static CastNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (CastNode) context.getNode(Id.parse(nodeDTO.id()));
        var type = TypeParser.parseType(nodeDTO.outputType(), context);
        if (node == null)
            node = new CastNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, scope);
        return node;
    }

    public CastNode(Long tmpId, String name, @NotNull Type outputType,
                    NodeRT previous, ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCastNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    public @NotNull Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    protected void setOutputType(Type outputType) {
        super.setOutputType(Objects.requireNonNull(outputType));
    }

    @Override
    public int execute(MetaFrame frame) {
        var inst = frame.pop();
        var type = getType();
        if (type.isInstance(inst)) {
            frame.push(inst);
            return MetaFrame.STATE_NEXT;
        } else if(type.isConvertibleFrom(inst.getType())) {
            frame.push(type.convert(inst));
            return MetaFrame.STATE_NEXT;
        } else {
            var exception = ClassInstance.allocate(StdKlass.exception.get().getType());
            var exceptionNative = new ExceptionNative(exception);
            exceptionNative.Exception(Instances.stringInstance(
                    String.format("Can not cast instance '%s' to type '%s'", inst.getTitle(), type.getName())
            ), frame);
            return frame.catchException(this, exception);
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write( "cast " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }
}
