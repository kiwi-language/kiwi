package org.metavm.jdbc;

import lombok.SneakyThrows;
import org.metavm.context.sql.TransactionIsolation;
import org.metavm.context.sql.TransactionPropagation;

import javax.sql.DataSource;
import java.util.function.Supplier;

public class TransactionTemplate implements TransactionOperations {

    private final TransactionManager transactionManager;
    private TransactionIsolation isolation = TransactionIsolation.READ_COMMITTED;

    public TransactionTemplate(DataSource dataSource) {
        transactionManager = new TransactionManager(dataSource);
    }

    @SneakyThrows
    @Override
    public <T> T execute(Supplier<T> action) {
        return execute(action, false, TransactionPropagation.REQUIRED);
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
        }
    }

    @Override
    public void setIsolationLevel(TransactionIsolation level) {
        isolation = level;
    }

}
