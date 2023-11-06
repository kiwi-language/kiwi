package tech.metavm.flow;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.flow.rest.CallParam;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.entity.ChildArray;
import tech.metavm.util.NncUtils;
import tech.metavm.entity.ReadonlyArray;

import java.util.List;

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
        super(tmpId, name, subFlow.getReturnType(), prev, scope);
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

    public ReadonlyArray<Argument> getArguments() {
        return arguments;
    }

    protected abstract Instance getSelf(MetaFrame frame);

    @Override
    public void execute(MetaFrame frame) {
        FlowStack stack = frame.getStack();
        var self = getSelf(frame);
        var args = NncUtils.map(arguments,arg -> arg.evaluate(frame));
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
