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
import org.metavm.util.Utils;

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

    public static BooleanValue IS_BLANK(Value str) {
        if (str instanceof NullValue) return BooleanValue.true_;
        return Instances.booleanInstance(Instances.toJavaString(str).isEmpty());
    }

    public static PasswordValue PASSWORD(Value str) {
        return Instances.passwordInstance(EncodingUtils.md5(Instances.toJavaString(str)));
    }

    public static Value GET_PASSWORD(PasswordValue password) {
        return Instances.stringInstance(password.getValue());
    }

    public static Value DATE_BEFORE(TimeValue date1, TimeValue date2) {
        return date1.before(date2);
    }

    public static Value DATE_AFTER(TimeValue date1, TimeValue date2) {
        return date1.after(date2);
    }

    public static Value UUID() {
        return Instances.stringInstance(UUID.randomUUID().toString());
    }

    public static Value MD5(Value stringInstance) {
        return Instances.stringInstance(EncodingUtils.md5(Instances.toJavaString(stringInstance)));
    }

    public static LongValue GET_ID(Value instance) {
        if(instance instanceof EntityReference d)
            return Instances.longInstance(Utils.orElse(d.tryGetTreeId(), 0L));
        else
            return Instances.longInstance(0L);
    }

    public static Value STARTS_WITH(Value first, Value prefix) {
        if(first instanceof NullValue)
            return Instances.zero();
        else if(first instanceof StringReference s1 && prefix instanceof StringReference s2)
            return Instances.booleanInstance(s1.getValue().startsWith(s2.getValue()));
        throw new IllegalArgumentException("Invalid argument for starts_with function: " + first);
    }

    public static Value CONTAINS(Value first, Value prefix) {
        if(first instanceof NullValue)
            return Instances.zero();
        if(first instanceof StringReference s1 && prefix instanceof StringReference s2)
            return Instances.booleanInstance(s1.getValue().contains(s2.getValue()));
        else
            throw new IllegalArgumentException("Invalid argument for contains function: " + first);
    }

    public static Value CONCAT(Value s1, Value s2) {
        return StringInstance.concat(s1, s2);
    }

    public static Value REPLACE(Value s, Value target, Value replacement) {
        return StringInstance.replace(s, target, replacement);
    }

    public static Value REPLACE_FIRST(Value s, Value regex, Value replacement) {
        return StringInstance.replaceFirst(s, regex, replacement);
    }

    public static LongValue RANDOM() {
        return new LongValue(Utils.random());
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
        return Utils.isBlank(value);
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

    public static Value TO_STRING(Value instance) {
        return Instances.stringInstance(instance.getTitle());
    }

    public static Value ARRAY_CONTAINS(ArrayInstance array, Value value) {
        return Instances.intInstance(array.contains(value));
    }

    public static Value RANDOM_PASSWORD() {
        return Instances.stringInstance(Utils.randomPassword());
    }

    public static Type IF$_TYPE_RESOLVER(List<Type> argumentCLasses) {
        Utils.require(argumentCLasses.size() == 2,
                "Incorrect number of arguments for function IF");
        return Types.getCompatibleType(argumentCLasses.getFirst(), argumentCLasses.get(1));
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
        var iteratorNative = (IteratorImplNative) NativeMethods.getNativeObject(iterator.resolveMvObject());
        return iteratorNative.hasNext();
    }

    public static Value REGEX_MATCH(Value regex, Value value) {
        return Instances.intInstance(Pattern.compile(Instances.toJavaString(regex)).matcher(Instances.toJavaString(value)).matches());
    }

    public static Value NUMBER_FORMAT(Value format, LongValue value) {
        return Instances.stringInstance(new DecimalFormat(Instances.toJavaString(format)).format(value.getValue()));
    }

    public static LongValue BOUNDED_RANDOM(LongValue bound) {
        return Instances.longInstance(new Random().nextLong(bound.getValue()));
    }

    public static Value STRING_FORMAT(Value format, ArrayInstance values) {
        var args = new Object[values.size()];
        Utils.map(values, Value::getTitle).toArray(args);
        return Instances.stringInstance(String.format(Instances.toJavaString(format)));
    }

    public static LongValue DATE_GET_TIME(TimeValue date) {
        return Instances.longInstance(date.getValue());
    }

}
