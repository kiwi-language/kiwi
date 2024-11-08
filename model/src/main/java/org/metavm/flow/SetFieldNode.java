package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetFieldNodeParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldRef;

import javax.annotation.Nullable;

@EntityType
public class SetFieldNode extends NodeRT {

    public static SetFieldNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        SetFieldNodeParam param = nodeDTO.getParam();
        var node = (SetFieldNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            ParsingContext parsingContext = FlowParsingContext.create(scope, prev, context);
            var objectId = ValueFactory.create(param.objectId(), parsingContext);
            var fieldRef = FieldRef.create(param.fieldRef(), context);
            var value = ValueFactory.create(param.value(), parsingContext);
            node = new SetFieldNode(
                    nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, objectId, fieldRef, value);
        }
        return node;
    }

    private final Value object;
    private final FieldRef fieldRef;
    private final Value value;

    public SetFieldNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, Value object, FieldRef fieldRef, Value value) {
        super(tmpId, name, code, null, prev, scope);
        this.object = object;
        this.fieldRef = fieldRef;
        this.value = value;
    }

    public Value getObject() {
        return object;
    }

    @Override
    protected SetFieldNodeParam getParam(SerializeContext serializeContext) {
        return new SetFieldNodeParam(
                object.toDTO(),
                fieldRef.toDTO(serializeContext),
                value.toDTO()
        );
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        ClassInstance instance = object.evaluate(frame).resolveObject();
        instance.setField(fieldRef.resolve(), value.evaluate(frame));
        return next(null);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setField " + object.getText() + " " + fieldRef.getRawField().getName() + " "
                + value.getText());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetFieldNode(this);
    }
}
