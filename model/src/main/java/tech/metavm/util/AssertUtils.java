package tech.metavm.util;

import tech.metavm.common.ErrorCode;

import javax.annotation.Nullable;

public class AssertUtils {


    public static void assertTrue(boolean condition, ErrorCode errorCode, Object... params) {
        if (!condition) {
            throw new BusinessException(errorCode, params);
        }
    }

    public static <T> T assertNonNull(@Nullable T value, ErrorCode errorCode, Object... params) {
        if (value == null) {
            throw new BusinessException(errorCode, params);
        }
        return value;
    }
}
