package org.metavm.flow;

import javax.annotation.Nullable;

public interface Frame {

    @Nullable
    FlowExecResult execute();

//    Instance getRet();

//    void resume(Instance ret);

//    ClassInstance getThrow();

//    void resumeWithException(ClassInstance exception);

}
