package tech.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.UpdateStaticNodeParam;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("更新静态字段节点")
public class UpdateStaticNode extends NodeRT {

    public static final Logger logger = LoggerFactory.getLogger(UpdateStaticNode.class);

    public static UpdateStaticNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        UpdateStaticNodeParam param = nodeDTO.getParam();
        var node = (UpdateStaticNode) context.getNode(Id.parse(nodeDTO.id()));
        var type = context.getKlass(Id.parse(param.typeId()));
        if (node == null)
            node = new UpdateStaticNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, type, List.of());
        else
            node.setKlass(type);
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        node.setFields(
                NncUtils.map(
                        param.fields(),
                        field -> new UpdateField(
                                field.getStaticField(type).getRef(),
                                UpdateOp.getByCode(field.opCode()),
                                ValueFactory.create(field.value(), parsingContext)
                        )
                )
        );
        return node;
    }

    @EntityField("类型")
    private Klass klass;

    @ChildEntity("字段列表")
    private final ChildArray<UpdateField> fields = addChild(new ChildArray<>(UpdateField.class), "fields");

    public UpdateStaticNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, Klass klass, List<UpdateField> fields) {
        super(tmpId, name, code, null, previous, scope);
        this.klass = klass;
        setFields(fields);
    }

    @Override
    protected UpdateStaticNodeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            return new UpdateStaticNodeParam(
                    serContext.getStringId(klass),
                    NncUtils.map(fields, UpdateField::toDTO)
            );
        }
    }

    public void setUpdateField(Field field, UpdateOp op, Value value) {
        NncUtils.requireTrue(field.isStatic());
        //noinspection DuplicatedCode
        var updateField = fields.get(UpdateField::getFieldRef, field);
        if (updateField == null) {
            updateField = new UpdateField(field.getRef(), op, value);
            fields.addChild(updateField);
        } else {
            updateField.setOp(op);
            updateField.setValue(value);
        }
    }

    public List<UpdateField> getFields() {
        return fields.toList();
    }

    public void setKlass(Klass klass) {
        this.klass = klass;
        this.fields.clear();
    }

    public void setFields(List<UpdateField> fields) {
        for (UpdateField field : fields) {
            if(!field.getFieldRef().getDeclaringType().isAssignableFrom(klass.getType())) {
                throw new InternalException("Field " + field.getFieldRef().resolve() + " is not defined in klass " + klass);
            }
        }
        this.fields.resetChildren(fields);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        for (UpdateField field : fields) {
            field.execute(null, frame, Flows.isConstructor(getFlow()));
        }
        return next(null);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("update(" + klass.getName() + ", {"
                + NncUtils.join(fields, UpdateField::getText, ", ") + "}");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUpdateStaticNode(this);
    }
}
