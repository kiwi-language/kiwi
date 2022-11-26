package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.InstanceContext;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.GetUniqueParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("唯一索引节点")
public class GetUniqueNode extends NodeRT<GetUniqueParamDTO> {

    private UniqueConstraintRT constraint;
    private Table<Value> values;

    public GetUniqueNode(NodeDTO nodeDTO, UniqueConstraintRT constraint, ScopeRT scope) {
        super(nodeDTO, constraint.getDeclaringType().getNullableType(), scope);
        setParam(nodeDTO.getParam());
    }

    @Override
    protected GetUniqueParamDTO getParam(boolean persisting) {
        return new GetUniqueParamDTO(
                constraint.getId(),
                new Table<>(NncUtils.map(values, value -> value.toDTO(persisting)))
        );
    }

    @Override
    protected void setParam(GetUniqueParamDTO param) {
//        constraint = getContext().getUniqueConstraint(param.constraintId());
//        values = NncUtils.map(
//                param.values(),
//                valueDTO -> ValueFactory.getValue(valueDTO, getParsingContext())
//        );
    }

    public void setValues(List<ValueDTO> values) {
        this.values = new Table<>(
                NncUtils.map(
                    values,
                    valueDTO -> ValueFactory.getValue(valueDTO, getParsingContext())
                )
        );
    }

    public void setConstraint(UniqueConstraintRT constraint) {
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
