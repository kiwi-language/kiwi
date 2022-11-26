package tech.metavm.object.instance.query;

import tech.metavm.util.BusinessException;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Method;

public class FunctionDesc {

    private final Function function;
    private final Method method;

    public FunctionDesc(Function function) {
        this.function = function;
        method = ReflectUtils.getMethodByName(FunctionMethods.class, function.name());
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

    public Class<?> getResultType() {
        return method.getReturnType();
    }

    public void checkArguments(Object...arguments) {
        Class<?>[] paramTypes = getParamTypes();
        if(arguments.length != paramTypes.length) {
            throw BusinessException.invalidFuncArguments(function);
        }
        for(int i = 0; i < arguments.length; i++) {
            if(arguments[i] != null && !paramTypes[i].isInstance(arguments[i])) {
                throw BusinessException.invalidFuncArguments(function);
            }
        }
    }

    public Object evaluate(Object...arguments) {
        checkArguments(arguments);
        try {
            return method.invoke(null, arguments);
        } catch (Exception e) {
            throw new RuntimeException("Fail to evaluate function " + function.name(), e);
        }
    }

}
