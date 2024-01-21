package tech.metavm.util;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class MockTransactionOperations implements TransactionOperations {
    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if(!transactionActive)
            TestUtils.beginTransaction();
        var result = action.doInTransaction(new TransactionStatus() {
            @Override
            public boolean isNewTransaction() {
                return false;
            }

            @Override
            public void setRollbackOnly() {

            }

            @Override
            public boolean isRollbackOnly() {
                return false;
            }

            @Override
            public boolean isCompleted() {
                return false;
            }

            @Override
            public Object createSavepoint() throws TransactionException {
                return null;
            }

            @Override
            public void rollbackToSavepoint(Object savepoint) throws TransactionException {

            }

            @Override
            public void releaseSavepoint(Object savepoint) throws TransactionException {

            }

            @Override
            public boolean hasSavepoint() {
                return false;
            }

            @Override
            public void flush() {

            }
        });
        if(!transactionActive)
            TestUtils.commitTransaction();;
        return result;
    }
}
