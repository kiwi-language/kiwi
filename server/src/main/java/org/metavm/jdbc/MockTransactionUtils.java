package org.metavm.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MockTransactionUtils {

    public static void beginTransaction() {
        TransactionStatus.setContext(new MockTransactionContext());
    }

    public static void commitTransaction() {
        doBeforeCommit();
        doAfterCommit();
    }

    private static void doBeforeCommit() {
        var syncs = TransactionStatus.getCallbacks();
        var offset = 0;
        do {
            for (int i = offset; i < syncs.size(); i++) {
                syncs.get(i).beforeCommit(TransactionStatus.isTransactionReadonly());
            }
            offset = syncs.size();
            syncs = TransactionStatus.getCallbacks();
        } while (offset != syncs.size());
    }

    private static void doAfterCommit() {
        var syncs = TransactionStatus.getCallbacks();
        var offset = 0;
        do {
            for (int i = offset; i < syncs.size(); i++) {
                syncs.get(i).afterCommit();
            }
            offset = syncs.size();
            syncs = TransactionStatus.getCallbacks();
        } while (offset != syncs.size());
    }

    public static void doInTransactionWithoutResult(Runnable action) {
        try {
            beginTransaction();
            action.run();
            commitTransaction();
        } finally {
            TransactionStatus.clear();
        }
    }

    public static <T> T doInTransaction(Supplier<T> action) {
        try {
            beginTransaction();
            var result = action.get();
            commitTransaction();
            return result;
        } finally {
            TransactionStatus.clear();
        }
    }

    private static class MockTransactionContext implements TransactionContext {

        private final List<TransactionCallback> callbacks = new ArrayList<>();
        private boolean readonly;

        @Override
        public List<TransactionCallback> getCallbacks() {
            return Collections.unmodifiableList(callbacks);
        }

        @Override
        public void addCallback(TransactionCallback callback) {
            callbacks.add(callback);
        }

        @Override
        public boolean isReadonly() {
            return readonly;
        }

        @Override
        public void setReadonly(boolean b) {
            this.readonly = b;
        }

        @Override
        public Connection getConnection() {
            throw new UnsupportedOperationException();
        }
    }

}
