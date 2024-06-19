package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.ReturnNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class ReturnNode extends NodeRT {

    public static ReturnNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext entityContext) {
        ReturnNode node = (ReturnNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        var param = (ReturnNodeParam) nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, entityContext);
        var value = param.value() != null ? ValueFactory.create(param.value(), parsingContext) : null;
        if (node == null)
            node = new ReturnNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, value);
        else
            node.setValue(value);
        return node;
    }

    private @Nullable Value value;

    public ReturnNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, @Nullable Value value) {
        super(tmpId, name, code, null, prev, scope);
        this.value = value;
    }

    public void setValue(@Nullable Value value) {
        this.value = value;
    }

    @Nullable
    public Value getValue() {
        return value;
    }

    @Override
    protected ReturnNodeParam getParam(SerializeContext serializeContext) {
        return new ReturnNodeParam(
                NncUtils.get(value, Value::toDTO)
        );
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var retValue = getType().isVoid() ? Instances.nullInstance() : Objects.requireNonNull(value).evaluate(frame);
        return NodeExecResult.ret(retValue);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("return");
        if (value != null)
            writer.write(" " + value.getText());
    }

    @Override
    protected String check0() {
        var callable = getEnclosingCallable();
        if (!callable.getReturnType().isVoid()) {
            if (value == null)
                return "Return value is not set";
            else if (!callable.getReturnType().isAssignableFrom(getExpressionTypes().getType(value.getExpression())))
                return "Invalid return value";
        }
        return null;
    }

    @Override
    @NotNull
    public Type getType() {
        return getEnclosingCallable().getReturnType();
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitReturnNode(this);
    }
}
