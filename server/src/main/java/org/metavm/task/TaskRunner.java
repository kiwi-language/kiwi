package org.metavm.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskRunner {

    <V> Future<V> run(Callable<V> callable);

}
