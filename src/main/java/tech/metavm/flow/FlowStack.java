package tech.metavm.flow;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.rest.InstanceDTO;

import java.util.LinkedList;

public class FlowStack {

    private final InstanceContext instanceContext;
    private final LinkedList<FlowFrame> stack = new LinkedList<>();
    private Instance ret;

    public FlowStack(FlowRT flow, long selfId, InstanceDTO argument, InstanceContext instanceContext) {
        this.instanceContext = instanceContext;
        stack.push(new FlowFrame(flow, selfId, argument, this));
    }

    public void execute() {
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
                return;
            }
        }
        instanceContext.finish();
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

    InstanceContext getInstanceContext() {
        return instanceContext;
    }

    FlowFrame peek() {
        return stack.peek();
    }

    boolean isEmpty() {
        return stack.isEmpty();
    }
}
