package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.expression.NodeExpression;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.*;
import tech.metavm.util.LinkedList;

import javax.annotation.Nullable;
import java.util.*;

public class MetaFrame implements EvaluationContext, Frame {

    private final Instance self;
    private final List<Instance> arguments;
    private final ClassType owner;
    private final Map<NodeRT<?>, Instance> results = new HashMap<>();
    private NodeRT<?> pc;
    private boolean jumped;
    private final IInstanceContext context;
    private final FlowStack stack;
    private final Map<BranchNode, Branch> selectedBranches = new IdentityHashMap<>();
    private final Map<BranchNode, Branch> exitBranches = new IdentityHashMap<>();
    private final LinkedList<Branch> branches = new LinkedList<>();
    private FrameState state = FrameState.RUNNING;
    private final LinkedList<TryNode> tryNodes = new LinkedList<>();
    private ClassInstance exception;

    private final Map<TryNode, ExceptionInfo> exceptions = new IdentityHashMap<>();

    public MetaFrame(Flow flow, Instance self, List<Instance> arguments, FlowStack stack) {
        this(flow.getRootNode(), flow.getDeclaringType(), self, arguments, stack);
    }

    public MetaFrame(NodeRT<?> entry, ClassType owner, Instance self, List<Instance> arguments, FlowStack stack) {
        this.stack = stack;
        this.context = stack.getContext();
        this.self = self;
        this.arguments = arguments;
        this.owner = owner;
        pc = entry;
    }

    public void setResult(Instance result) {
        checkResult(result, pc);
        results.put(pc, result);
    }

    public Instance getResult(NodeRT<?> node) {
        return results.get(node);
    }

    public Instance addInstance(Instance instance) {
        context.bind(instance);
        return instance;
    }

    public Instance getInstance(long id) {
        return context.get(id);
    }

    public void ret(Instance returnValue) {
        setResult(returnValue);
        state = FrameState.RETURN;
    }

    public void enterTrySection(TryNode tryNode) {
        tryNodes.push(tryNode);
    }

    public TryNode exitTrySection() {
        return tryNodes.pop();
    }

    public boolean inTrySection() {
        return !tryNodes.isEmpty();
    }

    public TryNode currentTrySection() {
        return NncUtils.requireNonNull(tryNodes.peek());
    }

    public void exception(ClassInstance exception) {
        exception(exception, false);
    }

    private void exception(ClassInstance exception, boolean fromResume) {
        if(!tryNodes.isEmpty()) {
            var tryNode = tryNodes.peek();
            exceptions.put(tryNode, new ExceptionInfo(pc, exception));
            if(fromResume) {
                pc = tryNode.getSuccessor();
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

    public void deleteInstance(long id) {
        context.remove(context.get(id));
    }


    public void deleteInstance(Instance instance) {
        context.remove(instance);
    }

    public Object getInstanceField(ClassInstance instance, Field field) {
        checkAccess(field);
        return instance.getField(field);
    }

    private void checkAccess(Field field) {
        if (field.getAccess() == Access.PUBLIC) {
            return;
        }
        if (field.getAccess() == Access.MODULE) {
            return;
        }

        if (field.getAccess() == Access.PRIVATE) {
            if (!field.getDeclaringType().equals(owner)) {
                throw BusinessException.illegalAccess();
            }
        }
    }

    public void execute() {
        for (; ; ) {
            NodeRT<?> node = pc;
            node.execute(this);
            if (state == FrameState.RETURN) {
                return;
            }
            if (state == FrameState.EXCEPTION) {
                return;
            }
            if (stack.peek() != this) {
                return;
            }
            if (jumped) {
                jumped = false;
            } else {
                pc = node.getNext();
            }
            if (pc == null) {
                state = FrameState.RETURN;
                return;
            }
        }
    }

    private void checkResult(Instance result, NodeRT<?> node) {
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
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if (expression instanceof NodeExpression nodeExpression) {
            return getResult(nodeExpression.getNode());
        } else {
            throw new RuntimeException("context '" + this + "' doesn't support expression: " + expression);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof NodeExpression;
    }

    @Override
    public IEntityContext getEntityContext() {
        return getStack().getContext().getEntityContext();
    }

    public void resume(Instance result) {
        setResult(result);
        if(pc instanceof CallNode<?> callNode) {
            callNode.onReturn(result, this);
        }
        pc = pc.getNext();
    }

    public void resumeWithException(ClassInstance exception) {
        exception(exception, true);
    }

    public Instance getSelf() {
        return self;
    }

    public List<Instance> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void finish() {
        context.finish();
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public void pushStack() {
        stack.push(this);
    }

    public FlowStack getStack() {
        return stack;
    }

    public void jumpTo(NodeRT<?> node) {
        jumped = true;
        this.pc = node;
    }

    public Branch getSelectedBranch(BranchNode branchNode) {
        return selectedBranches.get(branchNode);
    }

    public Branch currentBranch() {
        return NncUtils.requireNonNull(branches.peek());
    }

    public void setSelectedBranch(BranchNode branchNode, Branch branch) {
        selectedBranches.put(branchNode, branch);
    }

    public void setExitBranch(BranchNode branchNode, Branch branch) {
        exitBranches.put(branchNode, branch);
    }

    public @Nullable Branch getExitBranch(BranchNode branchNode) {
        return exitBranches.get(branchNode);
    }

    public FrameState getState() {
        return state;
    }

    public Instance getRet() {
        return pc != null ? results.get(pc) : InstanceUtils.nullInstance();
    }

    public ClassInstance getThrow() {
        return NncUtils.requireNonNull(exception);
    }

}
