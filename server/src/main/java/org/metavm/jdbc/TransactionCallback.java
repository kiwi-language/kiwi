package org.metavm.jdbc;

public interface TransactionCallback {

    default void afterCommit() {}

    default void beforeCommit(boolean transactionReadonly) {}
}
