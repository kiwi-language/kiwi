package org.manul.schedule;

public interface Scheduler {

    void schedule(Runnable run, int delay);
}
