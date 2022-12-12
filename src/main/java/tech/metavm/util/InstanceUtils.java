package tech.metavm.util;

import javassist.util.proxy.ProxyObject;
import tech.metavm.entity.EntityMethodHandler;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.PrimitiveKind;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.object.meta.Type;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class InstanceUtils {

//    private static volatile Function<Class<?>, Type> getTypeFunc = ModelDefRegistry::getType;

//    public static void setGetTypeFunc(Function<Class<?>, Type> getTypeFunc) {
//        getTypeFunc = getTypeFunc;
//    }

//    public static void resetGetTypeFunc() {
//        getTypeFunc = ModelDefRegistry::getType;
//    }

    public static Instance resolveValue(Type fieldType, Object columnValue) {
        if(columnValue instanceof Instance instance) {
            return instance;
        }
        NncUtils.requireTrue(fieldType.isPrimitive(),
                "Can not resolve value '" + columnValue + "' for type " + fieldType);
        return resolvePrimitiveValue(fieldType, columnValue);
    }

    public static PrimitiveInstance resolvePrimitiveValue(Type fieldType, Object columnValue) {
        if(columnValue == null) {
            return null;
        }
        if(ValueUtil.isInteger(columnValue)) {
            if(fieldType.isInt()) {
                return intInstance(((Number) columnValue).intValue());
            }
            else if(fieldType.isTime()) {
                return timeInstance(((Number) columnValue).longValue());
            }
            else {
                return longInstance(((Number) columnValue).longValue());
            }
        }
        else if(ValueUtil.isFloat(columnValue)) {
            return doubleInstance(((Number) columnValue).doubleValue());
        }
        else if(columnValue instanceof Boolean bool) {
            return booleanInstance(bool);
        }
        else if(columnValue instanceof String str) {
            return stringInstance(str);
        }
        throw new InternalException("Can not resolve column value '" + columnValue + "' for type " + fieldType);
    }

    public static PrimitiveInstance intInstance(int value) {
        return new IntInstance(
                value, getIntType()
        );
    }

    public static LongInstance longInstance(long value) {
        return new LongInstance(
                value, getLongType()
        );
    }

    public static BooleanInstance booleanInstance(boolean value) {
        return new BooleanInstance(
                value, getBooleanType()
        );
    }

    public static DoubleInstance doubleInstance(double value) {
        return new DoubleInstance(
                value, getDoubleType()
        );
    }

    public static TimeInstance timeInstance(long value) {
        return new TimeInstance(
                value, getTimeType()
        );
    }

    public static NullInstance nullInstance() {
        return new NullInstance(getNullType());
    }

    public static PasswordInstance passwordInstance(String password) {
        return new PasswordInstance(
                password, getPasswordType()
        );
    }

    public static StringInstance stringInstance(String value) {
        return new StringInstance(
                value, getStringType()
        );
    }

    public static Set<Instance> getAllNewInstances(Instance instance, IInstanceContext context) {
        return getAllNewInstances(List.of(instance), context);
    }

    public static Set<Instance> getAllNewInstances(Collection<Instance> instances, IInstanceContext context) {
        return getAllInstances(
                instances,
                instance -> !context.containsInstance(instance) && !instance.isValue()
        );
    }

    public static Set<Instance> getAllNonValueInstances(Instance root) {
        return getAllNonValueInstances(List.of(root));
    }

    public static Set<Instance> getAllNonValueInstances(Collection<Instance> roots) {
        return getAllInstances(roots, inst -> !inst.isValue());
    }

    public static Set<Instance> getAllInstances(Collection<Instance> roots, Predicate<Instance> filter) {
        IdentitySet<Instance> results = new IdentitySet<>();
        getAllInstances(roots, filter, results);
        return results;
    }

    private static void getAllInstances(Collection<Instance> instances, Predicate<Instance> filter, IdentitySet<Instance> results) {
        List<Instance> newInstances = NncUtils.filter(
                instances, instance -> filter.test(instance) && !results.contains(instance)
        );
        if(newInstances.isEmpty()) {
            return;
        }
        results.addAll(newInstances);
        getAllInstances(
                NncUtils.flatMap(newInstances, Instance::getRefInstances),
                filter,
                results
        );
    }

    public static boolean isInitialized(Instance instance) {
        if(instance instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = (EntityMethodHandler<?>) proxyObject.getHandler();
            return handler.isInitialized();
        }
        else {
            return true;
        }
    }

    public static PrimitiveType getStringType() {
//        return getPrimitiveType(String.class);
        return new PrimitiveType(PrimitiveKind.STRING);
    }

    public static PrimitiveType getIntType() {
//        return getPrimitiveType(Integer.class);
        return new PrimitiveType(PrimitiveKind.INT);
    }

    public static PrimitiveType getLongType() {
//        return getPrimitiveType(Long.class);
        return new PrimitiveType(PrimitiveKind.LONG);
    }

    public static PrimitiveType getBooleanType() {
//        return getPrimitiveType(Boolean.class);
        return new PrimitiveType(PrimitiveKind.BOOLEAN);
    }

    private static PrimitiveType getDoubleType() {
//        return getPrimitiveType(Double.class);
        return new PrimitiveType(PrimitiveKind.DOUBLE);
    }

    private static PrimitiveType getTimeType() {
//        return getPrimitiveType(Date.class);
        return new PrimitiveType(PrimitiveKind.TIME);
    }

    private static PrimitiveType getPasswordType() {
//        return getPrimitiveType(Password.class);
        return new PrimitiveType(PrimitiveKind.PASSWORD);
    }

    private static PrimitiveType getNullType() {
        return new PrimitiveType(PrimitiveKind.NULL);
//        return getPrimitiveType(Null.class);
    }

//    public static PrimitiveType getPrimitiveType(Class<?> klass) {
//        return (PrimitiveType) getTypeFunc.apply(klass);
//    }

}
