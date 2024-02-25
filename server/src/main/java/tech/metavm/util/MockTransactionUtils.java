package tech.metavm.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

public class MockTransactionUtils {

    public static void beginTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    public static void commitTransaction() {
        var synchronizations = TransactionSynchronizationManager.getSynchronizations();
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        TransactionSynchronizationManager.clear();
    }

    public static void doInTransactionWithoutResult(Runnable action) {
        try {
            beginTransaction();
            action.run();
            commitTransaction();
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    public static <T> T doInTransaction(Supplier<T> action) {
        try {
            beginTransaction();
            var result = action.get();
            commitTransaction();
            return result;
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

}
