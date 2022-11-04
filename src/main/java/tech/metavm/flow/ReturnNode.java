package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnNode extends NodeRT<ReturnParamDTO> {


    private final Map<Long, Value> fieldValueMap = new HashMap<>();

    public ReturnNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                null,
                nodeDTO.name(),
                NodeType.RETURN,
                scope.getFlow().getOutputType(),
                prev,
                scope
        );
    }

    public ReturnNode(NodeDTO nodeDTO, ReturnParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getOutputType(), scope);
        setParam(param);
    }

    public ReturnNode(NodePO nodePO, ReturnParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    @Override
    protected void setParam(ReturnParamDTO param) {
        fieldValueMap.clear();
        for (OutputFieldDTO field : param.fields()) {
            fieldValueMap.put(field.id(), ValueFactory.getValue(field.value(), getParsingContext()));
        }
    }

    @Override
    protected ReturnParamDTO getParam(boolean persisting) {
        Type outputType = getOutputType();
        List<OutputFieldDTO> outputFields = new ArrayList<>();
        for (Field field : outputType.getFields()) {
            Value value = fieldValue(field.getId());
            outputFields.add(new OutputFieldDTO(
                    field.getId(),
                    field.getName(),
                    field.getType().getId(),
                    NncUtils.get(value, v -> v.toDTO(persisting))
            ));
        }
        return new ReturnParamDTO(outputFields);
    }

    private Value fieldValue(long fieldId) {
        return fieldValueMap.get(fieldId);
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
                getFieldValues(frame)
        );
        Instance instance = new Instance(instanceDTO, frame.getStack().getInstanceContext());
        frame.ret(instance);
    }

    private List<InstanceFieldDTO> getFieldValues(FlowFrame frame) {
        List<InstanceFieldDTO> instanceFields = new ArrayList<>();
        for (Field field : getOutputType().getFields()) {
            Value value = fieldValue(field.getId());
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
