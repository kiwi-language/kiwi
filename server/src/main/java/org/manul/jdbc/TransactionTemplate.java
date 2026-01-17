package org.manul.jdbc;

import org.manul.context.sql.TransactionIsolation;
import org.manul.context.sql.TransactionPropagation;

import javax.sql.DataSource;
import java.util.function.Supplier;

public class TransactionTemplate implements TransactionOperations {

    private final TransactionManager transactionManager;
    private TransactionIsolation isolation = TransactionIsolation.READ_COMMITTED;

    public TransactionTemplate(DataSource dataSource) {
        transactionManager = new TransactionManager(dataSource);
    }

    @Override
    public <T> T execute(Supplier<T> action, boolean readonly, TransactionPropagation propagation) {
        transactionManager.begin(isolation, propagation, readonly);
        try {
            var r = action.get();
            transactionManager.commit();
            return r;
        } catch (Exception e) {
            transactionManager.rollback();
            throw e;
        } finally {
            transactionManager.exitScope();
        }
    }

    @Override
    public void setIsolationLevel(TransactionIsolation level) {
        isolation = level;
    }

}
