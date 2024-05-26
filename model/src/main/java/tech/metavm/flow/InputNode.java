package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.InputFieldDTO;
import tech.metavm.flow.rest.InputNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@EntityType("输入节点")
public class InputNode extends ChildTypeNode {

    public static InputNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var node = (InputNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var klass = ((ClassType) TypeParser.parseType(nodeDTO.outputType(), context)).resolve();
            node = new InputNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), klass, prev, scope);
        }
        return node;
    }

    public InputNode(Long tmpId, String name, @Nullable String code, @NotNull Klass type, NodeRT prev, ScopeRT scope) {
        super(tmpId, name, code, type, prev, scope);
    }

    @Override
    protected InputNodeParam getParam(SerializeContext serializeContext) {
        return new InputNodeParam(NncUtils.map(getKlass().getReadyFields(), this::toInputFieldDTO));
    }

    private InputFieldDTO toInputFieldDTO(Field field) {
        try (var serContext = SerializeContext.enter()) {
            return new InputFieldDTO(
                    serContext.getStringId(field),
                    field.getName(),
                    field.getType().toExpression(serContext),
                    NncUtils.get(field.getDefaultValue(), Instance::toFieldValueDTO),
                    NncUtils.get(getFieldCondition(field), Value::toDTO)
            );
        }
    }

    @Nullable
    public Value getFieldCondition(Field field) {
        var klass = getKlass();
        var constraints = klass.getFieldCheckConstraints(field);
        if (constraints.isEmpty()) {
            return null;
        } else {
            return constraints.get(0).getCondition();
        }
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        try(var ignored = ContextUtil.getProfiler().enter("InputNode.execute")) {
            Map<Field, Instance> fieldValues = new HashMap<>();
            NncUtils.biForEach(getKlass().getReadyFields(), frame.getArguments(), fieldValues::put);
            return next(ClassInstance.create(fieldValues, getType()));
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("input");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInputNode(this);
    }
}
