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

import java.util.List;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexSelectNode extends NodeRT {

    public static IndexSelectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var param = (IndexSelectNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
        var node = (IndexSelectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new IndexSelectNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, index);
        return node;
    }

    private Index index;

    public IndexSelectNode(Long tmpId, String name, NodeRT previous, ScopeRT scope,
                           Index index) {
        super(tmpId, name, null, previous, scope);
        this.index = index;
    }

    @Override
    protected IndexSelectNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectNodeParam(
                serializeContext.getStringId(index)
        );
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return new ClassType(StdKlass.arrayList.get(), List.of(index.getDeclaringType().getType()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + index.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - index.getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT);
        output.writeConstant(index.getRef());
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
