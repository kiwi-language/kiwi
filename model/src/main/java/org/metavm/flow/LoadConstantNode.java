package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.LoadConstantNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class LoadConstantNode extends NodeRT {

    public static LoadConstantNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var param = (LoadConstantNodeParam) nodeDTO.getParam();
        var node = (LoadConstantNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new LoadConstantNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope,
                    Instances.fromFieldValue(param.value(), id -> context.getInstanceContext().get(id).getReference()));
        }
        return node;
    }

    private final Value value;

    public LoadConstantNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope, Value value) {
        super(tmpId, name, null, previous, scope);
        this.value = value;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadConstantNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LoadConstantNodeParam(value.toFieldValueDTO());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ldc " + value.getText());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD_CONSTANT);
        output.writeConstant(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    @NotNull
    public Type getType() {
        return value.getType();
    }
}
