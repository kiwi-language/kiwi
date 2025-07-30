package org.metavm.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;

import java.sql.SQLException;
import java.util.function.Supplier;

@Slf4j
public class PersistenceUtil {

    private static final int MAX_RETRIES = 10;
    private static final String SERIALIZE_ERROR_SQL_STATE = "40001";
    private static final String TABLE_NONEXISTENT_SQL_STATE = "42P01";

    @SneakyThrows
    public static void doWithRetries(Runnable action) {
        doWithRetries(() -> {
            action.run();
            return null;
        });
    }

    @SneakyThrows
    public static <R> R doWithRetries(Supplier<R> action) {
        long wait = 1;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return action.get();
            }
            catch (Exception e) {
                if (!isSerializationError(e))
                    throw e;
                if (i + 1 < MAX_RETRIES)
                    log.warn("Serialization error occurred, retrying... (attempt {}/{})", i + 1, MAX_RETRIES);
                Thread.sleep(wait);
                wait *= 2; // Exponential backoff
            }
        }
        throw new BusinessException(ErrorCode.RETRY_FAILED);
    }

    public static boolean isSerializationError(Exception e) {
        return isSqlState(e, SERIALIZE_ERROR_SQL_STATE);
    }

    public static boolean isTableNonexistent(Exception e) {
        return isSqlState(e, TABLE_NONEXISTENT_SQL_STATE);
    }

    public static boolean isSqlState(Exception e, String sqlState) {
        Throwable ex = e;
        do {
            if (ex instanceof SQLException qslEx)
                return qslEx.getSQLState().equals(sqlState);
            ex = ex.getCause();
        } while (ex != null);
        return false;
    }

}
