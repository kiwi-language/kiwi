package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.ArrayExpression;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NewArrayNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.TypeParser;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("创建数组节点")
public class NewArrayNode extends NodeRT implements NewNode {

    public static NewArrayNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        NewArrayNodeParam param = nodeDTO.getParam();
        var type = (ArrayType) TypeParser.parseType(nodeDTO.outputType(), context);
        var value = NncUtils.get(param.value(), v -> ValueFactory.create(v, parsingContext));
        var parentRef = param.parentRef() != null ?
                ParentRef.create(param.parentRef(), parsingContext, context, type) : null;
        NewArrayNode node = (NewArrayNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setValue(value);
            node.setParentRef(parentRef);
        } else
            node = new NewArrayNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, value, parentRef, prev, scope);
        return node;
    }

    @EntityField("值")
    @Nullable
    private Value value;

    @EntityField("父引用")
    @Nullable
    private ParentRef parentRef;

    public NewArrayNode(Long tmpId, String name,
                        @Nullable String code,
                        ArrayType type,
                        @Nullable Value value,
                        @Nullable ParentRef parentRef,
                        NodeRT previous,
                        ScopeRT scope) {
        super(tmpId, name, code, type, previous, scope);
        this.parentRef = parentRef;
        this.value = value;
    }

    @Override
    protected NewArrayNodeParam getParam(SerializeContext serializeContext) {
        return new NewArrayNodeParam(
                NncUtils.get(value, Value::toDTO),
                NncUtils.get(parentRef, ParentRef::toDTO)
        );
    }

    public void setValue(@Nullable Value value) {
        this.value = value;
    }

    private Value check(Value value) {
        if (getType().isChildArray()) {
            var valueExpr = value.getExpression();
            NncUtils.requireTrue(valueExpr instanceof ArrayExpression arrayExpr
                    && arrayExpr.getExpressions().isEmpty());
        } else {
            NncUtils.requireTrue(value.getType() instanceof ArrayType);
            var valueType = (ArrayType) value.getType();
            NncUtils.requireTrue(getType().getElementType().isAssignableFrom(valueType.getElementType()));
        }
        return value;
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var instParentRef = NncUtils.get(parentRef, ref -> ref.evaluate(frame));
        // TODO support ephemeral
        var array = new ArrayInstance(getType(), instParentRef);
        if (!array.isChildArray() && value != null)
            array.addAll((ArrayInstance) value.evaluate(frame));
        return next(array);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName() + "(");
        if (value != null)
            writer.write(value.getText());
        writer.write(")");
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }

    public @Nullable Value getValue() {
        return value;
    }

    @Override
    public void setParentRef(@Nullable ParentRef parentRef) {
        this.parentRef = parentRef;
    }

    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayNode(this);
    }
}
