package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.IndexScanNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.Index;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType("索引扫描节点")
public class IndexScanNode extends NodeRT {

    public static IndexScanNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexScanNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, param.indexRef()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = context.getArrayType(index.getDeclaringType(), ArrayKind.READ_ONLY);
        var node = (IndexScanNode) context.getNode(nodeDTO.getRef());
        var from = IndexQueryKey.create(param.from(), context, parsingContext);
        var to = IndexQueryKey.create(param.to(), context, parsingContext);
        if (node != null) {
            node.setIndex(index);
            node.setFrom(from);
            node.setTo(to);
        } else
            node = new IndexScanNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope, index, from, to);
        return node;
    }

    @EntityField("索引")
    private Index index;
    @ChildEntity("最小值")
    private IndexQueryKey from;
    @ChildEntity("最大值")
    private IndexQueryKey to;

    public IndexScanNode(Long tmpId, String name, @Nullable String code, ArrayType type, NodeRT previous, ScopeRT scope, Index index,
                         IndexQueryKey from, IndexQueryKey to) {
        super(tmpId, name, code, type, previous, scope);
        this.index = index;
        this.from = addChild(from, "from");
        this.to = addChild(to, "to");
    }

    @Override
    protected IndexScanNodeParam getParam(SerializeContext serializeContext) {
        return new IndexScanNodeParam(
                serializeContext.getRef(index),
                from.toDTO(serializeContext),
                to.toDTO(serializeContext)
        );
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) requireNonNull(super.getType());
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
        var result = frame.getInstanceRepository().indexScan(from.buildIndexKey(frame), to.buildIndexKey(frame));
        return next(new ArrayInstance(getType(), result));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexQuery(" + index.getName() + ", " +
                "[" + from.getText() + "," + to.getText() + "]" + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexQueryNode(this);
    }
}
