package tech.metavm.flow;

import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.FlowExecutionException;

import java.util.LinkedList;

public class FlowStack {

    private final InstanceContext context;
    private final PathTree root = new PathTree("root");
    private final LinkedList<NodeRT<?>> actionBuffer = new LinkedList<>();
    private final LinkedList<FlowFrame> stack = new LinkedList<>();
    private Instance ret;

    public FlowStack(FlowRT flow, Instance self, InstanceDTO argument, InstanceContext context) {
        this.context = context;
        stack.push(new FlowFrame(flow, self, argument, this));
    }

    public Instance execute() {
        while (!stack.isEmpty()) {
            FlowFrame frame = stack.peek();
            frame.execute();
            if(frame.getState() == FlowFrame.State.RETURN) {
                stack.pop();
                if(stack.isEmpty()) {
                    ret = frame.getRet();
                }
                else {
                    stack.peek().resume(frame.getRet());
                }
            }
            else if(frame.getState() == FlowFrame.State.EXCEPTION) {
                throw new FlowExecutionException("未知错误");
            }
        }
        return ret;
    }

    FlowFrame pop() {
        return stack.pop();
    }

    public InstanceDTO getResult() {
        return ret.toDTO();
    }

    void push(FlowFrame context) {
        stack.push(context);
    }

    InstanceContext getContext() {
        return context;
    }

    FlowFrame peek() {
        return stack.peek();
    }

    boolean isEmpty() {
        return stack.isEmpty();
    }

    public void addActionToBuffer(NodeRT<?> node) {
        actionBuffer.add(node);
    }

    private void addExpressionToResolve(Expression expression) {

    }

}
