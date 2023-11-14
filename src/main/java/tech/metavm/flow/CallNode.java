package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.flow.rest.CallParam;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
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

    public void onReturn(Instance returnValue, MetaFrame frame) {
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

    protected abstract Instance getSelf(MetaFrame frame);

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
    public void execute(MetaFrame frame) {
        FlowStack stack = frame.getStack();
        var self = getSelf(frame);
        List<Instance> args = new ArrayList<>();
        var argMap = NncUtils.toMap(arguments, Argument::getParameter, Function.identity());
        for (Parameter param : subFlow.getParameters()) {
            var arg = argMap.get(param);
            if (arg != null)
                args.add(arg.evaluate(frame));
            else
                args.add(InstanceUtils.nullInstance());
        }
        var flow = subFlow;
        if (flow.isAbstract()) {
            flow = ((ClassType) self.getType()).getOverrideFlowRequired(flow);
        }
        if (flow.isNative()) {
            var result = NativeInvoker.invoke(flow, self, args);
            onReturn(result, frame);
            if (result != null) {
                frame.setResult(result);
            }
        } else {
            stack.push(new MetaFrame(flow, self, args, stack));
        }
    }

}
