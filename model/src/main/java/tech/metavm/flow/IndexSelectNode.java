package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.ListNative;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.IndexSelectNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Index;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexSelectNode extends NodeRT {

    public static IndexSelectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var param = (IndexSelectNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = new ClassType(StandardTypes.getReadWriteListKlass(), List.of(index.getDeclaringType().getType()));
        var key = IndexQueryKey.create(param.key(), context, parsingContext);
        var node = (IndexSelectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setIndex(index);
            node.setKey(key);
        } else
            node = new IndexSelectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, prev, scope, index, key);
        return node;
    }

    private Index index;
    private IndexQueryKey key;

    public IndexSelectNode(Long tmpId, String name, @Nullable String code, ClassType type, NodeRT previous, ScopeRT scope,
                           Index index, IndexQueryKey key) {
        super(tmpId, name, code, type, previous, scope);
        this.index = index;
        this.key = key;
    }

    @Override
    protected IndexSelectNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectNodeParam(
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
    @NotNull
    public ClassType getType() {
        return requireNonNull((ClassType) super.getType());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var result = frame.instanceRepository().indexSelect(key.buildIndexKey(frame));
        var list = ClassInstance.allocate(getType());
        var listNative = new ListNative(list);
        listNative.List(frame);
        result.forEach(e -> listNative.add(e, frame));
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
