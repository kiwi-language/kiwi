package org.metavm.task;


import lombok.SneakyThrows;
import org.metavm.util.ImmediateFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DirectTaskRunner implements TaskRunner {
    @SneakyThrows
    @Override
    public <V> Future<V> run(Callable<V> callable) {
        return new ImmediateFuture<>(callable.call());
    }
}
