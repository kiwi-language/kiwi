package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.NullPointerExceptionNative;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;

public class NonNullNode extends NodeRT {

    public static NonNullNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (NonNullNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NonNullNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    public NonNullNode(Long tmpId, @NotNull String name,  @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNonNullNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public int execute(MetaFrame frame) {
        var inst = frame.peek();
        if(inst.isNull()) {
            var npe = ClassInstance.allocate(StdKlass.nullPointerException.type());
            var nat = new NullPointerExceptionNative(npe);
            nat.NullPointerException(frame);
            return frame.catchException(this, npe);
        }
        else
            return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("nonnull");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    @NotNull
    public Type getType() {
        return Types.getAnyType();
    }
}
