
package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.IndexSelectFirstNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Index;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType("索引首记录查询节点")
public class IndexSelectFirstNode extends NodeRT {

    public static IndexSelectFirstNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexSelectFirstNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var key = IndexQueryKey.create(param.key(), context, parsingContext);
        var node = (IndexSelectFirstNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setIndex(index);
            node.setKey(key);
        } else
            node = new IndexSelectFirstNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, index, key);
        return node;
    }

    @EntityField("索引")
    private Index index;
    @ChildEntity("键")
    private IndexQueryKey key;

    public IndexSelectFirstNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                                Index index, IndexQueryKey key) {
        super(tmpId, name, code, index.getDeclaringType().getType(), previous, scope);
        this.index = index;
        this.key = addChild(key, "key");
    }

    @Override
    protected IndexSelectFirstNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectFirstNodeParam(
                serializeContext.getId(index),
                key.toDTO(serializeContext)
        );
    }

    public void setKey(IndexQueryKey key) {
        this.key = addChild(key, "key");
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public ClassType getType() {
        return requireNonNull((ClassType) super.getType());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var result = frame.instanceRepository().selectFirstByKey(key.buildIndexKey(frame));
        return next(NncUtils.orElse(result, Instances.nullInstance()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelectFirst(" + index.getName() + ", " + key.getText() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectFirstNode(this);
    }
}
