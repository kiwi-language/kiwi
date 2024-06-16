package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.IndexScanNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Index;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexScanNode extends NodeRT {

    public static IndexScanNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexScanNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = new ArrayType(index.getDeclaringType().getType(), ArrayKind.READ_ONLY);
        var node = (IndexScanNode) context.getNode(Id.parse(nodeDTO.id()));
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

    private Index index;
    private IndexQueryKey from;
    private IndexQueryKey to;

    public IndexScanNode(Long tmpId, String name, @Nullable String code, ArrayType type, NodeRT previous, ScopeRT scope, Index index,
                         IndexQueryKey from, IndexQueryKey to) {
        super(tmpId, name, code, type, previous, scope);
        this.index = index;
        this.from = from;
        this.to = to;
    }

    @Override
    protected IndexScanNodeParam getParam(SerializeContext serializeContext) {
        return new IndexScanNodeParam(
                serializeContext.getStringId(index),
                from.toDTO(serializeContext),
                to.toDTO(serializeContext)
        );
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) requireNonNull(super.getType());
    }

    public void setFrom(IndexQueryKey from) {
        this.from = from;
    }

    public void setTo(IndexQueryKey to) {
        this.to = to;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var result = frame.instanceRepository().indexScan(from.buildIndexKey(frame), to.buildIndexKey(frame));
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
