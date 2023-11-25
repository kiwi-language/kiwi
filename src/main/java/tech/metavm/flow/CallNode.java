package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.flow.rest.CallParam;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType("调用节点")
public abstract class CallNode<T extends CallParam> extends NodeRT<T> {

    public static Flow getFlow(CallParam param, IEntityContext context) {
        return context.getEntity(Flow.class, param.getFlowRef());
    }

    @ChildEntity("参数列表")
    protected final ChildArray<Argument> arguments = addChild(new ChildArray<>(Argument.class), "arguments");
    @EntityField("子流程")
    protected Flow subFlow;

    public CallNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope, List<Argument> arguments,
                    Flow subFlow) {
        super(tmpId, name, null, prev, scope);
        this.arguments.addChildren(arguments);
        this.subFlow = subFlow;
    }

    @Override
    protected void setParam(T param, IEntityContext context) {
        setCallParam(param, context);
    }

    protected void setCallParam(CallParam param, IEntityContext context) {
        subFlow = context.getFlow(param.getFlowRef());
        if (param.getArguments() != null) {
            arguments.clear();
            for (ArgumentDTO(Long tmpId, RefDTO paramRef, ValueDTO value) : param.getArguments()) {
                Parameter parameter = context.getEntity(Parameter.class, paramRef);
                arguments.addChild(new Argument(tmpId, parameter,
                        ValueFactory.create(value, getParsingContext(context))));
            }
        }
        setExtraParam(param, context);
    }

    protected void setExtraParam(CallParam paramDTO, IEntityContext context) {
    }

    @NotNull
    @Override
    public Type getType() {
        return subFlow.getReturnType();
    }

    @Override
    protected abstract T getParam(boolean persisting);

    public Flow getSubFlow() {
        return subFlow;
    }

    public void setSubFlow(Flow flow) {
        this.subFlow = flow;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments.resetChildren(arguments);
    }

    public List<Argument> getArguments() {
        return arguments.toList();
    }

    protected abstract @Nullable Instance getSelf(MetaFrame frame);

    @Override
    protected String check0() {
        var argMap = NncUtils.toMap(arguments, Argument::getParameter, Function.identity());
        for (Parameter parameter : subFlow.getParameters()) {
            if (parameter.getType().isNotNull() && argMap.get(parameter) == null)
                return String.format("必填参数'%s'未配置", parameter.getName());
        }
        return null;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var self = getSelf(frame);
        List<Instance> argInstances = new ArrayList<>();
        var flow = subFlow;
        var args = arguments;
        out: for (Parameter param : flow.getParameters()) {
            for (Argument arg : args) {
                if(arg.getParameter() == param) {
                    argInstances.add(arg.evaluate(frame));
                    continue out;
                }
            }
            argInstances.add(InstanceUtils.nullInstance());
        }
        if(!flow.isStatic())
            flow = ((ClassType) Objects.requireNonNull(self).getType())
                    .resolveFlow(flow, frame.getEntityContext());
        FlowExecResult result = flow.execute(self, argInstances, frame.getContext());
        if(result.exception() != null)
            return frame.catchException(this, result.exception());
        else
            return next(result.ret());
    }

}
