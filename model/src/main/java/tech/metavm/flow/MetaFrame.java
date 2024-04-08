package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

public class MetaFrame implements EvaluationContext, Frame, CallContext {

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    @Nullable
    private final ClassInstance self;
    private final List<Instance> arguments;
    @Nullable
    private final ClassType owner;
    private final Map<NodeRT, Instance> outputs = new HashMap<>();
    private final Set<LoopNode> loopingNodes = new IdentitySet<>();
    private NodeRT entry;
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final CompositeTypeFacade compositeTypeFacade;
    private final InstanceRepository instanceRepository;
    private final Map<BranchNode, Branch> selectedBranches = new IdentityHashMap<>();
    private final Map<BranchNode, Branch> exitBranches = new IdentityHashMap<>();
    private final LinkedList<Branch> branches = new LinkedList<>();
    private FrameState state = FrameState.RUNNING;
    private final LinkedList<TryNode> tryNodes = new LinkedList<>();
    private ClassInstance exception;

    private final Map<TryNode, ExceptionInfo> exceptions = new IdentityHashMap<>();

    public MetaFrame(NodeRT entry, @Nullable ClassType owner, @Nullable ClassInstance self, List<Instance> arguments,
                     InstanceRepository instanceRepository,
                     ParameterizedFlowProvider parameterizedFlowProvider,
                     CompositeTypeFacade compositeTypeFacade) {
        this.entry = entry;
        this.owner = owner;
        this.self = self;
        this.arguments = arguments;
        this.instanceRepository = instanceRepository;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.compositeTypeFacade = compositeTypeFacade;
    }

    public Instance getOutput(NodeRT node) {
        return outputs.get(node);
    }

    public void setOutput(NodeRT node, Instance output) {
        this.outputs.put(node, output);
    }

    public boolean isLooping(LoopNode loopNode) {
        return loopingNodes.contains(loopNode);
    }

    public void enterLoop(LoopNode loopNode) {
        loopingNodes.add(loopNode);
    }

    public void exitLoop(LoopNode loopNode) {
        loopingNodes.remove(loopNode);
    }

    public Instance addInstance(DurableInstance instance) {
        instanceRepository.bind(instance);
        return instance;
    }

    public void enterTrySection(TryNode tryNode) {
        tryNodes.push(tryNode);
    }

    public TryNode exitTrySection() {
        return tryNodes.pop();
    }

    @SuppressWarnings("unused")
    public boolean inTrySection() {
        return !tryNodes.isEmpty();
    }

    @SuppressWarnings("unused")
    public TryNode currentTrySection() {
        return NncUtils.requireNonNull(tryNodes.peek());
    }

    public NodeExecResult catchException(@NotNull NodeRT raiseNode, @NotNull ClassInstance exception) {
        var tryNode = tryNodes.peek();
        if(tryNode != null) {
            exceptions.put(tryNode, new ExceptionInfo(raiseNode, exception));
            return NodeExecResult.jump(tryNode.getSuccessor());
        }
        else
            return NodeExecResult.exception(exception);
    }

    @SuppressWarnings("unused")
    private void exception(ClassInstance exception, boolean fromResume) {
        if(!tryNodes.isEmpty()) {
            var tryNode = tryNodes.peek();
            exceptions.put(tryNode, new ExceptionInfo(entry, exception));
            if(fromResume) {
                entry = tryNode.getSuccessor();
            }
            else {
                jumpTo(tryNode.getSuccessor());
            }
        }
        else {
            state = FrameState.EXCEPTION;
            this.exception = exception;
        }
    }

    public @Nullable ExceptionInfo getExceptionInfo(TryNode tryNode) {
        return exceptions.get(tryNode);
    }

    public void deleteInstance(DurableInstance instance) {
        instanceRepository.remove(instance);
    }

    @SuppressWarnings("unused")
    public Object getInstanceField(ClassInstance instance, Field field) {
        checkAccess(field);
        return instance.getField(field);
    }

    private void checkAccess(Field field) {
        if (field.getAccess() == Access.PUBLIC)
            return;
        if (field.getAccess() == Access.PACKAGE)
            return;
        if (field.getAccess() == Access.PRIVATE) {
            if (!field.getDeclaringType().equals(owner))
                throw BusinessException.illegalAccess();
        }
    }

    public static final int MAX_STEPS = 100000;

    public @NotNull FlowExecResult execute() {
        var outputs = this.outputs;
        var pc = entry;
        for(int i = 0; i < MAX_STEPS; i++) {
            var result = pc.execute(this);
            if(result.exception() != null)
                return new FlowExecResult(null, result.exception());
            var output = result.output();
            if(result.next() == null)
                return new FlowExecResult(output, null);
            if(output != null)
                outputs.put(pc, output);
            pc = result.next();
        }
        throw new FlowExecutionException(String.format("流程执行步骤超出限制: %d", MAX_STEPS));
    }

    @SuppressWarnings("unused")
    private void checkResult(Instance result, NodeRT node) {
        Type outputType = node.getType();
        if (outputType == null || outputType.isVoid()) {
            if (result != null) {
                throw new InternalException("Node " + node + " can not return a result value");
            }
        } else {
            if (!outputType.isInstance(result)) {
                throw new InternalException("Node " + node + " returned a result '" + result
                        + "' that is not an instance of the output type: " + outputType);
            }
        }
    }

    @Override
    public Instance evaluate(Expression expression) {
        if (expression instanceof NodeExpression nodeExpression) {
            return getOutput(nodeExpression.getNode());
        } else {
            throw new RuntimeException("context '" + this + "' doesn't support expression: " + expression);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof NodeExpression;
    }

    @Override
    public ParameterizedFlowProvider parameterizedFlowProvider() {
        return parameterizedFlowProvider;
    }

    @Override
    public CompositeTypeFacade compositeTypeFacade() {
        return compositeTypeFacade;
    }

    public @Nullable ClassInstance getSelf() {
        return self;
    }

    public List<Instance> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void jumpTo(NodeRT node) {
        this.entry = node;
    }

    public Branch getSelectedBranch(BranchNode branchNode) {
        return selectedBranches.get(branchNode);
    }

    @SuppressWarnings("unused")
    public Branch currentBranch() {
        return NncUtils.requireNonNull(branches.peek());
    }

    public void setSelectedBranch(BranchNode branchNode, Branch branch) {
        selectedBranches.put(branchNode, branch);
    }

    public void setExitBranch(BranchNode branchNode, Branch branch) {
        exitBranches.put(branchNode, branch);
    }

    public @Nullable Branch removeExitBranch(BranchNode branchNode) {
        return exitBranches.remove(branchNode);
    }

    public FrameState getState() {
        return state;
    }

    public ClassInstance getThrow() {
        return NncUtils.requireNonNull(exception);
    }

    @Override
    public InstanceRepository instanceRepository() {
        return instanceRepository;
    }

}
