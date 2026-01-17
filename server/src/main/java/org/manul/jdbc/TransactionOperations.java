package org.manul.jdbc;

import org.manul.context.sql.TransactionIsolation;
import org.manul.context.sql.TransactionPropagation;

import java.util.function.Supplier;

public interface TransactionOperations {

    default <T> T execute(Supplier<T> action) {
        return execute(action, false, TransactionPropagation.REQUIRED);
    }

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
