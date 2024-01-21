package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.CallNodeParam;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@EntityType("调用节点")
public abstract class CallNode extends NodeRT {

    public static Flow getFlow(CallNodeParam param, IEntityContext context) {
        return context.getEntity(Method.class, param.getFlowRef());
    }

    @EntityField("子流程")
    private Flow subFlow;
    @ChildEntity("参数列表")
    protected final ChildArray<Argument> arguments = addChild(new ChildArray<>(Argument.class), "arguments");

    public CallNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, @NotNull Flow subFlow, @NotNull List<Argument> arguments) {
        super(tmpId, name, code, null, prev, scope);
        this.subFlow = subFlow;
        this.arguments.addChildren(arguments);
    }

    @NotNull
    @Override
    public Type getType() {
        return subFlow.getReturnType();
    }

    public Flow getSubFlow() {
        return subFlow;
    }

    public void setSubFlow(Flow subFlow) {
        this.subFlow = subFlow;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments.resetChildren(arguments);
    }

    public List<Argument> getArguments() {
        return arguments.toList();
    }

    protected abstract @Nullable ClassInstance getSelf(MetaFrame frame);

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
        var flow = subFlow;
        var args = arguments;
        List<Instance> argInstances = new ArrayList<>();
        out:
        for (Parameter param : flow.getParameters()) {
            for (Argument arg : args) {
                if (arg.getParameter() == param) {
                    argInstances.add(arg.evaluate(frame));
                    continue out;
                }
            }
            argInstances.add(Instances.nullInstance());
        }
        var self = getSelf(frame);
        if (flow instanceof Method method && method.isInstanceMethod())
            flow = requireNonNull(self).getType().resolveMethod(method, frame.parameterizedFlowProvider());
        FlowExecResult result = flow.execute(self, argInstances,
                frame.getInstanceRepository(),
                frame.parameterizedFlowProvider());
        if (result.exception() != null)
            return frame.catchException(this, result.exception());
        else
            return next(result.ret());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(subFlow.getName() + "(" + NncUtils.join(arguments, Argument::getText, ", ") + ")");
    }
}
