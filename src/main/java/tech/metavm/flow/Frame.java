package tech.metavm.flow;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

public interface Frame {

    void execute();

    FrameState getState();

    Instance getRet();

    void resume(Instance ret);

    ClassInstance getThrow();

    void resumeWithException(ClassInstance exception);

}
