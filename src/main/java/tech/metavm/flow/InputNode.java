package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

public class InputNode extends NodeRT<InputParamDTO> {

    private final Type objectType;

    public InputNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                null,
                nodeDTO.name(),
                NodeType.INPUT,
                scope.getFlow().getInputType(),
                prev,
                scope
        );
        this.objectType = getOutputType();
    }

    public InputNode(NodeDTO nodeDTO, InputParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getTypeFromContext(param.typeId()), scope);
        this.objectType = getTypeFromContext(param.typeId());
    }

    public InputNode(NodePO nodePO, InputParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        objectType = getTypeFromContext(param.typeId());
    }

    @Override
    protected void setParam(InputParamDTO param) {

    }

    @Override
    protected InputParamDTO getParam(boolean persisting) {
        return new InputParamDTO(
                objectType.getId(),
                NncUtils.map(
                        objectType.getFields(),
                        field -> toInputFieldDTO(field.toDTO())
                )
        );
    }

    private InputFieldDTO toInputFieldDTO(FieldDTO fieldDTO) {
        return new InputFieldDTO(
                fieldDTO.id(),
                fieldDTO.name(),
                fieldDTO.typeId(),
                fieldDTO.defaultValue()
        );
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(frame.getArgument());
    }
}
