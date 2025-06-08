package org.metavm.task;

import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class ThreadPoolTaskRunner implements TaskRunner {

    public static final int POOL_SIZE = 1;

    private final ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

    @Override
    public <V> Future<V> run(Callable<V> callable) {
        return executorService.submit(callable);
    }
}
