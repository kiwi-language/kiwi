package tech.metavm.expression;

import tech.metavm.entity.natives.IteratorImplNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
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

    public static TimeInstance NOW() {
        return InstanceUtils.timeInstance(System.currentTimeMillis());
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        NncUtils.requireTrue(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return ValueUtil.getConvertibleType(argumentCLasses.get(0), argumentCLasses.get(1));
    }

    public static boolean isAssignable(Type from, Type to) {
        return from.isAssignableFrom(to);
    }

    public static LongInstance LEN(ArrayInstance array) {
        return InstanceUtils.longInstance(array.length());
    }

    public static BooleanInstance HAS_NEXT(Instance iterator) {
        var iteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(iterator);
        return iteratorNative.hasNext();
    }

}
