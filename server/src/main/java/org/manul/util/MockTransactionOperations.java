package org.manul.util;


import org.manul.context.Component;
import org.manul.context.sql.TransactionIsolation;
import org.manul.context.sql.TransactionPropagation;
import org.manul.jdbc.MockTransactionUtils;
import org.manul.jdbc.TransactionOperations;
import org.manul.jdbc.TransactionStatus;

import java.util.function.Supplier;

@Component(module = "memory")
public class MockTransactionOperations implements TransactionOperations {

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
