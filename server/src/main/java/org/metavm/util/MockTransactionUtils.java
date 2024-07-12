package org.metavm.util;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

public class MockTransactionUtils {

    public static void beginTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    public static void commitTransaction() {
        var syncs = TransactionSynchronizationManager.getSynchronizations();
        var offset = 0;
        do {
            for (int i = offset; i < syncs.size(); i++) {
                syncs.get(i).afterCommit();
            }
            offset = syncs.size();
            syncs = TransactionSynchronizationManager.getSynchronizations();
        } while (offset != syncs.size());
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
