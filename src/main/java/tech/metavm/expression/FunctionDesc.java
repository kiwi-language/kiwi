package tech.metavm.expression;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.List;

public class FunctionDesc {

    private final Function function;
    private final Method method;
    private final Method resultTypeResolverMethod;

    public FunctionDesc(Function function) {
        this.function = function;
        List<Class<?>> instanceParamTypes = NncUtils.map(
                function.getParameterTypes(),
                InstanceUtils::getInstanceClassByJavaClass
        );
        method = ReflectUtils.getMethod(FunctionMethods.class, function.code(), instanceParamTypes);
        resultTypeResolverMethod =
                ReflectUtils.tryGetMethodByName(FunctionDesc.class, typeResolverMethodName(function.code()));
    }

    private static String typeResolverMethodName(String functionName) {
        return functionName + "$_TYPE_RESOLVER";
    }

    public Function getFunction() {
        return function;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParamTypes() {
        return method.getParameterTypes();
    }

    public Type getReturnType(List<Type> argumentTypes) {
        if(resultTypeResolverMethod != null) {
            return (Type) ReflectUtils.invoke(null, resultTypeResolverMethod, argumentTypes);
        }
        else {
            return InstanceUtils.getTypeByInstanceClass(method.getReturnType());
        }
    }

    public void checkArguments(List<Instance> arguments) {
        Class<?>[] paramTypes = getParamTypes();
        if(arguments.size() != paramTypes.length) {
            throw BusinessException.invalidFuncArguments(function);
        }

        int i = 0;
        for (Instance argument : arguments) {
            if(argument != null && !InstanceUtils.getTypeByInstanceClass(paramTypes[i]).isInstance(argument)) {
                throw BusinessException.invalidFuncArguments(function);
            }
            i++;
        }
    }

    public Instance evaluate(List<Instance> arguments) {
        checkArguments(arguments);
        try {
            Object[] args = new Object[arguments.size()];
            arguments.toArray(args);
            return (Instance) method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("Fail to evaluate function " + function.name(), e);
        }
    }

}
