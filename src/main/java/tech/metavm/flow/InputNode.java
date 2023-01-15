package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("输入节点")
public class InputNode extends NodeRT<InputParamDTO> {

    public InputNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.name(),
                NodeKind.INPUT,
                scope.getFlow().getInputType(),
                prev,
                scope
        );
    }

    public InputNode(NodeDTO nodeDTO, ScopeRT scope) {
        super(nodeDTO, scope.getFlow().getInputType(), scope);
    }

    @Override
    protected void setParam(InputParamDTO param, IEntityContext entityContext) {

    }

    @Override
    protected InputParamDTO getParam(boolean persisting) {
        return new InputParamDTO(
                getType().getId(),
                NncUtils.map(
                        getType().getFields(),
                        field -> toInputFieldDTO(field.toDTO())
                )
        );
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
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
    public void execute(FlowFrame frame) {
        frame.setResult(frame.getArgument());
    }
}
