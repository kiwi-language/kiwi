package tech.metavm.object.instance.query;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;

public class FunctionMethods {

    private FunctionMethods() {}

    public static Long MAX(Long a, Long b) {
        return Long.max(a, b);
    }

    public static Long MIN(Long a, Long b) {
        return Long.max(a, b);
    }

    public static Long SUM(Long a, Long b) {
        return a + b;
    }

    public static Integer MAX(Integer a, Integer b) {
        return Integer.max(a, b);
    }

    public static Integer MIN(Integer a, Integer b) {
        return Integer.max(a, b);
    }

    public static Integer SUM(Integer a, Integer b) {
        return a + b;
    }

    public static DoubleInstance MAX(DoubleInstance a, DoubleInstance b) {
        return InstanceUtils.max(a, b);
    }

    public static DoubleInstance MIN(DoubleInstance a, DoubleInstance b) {
        return InstanceUtils.min(a, b);
    }

    public static DoubleInstance SUM(DoubleInstance a, DoubleInstance b) {
        return InstanceUtils.sum(a, b);
    }

    public static IntInstance MAX(IntInstance a, IntInstance b) {
        return InstanceUtils.max(a, b);
    }

    public static IntInstance MIN(IntInstance a, IntInstance b) {
        return InstanceUtils.min(a, b);
    }

    public static IntInstance SUM(IntInstance a, IntInstance b) {
        return InstanceUtils.sum(a, b);
    }

    public static LongInstance MAX(LongInstance a, LongInstance b) {
        return InstanceUtils.max(a, b);
    }

    public static LongInstance MIN(LongInstance a, LongInstance b) {
        return InstanceUtils.min(a, b);
    }

    public static LongInstance SUM(LongInstance a, LongInstance b) {
        return InstanceUtils.sum(a, b);
    }

    public static BooleanInstance IS_BLANK(StringInstance str) {
        return str.isBlank();
    }

    public static Instance IF(BooleanInstance condition, Instance value1, Instance value2) {
        return condition.isTrue() ? value1 : value2;
    }

    public static Double MAX(Double a, Double b) {
        return Double.max(a, b);
    }

    public static Double MIN(Double a, Double b) {
        return Double.max(a, b);
    }

    public static Double SUM(Double a, Double b) {
        return a + b;
    }

    public static Boolean IS_BLANK(String value) {
        return NncUtils.isBlank(value);
    }

    public static Object IF(Boolean condition, Object value1, Object value2) {
        return condition ? value1 : value2;
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        NncUtils.requireTrue(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return ValueUtil.getConvertibleType(argumentCLasses.get(0), argumentCLasses.get(1));
    }

}
