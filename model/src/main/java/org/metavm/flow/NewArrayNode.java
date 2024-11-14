package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.TypeParser;
import org.metavm.util.NncUtils;

@EntityType
public class NewArrayNode extends NodeRT {

    public static NewArrayNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        NewArrayNode node = (NewArrayNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var type = (ArrayType) TypeParser.parseType(nodeDTO.outputType(), context);
            node = new NewArrayNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, scope);
        }
        return node;
    }

    public NewArrayNode(Long tmpId, String name,
                        ArrayType type,
                        NodeRT previous,
                        ScopeRT scope) {
        super(tmpId, name, type, previous, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_ARRAY);
        output.writeConstant(getType());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayNode(this);
    }
}
