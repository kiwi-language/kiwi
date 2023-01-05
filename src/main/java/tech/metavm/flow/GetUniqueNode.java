package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.flow.rest.GetUniqueParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.meta.IndexConstraintRT;
import tech.metavm.object.meta.TypeUtil;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("唯一索引节点")
public class GetUniqueNode extends NodeRT<GetUniqueParamDTO> {

    public static GetUniqueNode create(NodeDTO nodeDTO, IEntityContext context) {
        GetUniqueParamDTO param = nodeDTO.getParam();
        IndexConstraintRT constraint = context.getEntity(IndexConstraintRT.class, param.constraintId());
        GetUniqueNode node = new GetUniqueNode(nodeDTO, constraint, context.getScope(nodeDTO.scopeId()));
        node.setParam(param, context);
        return node;
    }

    @EntityField("索引")
    private IndexConstraintRT constraint;
    @EntityField("字段值列表")
    private final Table<Value> values = new Table<>(Value.class);

    public GetUniqueNode(NodeDTO nodeDTO, IndexConstraintRT constraint, ScopeRT scope) {
        super(nodeDTO, TypeUtil.getNullableType(constraint.getDeclaringType()), scope);
        this.constraint = constraint;
    }

    @Override
    protected GetUniqueParamDTO getParam(boolean persisting) {
        return new GetUniqueParamDTO(
                constraint.getId(),
                NncUtils.map(values, value -> value.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(GetUniqueParamDTO param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        setValues(
                NncUtils.map(
                        param.values(), v -> ValueFactory.getValue(v, parsingContext)
                )
        );
    }

    public void setValues(List<Value> values) {
        this.values.clear();
        this.values.addAll(values);
    }

    public void setConstraint(IndexConstraintRT constraint) {
        this.constraint = constraint;
    }

    @Override
    public void execute(FlowFrame frame) {
        InstanceContext instanceContext = frame.getStack().getContext();
        frame.setResult(
                instanceContext.selectByUniqueKey(buildIndexKey(frame))
        );
    }

    private IndexKeyPO buildIndexKey(FlowFrame frame) {
        return new IndexKeyPO(
                constraint.getId(),
                NncUtils.map(
                        values,
                        fp -> IndexKeyPO.getIndexColumn(fp.evaluate(frame))
                )
        );
    }

}
