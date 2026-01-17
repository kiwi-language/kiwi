package org.manul.schedule;

import lombok.extern.slf4j.Slf4j;
import org.manul.context.ApplicationContext;
import org.manul.context.Component;

@Slf4j
@Component
public class DefaultScheduler implements Scheduler {
    @Override
    public void schedule(Runnable run, int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }
        Thread.startVirtualThread(() -> {
            while (!ApplicationContext.isStopped()) {
                try {
                    run.run();
                    //noinspection BusyWait
                } catch (Throwable e) {
                    log.error("Error in scheduled task", e);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}
