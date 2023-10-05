package tech.metavm.flow;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.expression.NodeExpression;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class FlowFrame implements EvaluationContext {

    private final Instance self;
    private final Instance argument;
    private final ClassType owner;
    private final Flow flow;
    private final Map<NodeRT<?>, Instance> results = new HashMap<>();
    private NodeRT<?> pc;
    private boolean jumped;
    private final IInstanceContext context;
    private final FlowStack stack;
    private final Map<BranchNode, Branch> selectedBranches = new IdentityHashMap<>();
    private final Map<BranchNode, Branch> exitBranches = new IdentityHashMap<>();
    private final LinkedList<Branch> branches = new LinkedList<>();
    private State state = State.NORMAL;
    private String exceptionMessage;
    private NodeRT<?> lastNode;
    private final LinkedList<TryNode> tryNodes = new LinkedList<>();
    private ClassInstance exception;

    private final Map<TryNode, ExceptionInfo> exceptions = new IdentityHashMap<>();

    public enum State {
        NORMAL,
        RETURN,
        EXCEPTION
    }

    public FlowFrame(Flow flow, Instance self, Instance argument, FlowStack stack) {
        this.flow = flow;
        this.stack = stack;
        this.context = stack.getContext();
        this.self = self;
        this.argument = argument;
        owner = flow.getDeclaringType();
        pc = flow.getRootNode();
    }

    public void setResult(Instance result) {
        checkResult(result, pc);
        results.put(pc, result);
    }

    public Object getResult(NodeRT<?> node, long fieldId) {
        return NncUtils.get((ClassInstance) results.get(node), inst -> inst.get(fieldId));
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
        state = State.RETURN;
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

    public void exception(String message) {
        this.exceptionMessage = message;
        state = State.EXCEPTION;
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
            state = State.EXCEPTION;
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
        return instance.get(field);
    }

    private void checkAccess(Field field) {
        if (field.getAccess() == Access.GLOBAL) {
            return;
        }
        if (field.getAccess() == Access.MODULE) {
            return;
        }

        if (field.getAccess() == Access.CLASS) {
            if (!field.getDeclaringType().equals(owner)) {
                throw BusinessException.illegalAccess();
            }
        }
    }

    public void execute() {
        for (; ; ) {
            NodeRT<?> node = pc;
            node.execute(this);
            if (state == State.RETURN) {
                return;
            }
            if (state == State.EXCEPTION) {
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
                state = State.RETURN;
                return;
            }
        }
    }

    public @Nullable NodeRT<?> getLastNode() {
        return lastNode;
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

    public void resume(Instance result) {
        setResult(result);
        pc = pc.getNext();
    }

    public void resumeWithException(ClassInstance exception) {
        exception(exception, true);
    }

    public Instance getSelf() {
        return self;
    }

    public Instance getArgument() {
        return argument;
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

    public void catchException() {
        exception = null;
    }

    public Flow getFlow() {
        return flow;
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

    public State getState() {
        return state;
    }

    public Instance getRet() {
        return pc != null ? results.get(pc) : InstanceUtils.nullInstance();
    }

    public ClassInstance getThrow() {
        return NncUtils.requireNonNull(exception);
    }

}
