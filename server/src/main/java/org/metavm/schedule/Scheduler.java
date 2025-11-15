package org.metavm.schedule;

public interface Scheduler {

    void schedule(Runnable run, int delay);
}
