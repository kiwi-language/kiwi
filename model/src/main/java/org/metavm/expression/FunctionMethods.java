package org.metavm.expression;

import org.metavm.common.ErrorCode;
import org.metavm.entity.natives.IteratorImplNative;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.BusinessException;
import org.metavm.util.EncodingUtils;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

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

    public static DoubleValue MAX(DoubleValue a, DoubleValue b) {
        return Instances.max(a, b);
    }

    public static DoubleValue MIN(DoubleValue a, DoubleValue b) {
        return Instances.min(a, b);
    }

    public static DoubleValue SUM(DoubleValue a, DoubleValue b) {
        return Instances.sum(a, b);
    }

    public static LongValue MAX(LongValue a, LongValue b) {
        return Instances.max(a, b);
    }

    public static LongValue MIN(LongValue a, LongValue b) {
        return Instances.min(a, b);
    }

    public static LongValue SUM(LongValue a, LongValue b) {
        return Instances.sum(a, b);
    }

    public static BooleanValue IS_BLANK(StringValue str) {
        return str.isBlank();
    }

    public static PasswordValue PASSWORD(StringValue str) {
        return Instances.passwordInstance(EncodingUtils.md5(str.getValue()));
    }

    public static StringValue GET_PASSWORD(PasswordValue password) {
        return Instances.stringInstance(password.getValue());
    }

    public static Value DATE_BEFORE(TimeValue date1, TimeValue date2) {
        return date1.before(date2);
    }

    public static Value DATE_AFTER(TimeValue date1, TimeValue date2) {
        return date1.after(date2);
    }

    public static StringValue UUID() {
        return Instances.stringInstance(UUID.randomUUID().toString());
    }

    public static StringValue MD5(StringValue stringInstance) {
        return Instances.stringInstance(EncodingUtils.md5(stringInstance.getValue()));
    }

    public static LongValue GET_ID(Value instance) {
        if(instance instanceof Reference d)
            return Instances.longInstance(NncUtils.orElse(d.tryGetTreeId(), 0L));
        else
            return Instances.longInstance(0L);
    }

    public static Value STARTS_WITH(Value first, StringValue prefix) {
        if(first instanceof NullValue)
            return Instances.zero();
        else if(first instanceof StringValue str)
            return str.startsWith(prefix);
        throw new IllegalArgumentException("Invalid argument for starts_with function: " + first);
    }

    public static Value CONTAINS(Value first, StringValue prefix) {
        if(first instanceof NullValue)
            return Instances.zero();
        if(first instanceof StringValue str)
            return str.contains(prefix);
        else
            throw new IllegalArgumentException("Invalid argument for contains function: " + first);
    }

    public static StringValue CONCAT(Value str1, Value str2) {
        return new StringValue(str1.getTitle() + str2.getTitle());
    }

    public static StringValue REPLACE(StringValue string, StringValue target, StringValue replacement) {
        return Instances.stringInstance(string.getValue().replace(target.getValue(), replacement.getValue()));
    }

    public static StringValue REPLACE_FIRST(StringValue string, StringValue regex, StringValue replacement) {
        return Instances.stringInstance(string.getValue().replaceFirst(regex.getValue(), replacement.getValue()));
    }

    public static LongValue RANDOM() {
        return new LongValue(NncUtils.random());
    }

    public static Value IF(BooleanValue condition, Value value1, Value value2) {
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

    public static TimeValue NOW() {
        return Instances.timeInstance(System.currentTimeMillis());
    }

    public static LongValue NOW_MILLIS() {
        return Instances.longInstance(System.currentTimeMillis());
    }

    public static TimeValue TIME(LongValue timeMillis) {
        return Instances.timeInstance(timeMillis.getValue());
    }

    public static StringValue TO_STRING(Value instance) {
        return Instances.stringInstance(instance.getTitle());
    }

    public static Value ARRAY_CONTAINS(ArrayInstance array, Value value) {
        return Instances.intInstance(array.contains(value));
    }

    public static StringValue RANDOM_PASSWORD() {
        return Instances.stringInstance(NncUtils.randomPassword());
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        NncUtils.requireTrue(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return Types.getCompatibleType(argumentCLasses.get(0), argumentCLasses.get(1));
    }

    public static boolean isAssignable(Type from, Type to) {
        return from.isAssignableFrom(to);
    }

    public static IntValue LEN(Value instance) {
        if(instance.isArray())
            return Instances.intInstance(instance.resolveArray().length());
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT, "LEN");
    }

    public static Value HAS_NEXT(Value iterator) {
        var iteratorNative = (IteratorImplNative) NativeMethods.getNativeObject(iterator.resolveObject());
        return iteratorNative.hasNext();
    }

    public static Value REGEX_MATCH(StringValue regex, StringValue value) {
        return Instances.intInstance(Pattern.compile(regex.getValue()).matcher(value.getValue()).matches());
    }

    public static StringValue NUMBER_FORMAT(StringValue format, LongValue value) {
        return Instances.stringInstance(new DecimalFormat(format.getValue()).format(value.getValue()));
    }

    public static LongValue BOUNDED_RANDOM(LongValue bound) {
        return Instances.longInstance(new Random().nextLong(bound.getValue()));
    }

    public static StringValue STRING_FORMAT(StringValue format, ArrayInstance values) {
        var args = new Object[values.size()];
        NncUtils.map(values, Value::getTitle).toArray(args);
        return Instances.stringInstance(String.format(format.getValue(), args));
    }

    public static LongValue DATE_GET_TIME(TimeValue date) {
        return Instances.longInstance(date.getValue());
    }

}
