package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.flow.rest.AddObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("新增记录节点")
public class AddObjectNode extends NodeRT<AddObjectParamDTO> {

    public static AddObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        AddObjectParamDTO param = nodeDTO.getParam();
        ClassType type = context.getClassType(param.typeId());
        AddObjectNode node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("字段")
    private final Table<FieldParam> fields = new Table<>(FieldParam.class, true);

    public AddObjectNode(Long tmpId, String name, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, type, prev, scope);
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
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

    public void setField(long fieldId, Value value) {
        fields.get(FieldParam::getId, fieldId).setValue(value);
    }

    public void setField(Field field, Expression value) {
        // TODO to implement
    }

    @Override
    protected void setParam(AddObjectParamDTO param, IEntityContext context) {
//        setOutputType(context.getType(param.typeId()));
        fields.clear();
        fields.addAll(NncUtils.map(
                param.fieldParams(),
                fp -> new FieldParam(context.getField(fp.fieldRef()), fp.value(), getParsingContext(context))
        ));
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(
            frame.addInstance(
                    new ClassInstance(
                            NncUtils.toMap(
                                    fields,
                                    FieldParam::getField,
                                    fp -> fp.evaluate(frame)
                            ),
                            getType()
                    )
                )
        );
    }

}
