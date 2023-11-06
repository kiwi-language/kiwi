package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ReturnParamDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("结束节点")
public class ReturnNode extends NodeRT<ReturnParamDTO> {

    public static ReturnNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ReturnNode node = new ReturnNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

    public static Callable getCallable(ScopeRT scope) {
        var owner = scope.getOwner();
        while (owner != null) {
            if(owner instanceof Callable callable) {
                return callable;
            }
            owner = owner.getScope().getOwner();
        }
        return scope.getFlow();
    }

    @ChildEntity("结果")
    private @Nullable Value value;

    public ReturnNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, getCallable(scope).getReturnType(), prev, scope);
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
        Instance instance = value != null ? value.evaluate(frame) : null;
        frame.ret(instance);
    }

    @Override
    @NotNull
    public Type getType() {
        return NncUtils.requireNonNull(super.getType());
    }

    @Override
    public boolean isExit() {
        return true;
    }

}
