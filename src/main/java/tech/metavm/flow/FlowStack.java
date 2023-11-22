package tech.metavm.flow;

import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.StaticFieldExpression;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.CompositeType;
import tech.metavm.object.type.Type;
import tech.metavm.util.FlowExecutionException;

import java.util.LinkedList;
import java.util.List;

public class FlowStack {

    private final IInstanceContext context;
    private final PathTree root = new PathTree("root");
    private final LinkedList<NodeRT<?>> actionBuffer = new LinkedList<>();
    private final LinkedList<Frame> stack = new LinkedList<>();
    private Instance ret;

    public FlowStack(Flow flow, Instance self, List<Instance> arguments, IInstanceContext context) {
        this.context = context;
        push(createFlowFrame(flow, self, arguments));
    }

    public MetaFrame createFlowFrame(Flow flow, Instance self, List<Instance> arguments) {
//        setLoadFromCache(flow);
        return new MetaFrame(flow, self, arguments, this);
    }

//    private void setLoadFromCache(Flow flow) {
//        flow.accept(new WithCacheVisitor(context.getEntityContext()));
//    }

    public Instance execute() {
        while (!stack.isEmpty()) {
            Frame frame = stack.peek();
            frame.execute();
            if(frame.getState() == FrameState.RETURN) {
                stack.pop();
                if(stack.isEmpty()) {
                    ret = frame.getRet();
                }
                else {
                    stack.peek().resume(frame.getRet());
                }
            }
            else {
                while (frame.getState() == FrameState.EXCEPTION) {
                    stack.pop();
                    var exception = frame.getThrow();
                    if (stack.isEmpty()) {
                        exception(exception);
                    } else {
                        frame = stack.peek();
                        frame.resumeWithException(exception);
                    }
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

    Frame pop() {
        return stack.pop();
    }

    public InstanceDTO getResult() {
        return ret.toDTO();
    }

    void push(Frame frame) {
        stack.push(frame);
    }

    IInstanceContext getContext() {
        return context;
    }

    Frame peek() {
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
