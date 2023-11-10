package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ReturnParamDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("结束节点")
public class ReturnNode extends NodeRT<ReturnParamDTO> {

    public static ReturnNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ReturnNode node = new ReturnNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    @ChildEntity("结果")
    private @Nullable Value value;

    public ReturnNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
    }

    public void setValue(@Nullable Value value) {
        this.value = value;
    }

    @Override
    protected void setParam(ReturnParamDTO param, IEntityContext context) {
        if(param.value() != null) {
            value = ValueFactory.create(param.value(), getParsingContext(context));
        }
    }

    @Nullable
    public Value getValue() {
        return value;
    }

    @Override
    protected ReturnParamDTO getParam(boolean persisting) {
        return new ReturnParamDTO(
                NncUtils.get(value, v -> v.toDTO(persisting))
        );
    }

    @Override
    public void execute(MetaFrame frame) {
        if(!getType().isVoid()) {
            frame.ret(Objects.requireNonNull(value).evaluate(frame));
        }
    }

    @Override
    protected String check0() {
        var callable = getEnclosingCallable();
        if(!callable.getReturnType().isVoid()) {
            if(value == null)
                return "未配置返回结果";
            else if(!callable.getReturnType().isAssignableFrom(value.getType()))
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
