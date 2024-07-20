package org.metavm.task;

public interface ParentTask {

    void onSubTaskFinished(Task task);

    int getActiveSubTaskCount();
}
