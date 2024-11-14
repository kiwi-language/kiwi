package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.LoadNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;

import javax.annotation.Nullable;
import java.util.Objects;

@Slf4j
public class LoadNode extends VariableAccessNode {

    public static LoadNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        LoadNodeParam param = nodeDTO.getParam();
        var node = (LoadNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var type = TypeParser.parseType(nodeDTO.outputType(), context);
            node = new LoadNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, code, param.index());
        }
        return node;
    }

    public LoadNode(Long tmpId, @NotNull String name, Type outputType,
                    @Nullable NodeRT previous, @NotNull Code code, int index) {
        super(tmpId, name, outputType, previous, code, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new LoadNodeParam(index, getType().toExpression(serializeContext));
    }

    @Override
    @NotNull
    public Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("load " + index);
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOAD);
        output.writeShort(index);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public int getIndex() {
        return index;
    }
}
