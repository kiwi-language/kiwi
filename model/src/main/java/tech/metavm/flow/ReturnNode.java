package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ReturnNodeParam;
import tech.metavm.object.type.Type;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("返回节点")
public class ReturnNode extends NodeRT {

    public static ReturnNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext entityContext) {
        ReturnNode node = (ReturnNode) entityContext.getNode(nodeDTO.getRef());
        var param = (ReturnNodeParam) nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, entityContext);
        var value = param.value() != null ? ValueFactory.create(param.value(), parsingContext) : null;
        if (node == null)
            node = new ReturnNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, value);
        else
            node.setValue(value);
        return node;
    }

    @ChildEntity("结果")
    @Nullable
    private Value value;

    public ReturnNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, @Nullable Value value) {
        super(tmpId, name, code, null, prev, scope);
        this.value = NncUtils.get(value, v -> addChild(v, "value"));
    }

    public void setValue(@Nullable Value value) {
        this.value = NncUtils.get(value, v -> addChild(v, "value"));
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
        try(var ignored = ContextUtil.getProfiler().enter("ReturnNode.execute")) {
            var retValue = getType().isVoid() ? null : Objects.requireNonNull(value).evaluate(frame);
            return NodeExecResult.ret(retValue);
        }
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
                return "未配置返回结果";
            else if (!callable.getReturnType().isAssignableFrom(value.getType()))
                return "返回结果错误";
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
