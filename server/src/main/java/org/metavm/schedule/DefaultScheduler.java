package org.metavm.schedule;

import lombok.extern.slf4j.Slf4j;
import org.metavm.context.ApplicationContext;
import org.metavm.context.Component;

@Slf4j
@Component
public class DefaultScheduler implements Scheduler {
    @Override
    public void schedule(Runnable run, int delay) {
        Thread.startVirtualThread(() -> {
            while (!ApplicationContext.isShutdown()) {
                try {
                    run.run();
                    //noinspection BusyWait
                    Thread.sleep(delay);
                } catch (Throwable e) {
                    log.error("Error in scheduled task", e);
                }
            }
        });
    }
}
