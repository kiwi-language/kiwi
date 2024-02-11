package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.IndexCountNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.type.Index;
import tech.metavm.util.Instances;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType("索引查询节点")
public class IndexCountNode extends NodeRT {

    public static IndexCountNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexCountNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, param.indexRef()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var node = (IndexCountNode) context.getNode(nodeDTO.getRef());
        var min = IndexQueryKey.create(param.min(), context, parsingContext);
        var max = IndexQueryKey.create(param.min(), context, parsingContext);
        if (node != null) {
            node.setIndex(index);
            node.setFrom(min);
            node.setTo(max);
        } else
            node = new IndexCountNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, index, min, max);
        return node;
    }

    @EntityField("索引")
    private Index index;
    @ChildEntity("最小值")
    private IndexQueryKey from;
    @ChildEntity("最大值")
    private IndexQueryKey to;

    public IndexCountNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, Index index,
                          IndexQueryKey from, IndexQueryKey to) {
        super(tmpId, name, code, StandardTypes.getLongType(), previous, scope);
        this.index = index;
        this.from = addChild(from, "from");
        this.to = addChild(to, "to");
    }

    @Override
    protected IndexCountNodeParam getParam(SerializeContext serializeContext) {
        return new IndexCountNodeParam(
                serializeContext.getRef(index),
                from.toDTO(serializeContext),
                to.toDTO(serializeContext)
        );
    }

    public void setFrom(IndexQueryKey from) {
        this.from = addChild(from, "from");
    }

    public void setTo(IndexQueryKey to) {
        this.to = addChild(to, "to");
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var count = frame.getInstanceRepository().indexCount(
                from.buildIndexKey(frame), to.buildIndexKey(frame)
        );
        return next(Instances.longInstance(count));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + index.getName() + ", " +
                "[" + from.getText() + "," + to.getText() + "]" + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexCountNode(this);
    }
}
