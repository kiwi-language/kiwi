package org.metavm.flow;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.GetUniqueNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Index;
import org.metavm.object.type.UnionType;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class GetUniqueNode extends NodeRT {

    public static GetUniqueNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        GetUniqueNodeParam param = nodeDTO.getParam();
        Index index = context.getEntity(Index.class, Id.parse(param.indexId()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = StandardTypes.getNullableType(index.getDeclaringType().getType());
        var values = NncUtils.map(param.values(), v -> ValueFactory.create(v, parsingContext));
        GetUniqueNode node = (GetUniqueNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setIndex(index);
            node.setValues(values);
        } else
            node = new GetUniqueNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, index, prev, scope, values);
        return node;
    }

    private Index index;
    @ChildEntity
    private final ReadWriteArray<Value> values = addChild(new ReadWriteArray<>(Value.class), "values");

    public GetUniqueNode(Long tmpId, String name, @Nullable String code, UnionType type, Index index, NodeRT previous, ScopeRT scope, List<Value> values) {
        super(tmpId, name, code, type, previous, scope);
        this.index = index;
        this.values.addAll(values);
    }

    @Override
    protected GetUniqueNodeParam getParam(SerializeContext serializeContext) {
        return new GetUniqueNodeParam(
                index.getStringId(),
                NncUtils.map(values, Value::toDTO)
        );
    }

    public void setValues(List<Value> values) {
        this.values.reset(values);
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        Instance result = frame.instanceRepository().selectFirstByKey(buildIndexKey(frame));
        if (result == null)
            result = Instances.nullInstance();
        return next(result);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getUnique(" + index.getName() + ", " +
                NncUtils.join(values, Value::getText, ", ") + ")");
    }

    private IndexKeyRT buildIndexKey(MetaFrame frame) {
        return index.createIndexKey(NncUtils.map(values, fp -> fp.evaluate(frame)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetUniqueNode(this);
    }
}
