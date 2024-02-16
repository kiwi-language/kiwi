package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.entity.natives.ListNative;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.IndexSelectNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Index;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType("索引查询节点")
public class IndexSelectNode extends NodeRT {

    public static IndexSelectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexSelectNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, param.indexRef()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = context.getReadWriteListType(index.getDeclaringType());
        var key = IndexQueryKey.create(param.key(), context, parsingContext);
        var node = (IndexSelectNode) context.getNode(nodeDTO.getRef());
        if (node != null) {
            node.setIndex(index);
            node.setKey(key);
        } else
            node = new IndexSelectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope, index, key);
        return node;
    }

    @EntityField("索引")
    private Index index;
    @ChildEntity("键")
    private IndexQueryKey key;

    public IndexSelectNode(Long tmpId, String name, @Nullable String code, ClassType type, NodeRT previous, ScopeRT scope,
                           Index index, IndexQueryKey key) {
        super(tmpId, name, code, type, previous, scope);
        this.index = index;
        this.key = addChild(key, "key");
    }

    @Override
    protected IndexSelectNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectNodeParam(
                serializeContext.getRef(index),
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
        var result = frame.getInstanceRepository().indexSelect(key.buildIndexKey(frame));
        var list = ClassInstance.allocate(getType());
        var listNative = new ListNative(list);
        listNative.List();
        result.forEach(listNative::add);
        return next(list);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + index.getName() + ", " + key.getText() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectNode(this);
    }
}
