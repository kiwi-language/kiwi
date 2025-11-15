package org.metavm.jdbc;

import org.metavm.context.sql.TransactionIsolation;
import org.metavm.context.sql.TransactionPropagation;

import java.util.function.Supplier;

public interface TransactionOperations {

    <T> T execute(Supplier<T> action);

    default void execute(Runnable run) {
        execute(() -> {
            run.run();
            return null;
        });
    }

    <T> T execute(Supplier<T> action, boolean readonly, TransactionPropagation propagation);

    default void execute(Runnable action, boolean readonly, TransactionPropagation propagation) {
        execute(() -> {
            action.run();
            return null;
        }, readonly, propagation);
    }

    void setIsolationLevel(TransactionIsolation level);

}
