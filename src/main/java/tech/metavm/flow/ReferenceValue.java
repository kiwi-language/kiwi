package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;

public class ReferenceValue extends Value {

    private final long nodeId;
    private final long fieldId;

    public ReferenceValue(ValueDTO valueDTO) {
        super(valueDTO);
        String value = (String) valueDTO.value();
        if(value.contains("-")) {
            String[] splits = value.split("-");
            nodeId = Long.parseLong(splits[0]);
            fieldId = Long.parseLong(splits[1]);
        }
        else {
            nodeId = Long.parseLong(value);
            fieldId = -1L;
        }
    }

    @Override
    protected Object getDTOValue() {
        if(fieldId >= 0L) {
            return nodeId + "-" + fieldId;
        }
        else {
            return nodeId + "";
        }
    }

    @Override
    public Object evaluate(FlowFrame frame) {
        if(fieldId >= 0) {
            return frame.getResult(nodeId, fieldId);
        }
        else {
            return frame.getResult(nodeId);
        }
    }

}
