package tech.metavm.flow;

import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

import java.util.ArrayList;
import java.util.List;

public class NativeFrame implements Frame {

    private final Flow flow;
    private final Instance self;
    private final List<Instance> arguments;
    private Instance ret;
    private FrameState state = FrameState.RUNNING;

    public NativeFrame(Flow flow, Instance self, List<Instance> arguments) {
        this.flow = flow;
        this.self = self;
        this.arguments = new ArrayList<>(arguments);
    }

    @Override
    public void execute() {
        ret = NativeInvoker.invoke(flow, self, arguments);
        state = FrameState.RETURN;
    }

    @Override
    public FrameState getState() {
        return state;
    }

    @Override
    public Instance getRet() {
        return ret;
    }

    @Override
    public ClassInstance getThrow() {
        return null;
    }

    @Override
    public void resume(Instance ret) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeWithException(ClassInstance exception) {
        throw new UnsupportedOperationException();
    }
}
