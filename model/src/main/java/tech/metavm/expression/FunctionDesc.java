package tech.metavm.expression;

import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

public class FunctionDesc {

    private final Func function;
    private final Method method;
    private final Method resultTypeResolverMethod;

    public FunctionDesc(Func function) {
        this.function = function;
        List<Class<?>> instanceParamTypes = NncUtils.map(
                function.getParameterTypes(),
                Instances::getInstanceClassByJavaClass
        );
        method = ReflectionUtils.getMethod(FunctionMethods.class, function.code(), instanceParamTypes);
        resultTypeResolverMethod =
                EntityUtils.tryGetMethodByName(FunctionDesc.class, typeResolverMethodName(function.code()));
    }

    private static String typeResolverMethodName(String functionName) {
        return functionName + "$_TYPE_RESOLVER";
    }

    public Func getFunction() {
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
            return (Type) ReflectionUtils.invoke(null, resultTypeResolverMethod, argumentTypes);
        }
        else {
            return Instances.getTypeByInstanceClass(method.getReturnType());
        }
    }

    public void checkArguments(List<Instance> arguments) {
        Class<?>[] paramTypes = getParamTypes();
        if(arguments.size() != paramTypes.length) {
            throw BusinessException.invalidFuncArguments(function);
        }
//        int i = 0;
//        for (Instance argument : arguments) {
//            if(argument != null && !InstanceUtils.getTypeByInstanceClass(paramTypes[i]).isInstance(argument)) {
//                throw BusinessException.invalidFuncArguments(function);
//            }
//            i++;
//        }
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
