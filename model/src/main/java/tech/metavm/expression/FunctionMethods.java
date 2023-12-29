package tech.metavm.expression;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.IteratorImplNative;
import tech.metavm.entity.natives.NativeMethods;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.Type;
import tech.metavm.util.*;

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
        return Instances.max(a, b);
    }

    public static DoubleInstance MIN(DoubleInstance a, DoubleInstance b) {
        return Instances.min(a, b);
    }

    public static DoubleInstance SUM(DoubleInstance a, DoubleInstance b) {
        return Instances.sum(a, b);
    }

    public static LongInstance MAX(LongInstance a, LongInstance b) {
        return Instances.max(a, b);
    }

    public static LongInstance MIN(LongInstance a, LongInstance b) {
        return Instances.min(a, b);
    }

    public static LongInstance SUM(LongInstance a, LongInstance b) {
        return Instances.sum(a, b);
    }

    public static BooleanInstance IS_BLANK(StringInstance str) {
        return str.isBlank();
    }

    public static BooleanInstance STARTS_WITH(StringInstance str, StringInstance prefix) {
        return str.startsWith(prefix);
    }

    public static BooleanInstance CONTAINS(StringInstance str, StringInstance prefix) {
        return str.contains(prefix);
    }

    public static StringInstance CONCAT(Instance str1, Instance str2) {
        return new StringInstance(str1.getTitle() + str2.getTitle(), StandardTypes.getStringType());
    }

    public static LongInstance RANDOM() {
        return new LongInstance(NncUtils.random(), StandardTypes.getLongType());
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
        return Instances.timeInstance(System.currentTimeMillis());
    }

    public static TimeInstance TIME(LongInstance timeMillis) {
        return Instances.timeInstance(timeMillis.getValue());
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        NncUtils.requireTrue(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return ValueUtil.getCompatibleType(argumentCLasses.get(0), argumentCLasses.get(1));
    }

    public static boolean isAssignable(Type from, Type to) {
        return from.isAssignableFrom(to);
    }

    public static LongInstance LEN(Instance instance) {
        if(instance instanceof ArrayInstance array) {
            return Instances.longInstance(array.length());
        }
        else {
            throw new BusinessException(ErrorCode.ERROR_DELETING_TYPE, "LEN");
        }
    }

    public static BooleanInstance HAS_NEXT(Instance iterator) {
        var iteratorNative = (IteratorImplNative) NativeMethods.getNativeObject((ClassInstance) iterator);
        return iteratorNative.hasNext();
    }

}
