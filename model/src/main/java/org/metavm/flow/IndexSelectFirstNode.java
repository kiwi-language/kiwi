
package org.metavm.flow;

import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.IndexSelectFirstNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Index;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType
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

    private Index index;
    private IndexQueryKey key;

    public IndexSelectFirstNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                                Index index, IndexQueryKey key) {
        super(tmpId, name, code, index.getDeclaringType().getType(), previous, scope);
        this.index = index;
        this.key = key;
    }

    @Override
    protected IndexSelectFirstNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectFirstNodeParam(
                serializeContext.getStringId(index),
                key.toDTO(serializeContext)
        );
    }

    public void setKey(IndexQueryKey key) {
        this.key = key;
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
