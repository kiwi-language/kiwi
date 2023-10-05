package tech.metavm.flow;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.flow.rest.CallParamDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

@EntityType("调用节点")
public abstract class CallNode<T extends CallParamDTO> extends NodeRT<T> {

    public static Flow getFlow(CallParamDTO param, IEntityContext context) {
        return context.getEntity(Flow.class, param.getFlowRef());
    }

    @ChildEntity("参数列表")
    protected final Table<Argument> arguments = new Table<>(Argument.class, true);
    @EntityField("子流程")
    protected Flow subFlow;

    public CallNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope, List<Argument> arguments,
                    Flow subFlow) {
        super(tmpId, name, subFlow.getReturnType(), prev, scope);
        this.arguments.addAll(arguments);
        this.subFlow = subFlow;
    }

    @Override
    protected void setParam(T param, IEntityContext context) {
        setCallParam(param, context);
    }

    protected void setCallParam(CallParamDTO param, IEntityContext context) {
        ParsingContext parsingContext = getParsingContext(context);
        subFlow = context.getFlow(param.getFlowRef());
//        ClassType inputType = subFlow.getInputType();
        if (param.getArguments() != null) {
            for (ArgumentDTO(Long tmpId, RefDTO paramRef, ValueDTO value) : param.getArguments()) {
                Parameter parameter = context.getEntity(Parameter.class, paramRef);
//                NncUtils.requireTrue(param.getDeclaringType() == subFlow);
                arguments.add(new Argument(tmpId, parameter,
                        ValueFactory.create(value, getParsingContext(context))));
            }
        }
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
        this.arguments.clear();
        this.arguments.addAll(arguments);
    }

    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    protected abstract Instance getSelf(FlowFrame frame);

    @Override
    public void execute(FlowFrame frame) {
        FlowStack stack = frame.getStack();
        var self = getSelf(frame);
        var args = NncUtils.map(arguments,arg -> arg.evaluate(frame));
        var flow = subFlow;
        if (flow.isAbstract()) {
            flow = ((ClassType) self.getType()).getOverrideFlowRequired(flow);
        }
        if (flow.isNative()) {
            var result = NativeInvoker.invoke(flow, self, args);
            if (result != null) {
                frame.setResult(result);
            }
        } else {
            FlowFrame newFrame = new FlowFrame(flow, self, args, stack);
            stack.push(newFrame);
        }
    }

}
