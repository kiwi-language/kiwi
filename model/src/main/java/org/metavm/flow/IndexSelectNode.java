package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.IndexSelectNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;

import java.util.List;

@EntityType
public class IndexSelectNode extends NodeRT {

    public static IndexSelectNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexSelectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexSelectNodeParam) nodeDTO.param();
            var indexRef = IndexRef.create(param.indexRef(), context);
            node = new IndexSelectNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, indexRef);
        }
        return node;
    }

    private final IndexRef indexRef;

    public IndexSelectNode(Long tmpId, String name, NodeRT previous, Code code,
                           IndexRef indexRef) {
        super(tmpId, name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    protected IndexSelectNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectNodeParam(indexRef.toDTO(serializeContext));
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    @NotNull
    public ClassType getType() {
        return new ClassType(StdKlass.arrayList.get(), List.of(getIndex().getDeclaringType().getType()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - getIndex().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectNode(this);
    }
}
