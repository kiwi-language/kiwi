package org.manul.jdbc;

import lombok.SneakyThrows;
import org.manul.context.sql.TransactionIsolation;
import org.manul.context.sql.TransactionPropagation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class TransactionManager {
    private final ThreadLocal<Status> statusTl = ThreadLocal.withInitial(Status::new);
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void begin(TransactionIsolation isolation, TransactionPropagation propagation, boolean readonly) {
        var status = statusTl.get();
        var tx = switch (propagation) {
            case REQUIRED -> {
                if (status.transactions.isEmpty())
                    yield status.enterTx(readonly, isolation);
                else
                    yield status.transactions.peek();
            }
            case REQUIRES_NEW -> status.enterTx(readonly, isolation);
        };
        tx.enterScope();
    }

    @SneakyThrows
    void commit() {
        var status = statusTl.get();
        status.currentTx().commit();
    }

    void rollback() {
        var status = statusTl.get();
        status.currentTx().rollback();
    }

    void exitScope() {
        var status = statusTl.get();
        status.currentTx().exitScope();
    }

    private class Status {
        final LinkedList<Transaction> transactions = new LinkedList<>();

        Transaction enterTx(boolean readonly, TransactionIsolation isolation) {
            var tx = new Transaction(this, readonly, isolation);
            transactions.push(tx);
            return tx;
        }

        Transaction currentTx() {
            return Objects.requireNonNull(transactions.peek());
        }

    }

    private class Transaction implements TransactionContext {
        final Status status;
        final Connection conn;
        final LinkedList<Scope> scopes = new LinkedList<>();
        final List<TransactionCallback> callbacks = new ArrayList<>();
        boolean aborted;

        @SneakyThrows
        private Transaction(Status status, boolean readonly, TransactionIsolation isolation) {
            this.status = status;
            TransactionStatus.addContext(dataSource, this);
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            conn.setReadOnly(readonly);
            conn.setTransactionIsolation(switch (isolation) {
                case READ_UNCOMMITTED -> Connection.TRANSACTION_READ_UNCOMMITTED;
                case READ_COMMITTED -> Connection.TRANSACTION_READ_COMMITTED;
                case REPEATABLE_READ -> Connection.TRANSACTION_REPEATABLE_READ;
                case SERIALIZABLE -> Connection.TRANSACTION_SERIALIZABLE;
            });
        }


        @Override
        public List<TransactionCallback> getCallbacks() {
            return Collections.unmodifiableList(callbacks);
        }

        @Override
        public void addCallback(TransactionCallback callback) {
            callbacks.add(callback);
        }

        @SneakyThrows
        @Override
        public boolean isReadonly() {
            return conn.isReadOnly();
        }

        @Override
        public void setReadonly(boolean b) {

        }

        @SneakyThrows
        @Override
        public Connection getConnection() {
            if (aborted)
                throw new SQLException("Transaction has been aborted");
            return conn;
        }

        void enterScope() {
            scopes.push(new Scope(this));
        }

        @SneakyThrows
        void commit() {
            if (scopes.size() == 1) {
                doBeforeCommit();
                conn.commit();
                doAfterCommit();
            }
        }

        @SneakyThrows
        void rollback() {
            conn.rollback();
            aborted = true;
        }

        @SneakyThrows
        void exitScope() {
            scopes.pop().exit();
            if (scopes.isEmpty()) {
                conn.close();
                var popped = status.transactions.pop();
                assert this == popped;
                TransactionStatus.removeContext(dataSource);
                if (!status.transactions.isEmpty())
                    TransactionStatus.addContext(dataSource, status.transactions.peek());
            }
        }

        private void doBeforeCommit() {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < callbacks.size(); i++) {
                callbacks.get(i).beforeCommit(isReadonly());
            }
        }

        private void doAfterCommit() {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < callbacks.size(); i++) {
                callbacks.get(i).afterCommit();
            }
        }

    }

    private class Scope {
        final TransactionContext suspendedCtx;
        final Transaction tx;

        @SneakyThrows
        Scope(Transaction tx) {
            suspendedCtx = TransactionStatus.getContext();
            TransactionStatus.setContext(tx);
            this.tx = tx;
        }

        private void exit() {
            TransactionStatus.setContext(suspendedCtx);
        }

    }

}
