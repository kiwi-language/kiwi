package tech.metavm.flow;

import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionEvaluator;
import tech.metavm.object.instance.query.NodeExpression;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.FlowExecutionException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlowFrame implements EvaluationContext {

    private final Instance self;
    private final Instance argument;
    private final ClassType owner;
    private final FlowRT flow;
    private final Map<Long, Instance> results = new HashMap<>();
    private NodeRT<?> pc;
    private boolean jumped;
    private final InstanceContext context;
    private final FlowStack stack;

    private State state = State.NORMAL;
    private String exceptionMessage;

    public enum State {
        NORMAL,
        RETURN,
        EXCEPTION
    }

    public FlowFrame(FlowRT flow, Instance self, InstanceDTO argument, FlowStack stack) {
        this.flow = flow;
        this.stack = stack;
        this.context = stack.getContext();
        this.self = self;
        this.argument = InstanceFactory.create(argument, context);
        owner = flow.getType();
        pc = flow.getRootNode();
    }

    public void setResult(Instance result) {
        checkResult(result, pc);
        results.put(pc.getId(), result);
    }

    public Object getResult(long nodeId, long fieldId) {
        return NncUtils.get((ClassInstance) results.get(nodeId), inst -> inst.get(fieldId));
    }

    public Instance getResult(long nodeId) {
        return results.get(nodeId);
    }

    public Object getResult(NodeRT<?> node, long fieldId) {
        return getResult(node.getId(), fieldId);
    }

    public Instance addInstance(InstanceDTO instanceDTO) {
        return InstanceFactory.create(instanceDTO, context);
    }

    public Instance getInstance(long id) {
        return context.get(id);
    }

    public void ret(Instance returnValue) {
        setResult(returnValue);
        state = State.RETURN;
    }

    public void exception(String message) {
        this.exceptionMessage = message;
        state = State.EXCEPTION;
    }

    public void deleteInstance(long id) {
        context.remove(context.get(id));
    }

    public Object getInstanceField(ClassInstance instance, Field field) {
        checkAccess(field);
        return instance.get(field);
    }

    private void checkAccess(Field field) {
        if(field.getAccess() == Access.GLOBAL) {
            return;
        }
        if(field.getAccess() == Access.MODULE) {
            return;
        }

        if(field.getAccess() == Access.CLASS) {
            if(!field.getDeclaringType().equals(owner)) {
                throw BusinessException.illegalAccess();
            }
        }
    }

    public void execute() {
        for (;;) {
            NodeRT<?> node = pc;
            node.execute(this);
            if(state == State.RETURN ) {
                return;
            }
            if(state == State.EXCEPTION) {
                throw new FlowExecutionException(exceptionMessage);
            }
            if(stack.peek() != this) {
                return;
            }
            if(jumped) {
                jumped = false;
            }
            else {
                pc = node.getGlobalSuccessor();
            }
            if(pc == null) {
                state = State.RETURN;
                return;
            }
        }
    }

    private void checkResult(Instance result, NodeRT<?> node) {
        Type outputType = node.getType();
        if(outputType == null) {
            if(result != null) {
                throw new InternalException("Node " + node + " can not return a result value");
            }
        }
        else {
            if(!outputType.isInstance(result)) {
                throw new InternalException("Node " + node + " returned a result '" + result
                        + "' that does not match the output category: " + outputType);
            }
        }
    }

    @Override
    public Object evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof NodeExpression nodeExpression) {
            return getResult(nodeExpression.getNode().getId());
        }
        else {
            throw new RuntimeException("context '" + this + "' doesn't support expression: " + expression);
        }
    }

    @Override
    public Set<Class<? extends Expression>> supportedExpressionClasses() {
        return Set.of(NodeExpression.class);
    }


    public void resume(Instance result) {
        setResult(result);
        pc = pc.getSuccessor();
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

    public State getState() {
        return state;
    }

    public Instance getRet() {
        return pc != null ? results.get(pc.getId()) : null;
    }
}
