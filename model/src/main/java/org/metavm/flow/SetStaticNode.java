package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetStaticNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

@EntityType
public class SetStaticNode extends NodeRT {

    public static final Logger logger = LoggerFactory.getLogger(SetStaticNode.class);

    public static SetStaticNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        SetStaticNodeParam param = nodeDTO.getParam();
        var node = (SetStaticNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var fieldRef = FieldRef.create(param.fieldRef(), context);
            var value = ValueFactory.create(param.value(), parsingContext);
            node = new SetStaticNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope,  fieldRef, value);
        }
        return node;
    }

    private final FieldRef fieldRef;
    private final Value value;

    public SetStaticNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, FieldRef fieldRef, Value value) {
        super(tmpId, name, code, null, previous, scope);
        this.fieldRef = fieldRef;
        this.value = value;
    }

    @Override
    protected SetStaticNodeParam getParam(SerializeContext serializeContext) {
        return new SetStaticNodeParam(fieldRef.toDTO(serializeContext), value.toDTO());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var field = fieldRef.resolve();
        var sft = StaticFieldTable.getInstance(field.getDeclaringType(), ContextUtil.getEntityContext());
        sft.set(field, value.evaluate(frame));
        return next(null);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setStatic " + fieldRef.resolve().getQualifiedName() + " " + value.getText());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetStaticNode(this);
    }
}
