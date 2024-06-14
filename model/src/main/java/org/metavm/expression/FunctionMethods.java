package org.metavm.expression;

import org.metavm.common.ErrorCode;
import org.metavm.entity.StandardTypes;
import org.metavm.entity.natives.IteratorImplNative;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;
import org.metavm.util.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

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

    public static PasswordInstance PASSWORD(StringInstance str) {
        return Instances.passwordInstance(EncodingUtils.md5(str.getValue()));
    }

    public static StringInstance GET_PASSWORD(PasswordInstance password) {
        return Instances.stringInstance(password.getValue());
    }

    public static BooleanInstance DATE_BEFORE(TimeInstance date1, TimeInstance date2) {
        return date1.before(date2);
    }

    public static BooleanInstance DATE_AFTER(TimeInstance date1, TimeInstance date2) {
        return date1.after(date2);
    }

    public static StringInstance UUID() {
        return Instances.stringInstance(UUID.randomUUID().toString());
    }

    public static StringInstance MD5(StringInstance stringInstance) {
        return Instances.stringInstance(EncodingUtils.md5(stringInstance.getValue()));
    }

    public static LongInstance GET_ID(Instance instance) {
        if(instance instanceof DurableInstance d)
            return Instances.longInstance(NncUtils.orElse(d.tryGetTreeId(), 0L));
        else
            return Instances.longInstance(0L);
    }

    public static BooleanInstance STARTS_WITH(Instance first, StringInstance prefix) {
        if(first instanceof NullInstance)
            return Instances.falseInstance();
        else if(first instanceof StringInstance str)
            return str.startsWith(prefix);
        throw new IllegalArgumentException("Invalid argument for starts_with function: " + first);
    }

    public static BooleanInstance CONTAINS(Instance first, StringInstance prefix) {
        if(first instanceof NullInstance)
            return Instances.falseInstance();
        if(first instanceof StringInstance str)
            return str.contains(prefix);
        else
            throw new IllegalArgumentException("Invalid argument for contains function: " + first);
    }

    public static StringInstance CONCAT(Instance str1, Instance str2) {
        return new StringInstance(str1.getTitle() + str2.getTitle(), StandardTypes.getStringType());
    }

    public static StringInstance REPLACE(StringInstance string, StringInstance target, StringInstance replacement) {
        return Instances.stringInstance(string.getValue().replace(target.getValue(), replacement.getValue()));
    }

    public static StringInstance REPLACE_FIRST(StringInstance string, StringInstance regex, StringInstance replacement) {
        return Instances.stringInstance(string.getValue().replaceFirst(regex.getValue(), replacement.getValue()));
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

    public static LongInstance NOW_MILLIS() {
        return Instances.longInstance(System.currentTimeMillis());
    }

    public static TimeInstance TIME(LongInstance timeMillis) {
        return Instances.timeInstance(timeMillis.getValue());
    }

    public static StringInstance TO_STRING(Instance instance) {
        return Instances.stringInstance(instance.getTitle());
    }

    public static BooleanInstance ARRAY_CONTAINS(ArrayInstance array, Instance value) {
        return Instances.booleanInstance(array.contains(value));
    }

    public static StringInstance RANDOM_PASSWORD() {
        return Instances.stringInstance(NncUtils.randomPassword());
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        NncUtils.requireTrue(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return ValueUtils.getCompatibleType(argumentCLasses.get(0), argumentCLasses.get(1));
    }

    public static boolean isAssignable(Type from, Type to) {
        return from.isAssignableFrom(to);
    }

    public static LongInstance LEN(Instance instance) {
        if(instance instanceof ArrayInstance array)
            return Instances.longInstance(array.length());
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT, "LEN");
    }

    public static BooleanInstance HAS_NEXT(Instance iterator) {
        var iteratorNative = (IteratorImplNative) NativeMethods.getNativeObject((ClassInstance) iterator);
        return iteratorNative.hasNext();
    }

    public static BooleanInstance REGEX_MATCH(StringInstance regex, StringInstance value) {
        return Instances.booleanInstance(Pattern.compile(regex.getValue()).matcher(value.getValue()).matches());
    }

    public static StringInstance NUMBER_FORMAT(StringInstance format, LongInstance value) {
        return Instances.stringInstance(new DecimalFormat(format.getValue()).format(value.getValue()));
    }

    public static LongInstance BOUNDED_RANDOM(LongInstance bound) {
        return Instances.longInstance(new Random().nextLong(bound.getValue()));
    }

    public static StringInstance STRING_FORMAT(StringInstance format, ArrayInstance values) {
        var args = new Object[values.size()];
        NncUtils.map(values, Instance::getTitle).toArray(args);
        return Instances.stringInstance(String.format(format.getValue(), args));
    }

    public static LongInstance DATE_GET_TIME(TimeInstance date) {
        return Instances.longInstance(date.getValue());
    }

}
