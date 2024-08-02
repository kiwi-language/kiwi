package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.InputFieldDTO;
import org.metavm.flow.rest.InputNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeParser;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@EntityType
public class InputNode extends ChildTypeNode {

    public static final Logger logger = LoggerFactory.getLogger(InputNode.class);

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
                    NncUtils.get(field.getDefaultValue(), Value::toFieldValueDTO),
                    NncUtils.get(getFieldCondition(field), org.metavm.flow.Value::toDTO)
            );
        }
    }

    @Nullable
    public org.metavm.flow.Value getFieldCondition(Field field) {
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
            Map<Field, Value> fieldValues = new HashMap<>();
            NncUtils.biForEach(getKlass().getReadyFields(), frame.getArguments(), fieldValues::put);
            return next(ClassInstance.create(fieldValues, getType()).getReference());
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
