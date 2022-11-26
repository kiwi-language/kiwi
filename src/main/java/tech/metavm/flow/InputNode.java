package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

@EntityType("输入节点")
public class InputNode extends NodeRT<InputParamDTO> {

    @EntityField("类型")
    private final Type objectType;

    public InputNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.name(),
                NodeKind.INPUT,
                scope.getFlow().getInputType(),
                prev,
                scope
        );
        this.objectType = getOutputType();
    }

    public InputNode(NodeDTO nodeDTO, Type type, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getInputType(), scope);
        this.objectType = type;
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
