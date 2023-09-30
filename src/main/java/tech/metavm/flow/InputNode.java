package tech.metavm.flow;

import tech.metavm.autograph.Parameter;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("输入节点")
public class InputNode extends NodeRT<InputParamDTO> {

    public static InputNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        return new InputNode(nodeDTO, prev, scope);
    }

    public InputNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.tmpId(),
                nodeDTO.name(),
                scope.getFlow().getInputType(),
                prev,
                scope
        );
    }

    public InputNode(Long tmpId, String name, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, type, prev, scope);
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

    public List<Parameter> getParameters() {
        return NncUtils.map(
                getType().getFields(),
                field -> new Parameter(field.getName(), field.getCode(), field.getType())
        );
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    private InputFieldDTO toInputFieldDTO(FieldDTO fieldDTO) {
        return new InputFieldDTO(
                fieldDTO.getRef(),
                fieldDTO.name(),
                fieldDTO.typeRef(),
                fieldDTO.defaultValue()
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(frame.getArgument());
    }
}
