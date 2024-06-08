package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.expression.Expression;
import tech.metavm.expression.VarType;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.CapturedType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.Types;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@EntityType
public abstract class CallNode extends NodeRT {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private FlowRef flowRef;
    @ChildEntity
    protected final ReadWriteArray<Argument> arguments = addChild(new ReadWriteArray<>(Argument.class), "arguments");
    @ChildEntity
    protected final ReadWriteArray<Type> capturedExpressionTypes = addChild(new ReadWriteArray<>(Type.class), "capturedExpressionTypes");
    @ChildEntity
    protected final ReadWriteArray<Expression> capturedExpressions = addChild(new ReadWriteArray<>(Expression.class), "capturedExpressions");

    public CallNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope, @NotNull FlowRef flowRef,
                    @NotNull List<Argument> arguments) {
        super(tmpId, name, code, null, prev, scope);
        this.flowRef = flowRef;
        this.arguments.addAll(arguments);
    }

    public FlowRef getFlowRef() {
        return flowRef;
    }

    public void setFlowRef(FlowRef flowRef) {
        this.flowRef = flowRef;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments.reset(arguments);
    }

    public List<Argument> getArguments() {
        return arguments.toList();
    }

    public void setCapturedExpressionTypes(List<Type> capturedExpressionTypes) {
        this.capturedExpressionTypes.reset(capturedExpressionTypes);
    }

    public void setCapturedExpressions(List<Expression> capturedExpressions) {
        this.capturedExpressions.reset(capturedExpressions);
    }

    protected abstract @Nullable ClassInstance getSelf(MetaFrame frame);

    @Override
    protected String check0() {
        var argMap = NncUtils.toMap(arguments, Argument::getParameter, Function.identity());
        var targetFlow = flowRef.resolve();
        for (Parameter parameter : targetFlow.getParameters()) {
            if (parameter.getType().isNotNull() && argMap.get(parameter) == null)
                return String.format("Not null argument '%s' is not set", parameter.getName());
        }
        return null;
    }

    private Flow tryUncaptureFlow(Flow flow, MetaFrame frame) {
        if(capturedExpressions.isEmpty())
            return flow;
        var actualExprTypes = new ArrayList<>(
                NncUtils.map(capturedExpressions, exr -> exr.evaluate(frame).getType())
        );
        var capturedTypeMap = new HashMap<CapturedType, Type>();
        for (int i = 0; i < actualExprTypes.size(); i++) {
            var capturedType = capturedExpressionTypes.get(i);
            Types.extractCapturedType(capturedType, actualExprTypes.get(i), capturedTypeMap::put);
        }
        var capturedTypes = new ArrayList<CapturedType>();
        var actualCapturedTypes = new ArrayList<Type>();
        capturedTypeMap.forEach((ct, t) -> {
            capturedTypes.add(ct);
            actualCapturedTypes.add(t);
        });
        var typeSubst = new TypeSubstitutor(capturedTypes, actualCapturedTypes);
        if(flow instanceof Method method && method.getDeclaringType().isParameterized()
                && NncUtils.anyMatch(method.getDeclaringType().getTypeArguments(), Type::isCaptured)) {
            var declaringType = method.getDeclaringType();
            var actualTypeArgs = NncUtils.map(declaringType.getTypeArguments(), t -> t.accept(typeSubst));
            var actualDeclaringType = declaringType.getEffectiveTemplate().getParameterized(actualTypeArgs);
            if(DebugEnv.debugging)
                debugLogger.info("uncapture flow declaring type from {} to {}",
                        declaringType.getTypeDesc(),
                        actualDeclaringType.getTypeDesc());
            flow = NncUtils.requireNonNull(actualDeclaringType.findSelfMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getEffectiveVerticalTemplate()));
        }
        if(NncUtils.anyMatch(flow.getTypeArguments(), Type::isCaptured)) {
            var actualTypeArgs = NncUtils.map(flow.getTypeArguments(), t -> t.accept(typeSubst));
            var uncapturedFlow = Objects.requireNonNull(flow.getHorizontalTemplate()).getParameterized(actualTypeArgs);
            if(DebugEnv.debugging) {
                debugLogger.info("uncapture flow from {} to {}, captured expressions: {}, captured expression types: {}, " +
                                "actual expression types: {}, captured types: {}, actual types: {}",
                        flow.getTypeDesc(), uncapturedFlow.getTypeDesc(),
                        NncUtils.join(capturedExpressions, e -> e.build(VarType.NAME), ", "),
                        NncUtils.join(capturedExpressionTypes, Type::getTypeDesc, ", "),
                        NncUtils.join(actualExprTypes, Type::getTypeDesc, ", "),
                        NncUtils.join(capturedTypes, Type::getTypeDesc, ", "),
                        NncUtils.join(actualCapturedTypes, Type::getTypeDesc, ", ")
                );
            }
            return uncapturedFlow;
        }
        else
            return flow;
    }

    @NotNull
    @Override
    public Type getType() {
        return getFlowRef().resolve().getReturnType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var flow = flowRef.resolve();
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
        flow = tryUncaptureFlow(flow, frame);
        var self = getSelf(frame);
        if (flow instanceof Method method && method.isInstanceMethod())
            flow = requireNonNull(self).getKlass().resolveMethod(method);
        FlowExecResult result = flow.execute(self, argInstances, frame);
        if (result.exception() != null)
            return frame.catchException(this, result.exception());
        else
            return next(result.ret());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(flowRef.resolve().getName() + "(" + NncUtils.join(arguments, Argument::getText, ", ") + ")");
    }
}
