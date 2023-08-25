package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.expression.Expression;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.OutputFieldDTO;
import tech.metavm.flow.rest.ReturnParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.ClassInstanceParamDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EntityType("结束节点")
public class ReturnNode extends NodeRT<ReturnParamDTO> {

    public static ReturnNode create(NodeDTO nodeDTO, IEntityContext entityContext) {
        NodeRT<?> prev = NncUtils.get(nodeDTO.prevId(), entityContext::getNode);
        ScopeRT scope = entityContext.getScope(nodeDTO.scopeId());
        ReturnNode node = new ReturnNode(nodeDTO, prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @EntityField("字段值列表")
    private final Table<FieldParam> fieldParams = new Table<>(FieldParam.class);

    public ReturnNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.name(),
                NodeKind.RETURN,
                scope.getFlow().getOutputType(),
                prev,
                scope
        );
    }

    public ReturnNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getOutputType(), scope);
    }

    public ReturnNode(String name, NodeRT<?> prev, ScopeRT scope) {
        super(name, NodeKind.RETURN, scope.getFlow().getOutputType(), prev, scope);
    }

    @Override
    protected void setParam(ReturnParamDTO param, IEntityContext entityContext) {
        fieldParams.clear();
        Map<Long, OutputFieldDTO> outFieldMap = NncUtils.toMap(param.fields(), OutputFieldDTO::id);
        ParsingContext parsingContext = getParsingContext(entityContext);
        for (Field field : getType().getFields()) {
            OutputFieldDTO outputFieldDTO = outFieldMap.get(field.getId());
            if(outputFieldDTO != null) {
                fieldParams.add(new FieldParam(field, outputFieldDTO.value(), parsingContext));
            }
        }
    }

    @Override
    protected ReturnParamDTO getParam(boolean persisting) {
        ClassType type = getType();
        List<OutputFieldDTO> outputFields = new ArrayList<>();
        for (Field field : type.getFields()) {
            Value value = fieldValue(field);
            outputFields.add(new OutputFieldDTO(
                    field.getId(),
                    field.getName(),
                    field.getType().getId(),
                    NncUtils.get(value, v -> v.toDTO(persisting))
            ));
        }
        return new ReturnParamDTO(outputFields);
    }

    public void setField(Field field, Expression value) {
        var param = fieldParams.get(FieldParam::getField, field);
        if(param == null) {
            param = new FieldParam(field, new ExpressionValue(value));
            fieldParams.add(param);
        }
        else param.setValue(new ExpressionValue(value));
    }

    private Value fieldValue(Field field) {
        return NncUtils.get(
                fieldParams.get(FieldParam::getField, field),
                FieldParam::getValue
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        InstanceDTO instanceDTO = new InstanceDTO(
                null,
                getType().getId(),
                getType().getName(),
            "临时对象",
                new ClassInstanceParamDTO(
                        getFieldValues(frame)
                )
        );
        Instance instance = InstanceFactory.create(instanceDTO, frame.getStack().getContext());
        frame.ret(instance);
    }

    private List<InstanceFieldDTO> getFieldValues(FlowFrame frame) {
        List<InstanceFieldDTO> instanceFields = new ArrayList<>();
        for (Field field : getType().getFields()) {
            Value value = fieldValue(field);
            instanceFields.add(
                    InstanceFieldDTO.valueOf(
                            field.getId(),
                            NncUtils.get(value, v -> v.evaluate(frame).toFieldValueDTO())
                    )
            );
        }
        return instanceFields;
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }
}
