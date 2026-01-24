package org.manul.jdbc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.manul.util.PersistenceUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

    @SneakyThrows
    public static int getPid() {
        try {
            return PersistenceUtil.getPid(statusTl.get().context.getConnection());
        } catch (Exception e) {
            return -1;
        }
    }

    public static void clear() {
        statusTl.remove();
    }

    @SneakyThrows
    public static Connection getConnection(DataSource dataSource) {
        var ctx = statusTl.get().contextMap.get(dataSource);
        var conn = ctx != null ? ctx.getConnection() : dataSource.getConnection();
//        var pgconn = conn.unwrap(PGConnection.class);
//        var pid = pgconn.getBackendPID();
//        log.info("Getting connection. PID: {}", pid, new Exception());
        return conn;
    }
    
    private static class Status {
        TransactionContext context;
        Map<DataSource, TransactionContext> contextMap = new HashMap<>();
    }
    
}
