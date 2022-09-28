package tech.metavm.flow;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

import java.util.HashMap;
import java.util.Map;

public class FlowFrame {

    private final Instance self;
    private final Instance argument;
    private final Type owner;
    private final FlowRT flow;
    private final Map<Long, Instance> results = new HashMap<>();
    private NodeRT<?> pc;
    private final InstanceContext instanceContext;
    private final FlowStack stack;

    private State state = State.NORMAL;

    public enum State {
        NORMAL,
        RETURN,
        EXCEPTION
    }

    public FlowFrame(FlowRT flow, long selfId, InstanceDTO argument, FlowStack stack) {
        this.flow = flow;
        this.stack = stack;
        this.instanceContext = stack.getInstanceContext();
        this.self = instanceContext.get(selfId);
        this.argument = instanceContext.add(argument);
        owner = flow.getType();
        pc = flow.getRootNode();
    }

    public void setResult(Instance result) {
        results.put(pc.getId(), result);
    }

    public Object getResult(long nodeId, long fieldId) {
        Instance instance = results.get(nodeId);
        return instance != null ? instance.get(fieldId) : null;
    }

    public Instance getResult(long nodeId) {
        return results.get(nodeId);
    }

    public Object getResult(NodeRT<?> node, long fieldId) {
        return getResult(node.getId(), fieldId);
    }

    public void addInstance(InstanceDTO instance) {
        instanceContext.add(instance);
    }

    public Instance getInstance(long id) {
        return instanceContext.get(id);
    }

    public void ret() {
        state = State.RETURN;
    }

    public void exception() {
        state = State.EXCEPTION;
    }

    public void deleteInstance(long id) {
        instanceContext.delete(id);
    }

    public void setInstanceField(Instance instance, Field field, Object value) {
        checkAccess(field);
        instance.set(field.getId(), value);
    }

    public Object getInstanceField(Instance instance, Field field) {
        checkAccess(field);
        return instance.get(field);
    }

    private void checkAccess(Field field) {
        if(field.getAccess() == Access.Public) {
            return;
        }
        if(field.getAccess() == Access.Protected) {
            return;
        }

        if(field.getAccess() == Access.Private) {
            if(!field.getOwner().equals(owner)) {
                throw BusinessException.illegalAccess();
            }
        }
    }

    public void execute() {
        for (;;) {
            NodeRT<?> node = pc;
            node.execute(this);
            if(state == State.RETURN || state == State.EXCEPTION) {
                return;
            }
            if(stack.peek() != this) {
                return;
            }
            pc = node.getSuccessor();
            if(pc == null) {
                throw BusinessException.missingEndNode();
            }
        }
    }

    public void resume(Instance result) {
        setResult(result);
        pc = pc.getSuccessor();
        execute();
    }

    public Instance getSelf() {
        return self;
    }

    public Instance getArgument() {
        return argument;
    }

    public void finish() {
        instanceContext.finish();
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

    public void gotoNode(NodeRT<?> node) {
        this.pc = node;
    }

    public State getState() {
        return state;
    }

    public Instance getRet() {
        return results.get(pc.getId());
    }
}
