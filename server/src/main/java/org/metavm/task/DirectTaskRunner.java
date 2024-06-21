package org.metavm.task;

import com.google.common.util.concurrent.Futures;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DirectTaskRunner implements TaskRunner {
    @Override
    public <V> Future<V> run(Callable<V> callable) {
        try {
            return Futures.immediateFuture(callable.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
