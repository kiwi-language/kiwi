package org.metavm.jdbc;

import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionStatus {

    private static final ThreadLocal<Status> statusTl = ThreadLocal.withInitial(Status::new);

    static void setContext(TransactionContext context) {
        statusTl.get().context = context;
    }
    
    static void addContext(DataSource dataSource, TransactionContext context) {
        statusTl.get().contextMap.put(dataSource, context);
    }

    static void removeContext(DataSource dataSource) {
        statusTl.get().contextMap.remove(dataSource);
    }

    static TransactionContext getContext() {
        return context();
    }

    private static TransactionContext context() {
        return statusTl.get().context;
    }
    
    public static boolean isTransactionActive() {
        return context() != null;
    }

    @SneakyThrows
    public static boolean isTransactionReadonly() {
        return context().isReadonly();
    }

    public static void registerCallback(TransactionCallback callback) {
        context().addCallback(callback);
    }

    public static List<TransactionCallback> getCallbacks() {
        return context().getCallbacks();
    }

    public static void clear() {
        statusTl.remove();
    }

    @SneakyThrows
    public static Connection getConnection(DataSource dataSource) {
        var ctx = statusTl.get().contextMap.get(dataSource);
        return ctx != null ? ctx.getConnection() : dataSource.getConnection();
    }
    
    private static class Status {
        TransactionContext context;
        Map<DataSource, TransactionContext> contextMap = new HashMap<>();
    }
    
}
