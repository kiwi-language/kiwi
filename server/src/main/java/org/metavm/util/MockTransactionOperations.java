package org.metavm.util;


import org.metavm.context.Component;
import org.metavm.context.sql.TransactionIsolation;
import org.metavm.context.sql.TransactionPropagation;
import org.metavm.jdbc.MockTransactionUtils;
import org.metavm.jdbc.TransactionOperations;
import org.metavm.jdbc.TransactionStatus;

import java.util.function.Supplier;

@Component(module = "memory")
public class MockTransactionOperations implements TransactionOperations {
    @Override
    public <T> T execute(Supplier<T> action) {
        return execute(action, false, TransactionPropagation.REQUIRED);
    }

    @Override
    public void execute(Runnable run) {
        execute(() -> {
            run.run();
            return null;
        });
    }

    @Override
    public <T> T execute(Supplier<T> action, boolean readonly, TransactionPropagation propagation) {
        boolean transactionActive = TransactionStatus.isTransactionActive();
        if(!transactionActive)
            return MockTransactionUtils.doInTransaction(action);
        else
            return action.get();
    }

    @Override
    public void setIsolationLevel(TransactionIsolation level) {

    }
}
