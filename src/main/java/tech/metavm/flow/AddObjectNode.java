package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.AddObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.AbsClassType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("新增记录节点")
public class AddObjectNode extends NodeRT<AddObjectParamDTO> {

    public static AddObjectNode create(NodeDTO nodeDTO, IEntityContext context) {
        AddObjectParamDTO param = nodeDTO.getParam();
        ClassType type = context.getClassType(param.typeId());
        AddObjectNode node = new AddObjectNode(nodeDTO, type, context.getScope(nodeDTO.scopeId()));
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("字段")
    private final Table<FieldParam> fields = new Table<>(FieldParam.class, true);

    private AddObjectNode(NodeDTO nodeDTO, ClassType type, ScopeRT scope) {
        super(nodeDTO, type, scope);
    }

    @Override
    public AbsClassType getType() {
        return (AbsClassType) super.getType();
    }

    public List<FieldParam> getFields() {
        return fields;
    }

    @Override
    protected AddObjectParamDTO getParam(boolean persisting) {
        return new AddObjectParamDTO(
                getType().getId(),
                NncUtils.map(fields, fp -> fp.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(AddObjectParamDTO param, IEntityContext entityContext) {
//        setOutputType(context.getType(param.typeId()));
        fields.clear();
        fields.addAll(NncUtils.map(
                param.fieldParams(),
                fp -> new FieldParam(getType().getField(fp.fieldId()), fp.value(), getParsingContext(entityContext))
        ));
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(
            frame.addInstance(
                InstanceDTO.valueOf(
                    NncUtils.get(getType(), Type::getId),
                    NncUtils.map(fields, fp -> fp.evaluate(frame))
                )
            )
        );
    }

}
