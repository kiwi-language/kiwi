package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EntityType("输入节点")
public class InputNode extends NodeRT<InputParamDTO> {

    public static InputNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        return new InputNode(nodeDTO, context.getClassType(nodeDTO.outputTypeRef()), prev, scope);
    }

    public InputNode(NodeDTO nodeDTO, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.tmpId(),
                nodeDTO.name(),
                type,
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
    protected List<Object> nodeBeforeRemove() {
        return List.of(getType());
    }

    @Override
    public void execute(FlowFrame frame) {
        Map<Field, Instance> fieldValues = new HashMap<>();
        NncUtils.biForEach(getType().getFields(), frame.getArguments(), fieldValues::put);
        var instance = new ClassInstance(fieldValues, getType());
        frame.setResult(instance);
    }
}
