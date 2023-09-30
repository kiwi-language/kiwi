package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateFieldDTO;
import tech.metavm.flow.rest.UpdateStaticParamDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

public class UpdateStaticNode extends NodeRT<UpdateStaticParamDTO> {

    public static UpdateStaticNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        UpdateStaticParamDTO param = nodeDTO.getParam();
        var node = new UpdateStaticNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope,
                context.getClassType(param.typeRef()));
        node.setParam(param, context);
        return node;
    }

    @SuppressWarnings("FieldCanBeLocal")
    @ChildEntity("更新类型")
    private ClassType type;

    @ChildEntity("更新字段")
    private final Table<UpdateField> fields = new Table<>(UpdateField.class, true);

    public UpdateStaticNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, ClassType type) {
        super(tmpId, name, null, previous, scope);
        this.type = type;
    }

    @Override
    protected UpdateStaticParamDTO getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new UpdateStaticParamDTO(
                    context.getRef(type),
                    NncUtils.map(fields, f -> f.toDTO(persisting))
            );
        }
    }

    @Override
    protected void setParam(UpdateStaticParamDTO param, IEntityContext context) {
        if (param.typeRef() != null) {
            type = context.getClassType(param.typeRef());
        }
        if (param.fields() != null) {
            for (UpdateFieldDTO field : param.fields()) {
                NncUtils.requireTrue(context.getField(field.fieldRef()).getDeclaringType() == type);
            }
            fields.clear();
            var parsingContext = getParsingContext(context);
            fields.addAll(
                    NncUtils.map(
                            param.fields(),
                            field -> new UpdateField(
                                    context.getField(field.fieldRef()),
                                    UpdateOp.getByCode(field.opCode()),
                                    ValueFactory.create(field.value(), parsingContext)
                            )
                    )
            );
        }
    }

    public void setUpdateField(Field field, UpdateOp op, Value value) {
        NncUtils.requireTrue(field.isStatic());
        //noinspection DuplicatedCode
        var updateField = fields.get(UpdateField::getField, field);
        if (updateField == null) {
            updateField = new UpdateField(field, op, value);
            fields.add(updateField);
        } else {
            updateField.setOp(op);
            updateField.setValue(value);
        }
    }

    public ClassType getUpdateType() {
        return type;
    }

    public List<UpdateField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public void execute(FlowFrame frame) {
        for (UpdateField field : fields) {
            field.execute(null, frame, getFlow().isConstructor(), frame.getStack().getContext());
        }
    }
}
