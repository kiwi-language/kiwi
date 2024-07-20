package org.metavm.task;

import javax.annotation.Nullable;

public interface SubTask {

    @Nullable
    ParentTask getParentTask();

    void setParentTask(ParentTask parentTask);

}
