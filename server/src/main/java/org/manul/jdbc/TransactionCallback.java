package org.manul.jdbc;

public interface TransactionCallback {

    default void afterCommit() {}

    default void beforeCommit(boolean transactionReadonly) {}
}
