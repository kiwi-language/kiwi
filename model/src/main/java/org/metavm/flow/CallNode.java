package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.CapturedType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.DebugEnv;
import org.metavm.util.LinkedList;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@EntityType
public abstract class CallNode extends NodeRT {

    public static final Logger logger = LoggerFactory.getLogger(CallNode.class);

    private FlowRef flowRef;
    @ChildEntity
    protected final ReadWriteArray<Type> capturedVariableTypes = addChild(new ReadWriteArray<>(Type.class), "capturedVariableTypes");
    @ChildEntity
    protected final ReadWriteArray<Long> capturedVariableIndexes = addChild(new ReadWriteArray<>(Long.class), "capturedVariableIndexes");

    public CallNode(Long tmpId, String name, NodeRT prev, ScopeRT scope, @NotNull FlowRef flowRef) {
        super(tmpId, name, null, prev, scope);
        this.flowRef = flowRef;
    }

    public FlowRef getFlowRef() {
        return flowRef;
    }

    public void setFlowRef(FlowRef flowRef) {
        this.flowRef = flowRef;
    }

    public void setCapturedVariableTypes(List<Type> capturedVariableTypes) {
        this.capturedVariableTypes.reset(capturedVariableTypes);
    }

    public void setCapturedVariableIndexes(List<Long> capturedVariableIndexes) {
        this.capturedVariableIndexes.reset(capturedVariableIndexes);
    }

    protected abstract @Nullable ClassInstance getSelf(MetaFrame frame);

    private Flow tryUncaptureFlow(Flow flow, MetaFrame frame) {
        if(capturedVariableIndexes.isEmpty())
            return flow;
        var actualExprTypes = new ArrayList<>(NncUtils.map(capturedVariableIndexes, i -> frame.getVariableType(i.intValue())));
        var capturedTypeMap = new HashMap<CapturedType, Type>();
        for (int i = 0; i < actualExprTypes.size(); i++) {
            var capturedType = capturedVariableTypes.get(i);
            Types.extractCapturedType(capturedType, actualExprTypes.get(i), capturedTypeMap::put);
        }
        // TODO Create a constructor in TypeSubstitutor that accepts a Map
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
                logger.info("uncapture flow declaring type from {} to {}",
                        declaringType.getTypeDesc(),
                        actualDeclaringType.getTypeDesc());
            flow = NncUtils.requireNonNull(actualDeclaringType.findSelfMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getEffectiveVerticalTemplate()));
        }
        if(NncUtils.anyMatch(flow.getTypeArguments(), Type::isCaptured)) {
            var actualTypeArgs = NncUtils.map(flow.getTypeArguments(), t -> t.accept(typeSubst));
            var uncapturedFlow = Objects.requireNonNull(flow.getHorizontalTemplate()).getParameterized(actualTypeArgs);
            if(DebugEnv.debugging) {
                logger.info("uncapture flow from {} to {}, captured expressions: {}, captured expression types: {}, " +
                                "actual expression types: {}, captured types: {}, actual types: {}",
                        flow.getTypeDesc(), uncapturedFlow.getTypeDesc(),
                        NncUtils.join(capturedVariableIndexes, Object::toString, ", "),
                        NncUtils.join(capturedVariableTypes, Type::getTypeDesc, ", "),
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

    @Override
    public Type getType() {
        var type = getFlowRef().resolve().getReturnType();
        return type.isVoid() ? null : type;
    }

    @Override
    public int execute(MetaFrame frame) {
        var flow = flowRef.resolve();
        var args = new LinkedList<Value>();
        int numArgs = flow.getParameters().size();
        for (int i = 0; i < numArgs; i++) {
            args.addFirst(frame.pop());
        }
        flow = tryUncaptureFlow(flow, frame);
        var self = getSelf(frame);
        if (flow instanceof Method method && method.isVirtual())
            flow = requireNonNull(self).getKlass().resolveMethod(method);
        FlowExecResult result = flow.execute(self, args, frame);
        if (result.exception() != null)
            return frame.catchException(this, result.exception());
        else {
            if(result.ret() != null)
                frame.push(result.ret());
            return MetaFrame.STATE_NEXT;
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("invoke " + flowRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        var flow = flowRef.resolve();
        if(flow.getReturnType().isVoid())
            return -flow.getInputCount();
        else
            return 1 - flow.getInputCount();
    }
}
