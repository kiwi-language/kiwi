package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.OutputFieldDTO;
import tech.metavm.flow.rest.ReturnParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EntityType("结束节点")
public class ReturnNode extends NodeRT<ReturnParamDTO> {

    @EntityField("字段值")
    private final Table<FieldParam> fieldParams = new Table<>();

    public ReturnNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.name(),
                NodeKind.RETURN,
                scope.getFlow().getOutputType(),
                prev,
                scope
        );
    }

    public ReturnNode(NodeDTO nodeDTO, ReturnParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getOutputType(), scope);
        setParam(param);
    }

    @Override
    protected void setParam(ReturnParamDTO param) {
        fieldParams.clear();
        Map<Long, OutputFieldDTO> outFieldMap = NncUtils.toMap(param.fields(), OutputFieldDTO::id);
        for (Field field : getOutputType().getFields()) {
            OutputFieldDTO outputFieldDTO = outFieldMap.get(field.getId());
            if(outputFieldDTO != null) {
                fieldParams.add(new FieldParam(field, outputFieldDTO.value(), getParsingContext()));
            }
        }
    }

    @Override
    protected ReturnParamDTO getParam(boolean persisting) {
        Type outputType = getOutputType();
        List<OutputFieldDTO> outputFields = new ArrayList<>();
        for (Field field : outputType.getFields()) {
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

    private Value fieldValue(Field field) {
        return fieldParams.get(FieldParam::getField, field).getValue();
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public void execute(FlowFrame frame) {
        InstanceDTO instanceDTO = new InstanceDTO(
                null,
                getOutputType().getId(),
                getOutputType().getName(),
            "临时对象",
                getFieldValues(frame),
                null
        );
        Instance instance = InstanceFactory.create(instanceDTO, frame.getStack().getContext());
        frame.ret(instance);
    }

    private List<InstanceFieldDTO> getFieldValues(FlowFrame frame) {
        List<InstanceFieldDTO> instanceFields = new ArrayList<>();
        for (Field field : getOutputType().getFields()) {
            Value value = fieldValue(field);
            instanceFields.add(
                    InstanceFieldDTO.valueOf(
                            field.getId(),
                            NncUtils.get(value, v -> v.evaluate(frame))
                    )
            );
        }
        return instanceFields;
    }

}
