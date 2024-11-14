package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.AddObjectNodeParam;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class AddObjectNode extends NodeRT {

    public static AddObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        AddObjectNodeParam param = nodeDTO.getParam();
        var klass = ((ClassType) TypeParser.parseType(param.getType(), context)).resolve();
        AddObjectNode node = (AddObjectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(),  param.isEphemeral(), klass.getType(), prev, scope);
        return node;
    }

    private boolean ephemeral;

    public AddObjectNode(Long tmpId, String name, boolean ephemeral, ClassType type, NodeRT prev,
                         ScopeRT scope) {
        super(tmpId, name, type, prev, scope);
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    protected AddObjectNodeParam getParam(SerializeContext serializeContext) {
        return new AddObjectNodeParam(getType().toExpression(serializeContext), ephemeral);
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        throw new UnsupportedOperationException();
    }

    public Klass getKlass() {
        return getType().resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("allocate " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 1 - getKlass().getAllFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.ADD_OBJECT);
        output.writeConstant(getType());
        output.writeBoolean(ephemeral);
    }

    @Override
    public int getLength() {
        return 4;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public NodeRT getSuccessor() {
        return Objects.requireNonNull(super.getSuccessor());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddObjectNode(this);
    }

}
