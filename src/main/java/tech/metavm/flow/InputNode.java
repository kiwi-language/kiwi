package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

@EntityType("输入节点")
public class InputNode extends ChildTypeNode<InputParamDTO> {

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
        return new InputParamDTO(NncUtils.map(getType().getReadyFields(), this::toInputFieldDTO));
    }

    private InputFieldDTO toInputFieldDTO(Field field) {
        try (var serContext = SerializeContext.enter()) {
            return new InputFieldDTO(
                    serContext.getRef(field),
                    field.getName(),
                    serContext.getRef(field.getType()),
                    NncUtils.get(field.getDefaultValue(), Instance::toFieldValueDTO),
                    NncUtils.get(getFieldCondition(field), v -> v.toDTO(false))
            );
        }
    }

    @Nullable
    public Value getFieldCondition(Field field) {
        var type = getType();
        var constraints = type.getFieldCheckConstraints(field);
        if (constraints.isEmpty()) {
            return null;
        } else {
            return constraints.get(0).getCondition();
        }
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        Map<Field, Instance> fieldValues = new HashMap<>();
        NncUtils.biForEach(getType().getReadyFields(), frame.getArguments(), fieldValues::put);
        return next(new ClassInstance(fieldValues, getType()));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInputNode(this);
    }
}
