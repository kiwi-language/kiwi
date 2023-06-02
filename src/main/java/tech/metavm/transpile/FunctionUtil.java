package tech.metavm.transpile;

import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class FunctionUtil {

    public static List<Type> getParameterType(Class<?> functionalInterface) {
        Method functionMethod = getFunctionMethod(functionalInterface);
        return Arrays.asList(functionMethod.getParameterTypes());
    }

    public static Type getReturnType(Class<?> functionInterface) {
        return getFunctionMethod(functionInterface).getGenericReturnType();
    }

    public static boolean isFunctionalInterface(Type type) {
        Class<?> klass = ReflectUtils.eraseToClass(type);
        if(!klass.isInterface()) {
            return false;
        }
        List<Method> abstractMethods = NncUtils.filter(
                Arrays.asList(klass.getMethods()),
                FunctionUtil::isAbstractMethod
        );
        return abstractMethods.size() == 1;
    }

    public static Method getFunctionMethod(Class<?> functionInterface) {
        if(!functionInterface.isInterface()) {
            throw new InternalException("Class " + functionInterface.getName() + " is not an interface");
        }
        List<Method> abstractMethods = NncUtils.filter(
                Arrays.asList(functionInterface.getMethods()),
                FunctionUtil::isAbstractMethod
        );
        if(abstractMethods.size() != 1) {
            throw new InternalException("'" + functionInterface.getName() + "' is not a functional interface. " +
                    "Functional interface must have exactly one abstract method");
        }
        return abstractMethods.get(0);
    }

    private static boolean isAbstractMethod(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

}
