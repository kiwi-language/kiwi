package tech.metavm.flow;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.FlowExecutionException;

import java.util.LinkedList;
import java.util.List;

public class FlowStack {

    private final IInstanceContext context;
    private final PathTree root = new PathTree("root");
    private final LinkedList<NodeRT<?>> actionBuffer = new LinkedList<>();
    private final LinkedList<FlowFrame> stack = new LinkedList<>();
    private Instance ret;

    public FlowStack(Flow flow, Instance self, List<Instance> arguments, IInstanceContext context) {
        this.context = context;
        stack.push(new FlowFrame(flow, self, arguments, this));
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
                stack.pop();
                var exception = frame.getThrow();
                if(stack.isEmpty()) {
                    exception(exception);
                }
                else {
                    stack.peek().resumeWithException(exception);
                }
            }
        }
        return ret;
    }

    private void exception(ClassInstance exception) {
        ThrowableNative nativeObject = (ThrowableNative) NativeInvoker.getNativeObject(exception);
        String message = nativeObject.getMessage() instanceof StringInstance str ? str.getValue() : "执行失败";
        throw new FlowExecutionException(message);
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

    IInstanceContext getContext() {
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
