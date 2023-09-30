package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.GetUniqueParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.meta.Index;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("唯一索引节点")
public class GetUniqueNode extends NodeRT<GetUniqueParamDTO> {

    public static GetUniqueNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        GetUniqueParamDTO param = nodeDTO.getParam();
        Index constraint = context.getEntity(Index.class, param.constraintId());
        return new GetUniqueNode(nodeDTO.tmpId(), nodeDTO.name(), constraint, prev, scope);
    }

    @EntityField("索引")
    private Index constraint;
    @ChildEntity("字段值列表")
    private final Table<Value> values = new Table<>(Value.class, true);

    public GetUniqueNode(Long tmpId, String name, Index index, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name,  index.getDeclaringType(), previous, scope);
        this.constraint = index;
    }

    @Override
    protected GetUniqueParamDTO getParam(boolean persisting) {
        return new GetUniqueParamDTO(
                constraint.getIdRequired(),
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
        this.values.clear();
        this.values.addAll(values);
    }

    public void setConstraint(Index constraint) {
        this.constraint = constraint;
    }

    public Index getConstraint() {
        return constraint;
    }

    @Override
    public void execute(FlowFrame frame) {
        IInstanceContext instanceContext = frame.getStack().getContext();
        frame.setResult(
                instanceContext.selectByUniqueKey(buildIndexKey(frame))
        );
    }

    private IndexKeyRT buildIndexKey(FlowFrame frame) {
        return constraint.createIndexKey(NncUtils.map(values, fp -> fp.evaluate(frame)));
    }

}
