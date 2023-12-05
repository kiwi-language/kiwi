package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.GetUniqueParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.Index;
import tech.metavm.entity.ChildArray;
import tech.metavm.object.type.UnionType;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("唯一索引节点")
public class GetUniqueNode extends NodeRT<GetUniqueParamDTO> {

    public static GetUniqueNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        GetUniqueParamDTO param = nodeDTO.getParam();
        Index index = context.getEntity(Index.class, param.indexId());
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var type = context.getNullableType(index.getDeclaringType());
        var values = NncUtils.map(param.values(), v -> ValueFactory.create(v, parsingContext));
        return new GetUniqueNode(nodeDTO.tmpId(), nodeDTO.name(), type, index, prev, scope, values);
    }

    @EntityField("索引")
    private Index index;
    @ChildEntity("字段值列表")
    private final ChildArray<Value> values = addChild(new ChildArray<>(Value.class), "values");

    public GetUniqueNode(Long tmpId, String name, UnionType type, Index index, NodeRT<?> previous, ScopeRT scope, List<Value> values) {
        super(tmpId, name,  type, previous, scope);
        this.index = index;
        this.values.addChildren(values);
    }

    @Override
    protected GetUniqueParamDTO getParam(boolean persisting) {
        return new GetUniqueParamDTO(
                index.getIdRequired(),
                NncUtils.map(values, value -> value.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(GetUniqueParamDTO param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        setValues(
                NncUtils.map(
                        param.values(), v -> ValueFactory.create(v, parsingContext)
                )
        );
    }

    public void setValues(List<Value> values) {
        this.values.resetChildren(values);
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        IInstanceContext instanceContext = frame.getContext();
        var result = instanceContext.selectByUniqueKey(buildIndexKey(frame));
        if(result == null)
            result = InstanceUtils.nullInstance();
        return next(result);
    }

    private IndexKeyRT buildIndexKey(MetaFrame frame) {
        return index.createIndexKey(NncUtils.map(values, fp -> fp.evaluate(frame)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetUniqueNode(this);
    }
}
