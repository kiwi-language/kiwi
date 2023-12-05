package tech.metavm.flow;

import javax.annotation.Nullable;

public interface Frame {

    @Nullable
    FlowExecResult execute();

    FrameState getState();

//    Instance getRet();

//    void resume(Instance ret);

//    ClassInstance getThrow();

//    void resumeWithException(ClassInstance exception);

}
