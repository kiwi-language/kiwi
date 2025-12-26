package org.metavm.jdbc;

import java.sql.Connection;
import java.util.List;

public interface TransactionContext {

    List<TransactionCallback> getCallbacks();

    void addCallback(TransactionCallback callback);

    boolean isReadonly();

    void setReadonly(boolean b);

    Connection getConnection();

}
