package tech.metavm.transpile;

import tech.metavm.util.ValuePlaceholder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static tech.metavm.transpile.JavaParser.LambdaExpressionContext;

public class LambdaTypeMatchUtil {

    public static boolean match(LambdaType lambdaType, Type functionType) {
        if(!FunctionUtil.isFunctionalInterface(functionType)) {
            return false;
        }
        List<Type> functionParameterTypes = resolveFunctionParameterTypes(functionType);
        if(lambdaType.parameterTypes().size() != functionParameterTypes.size() ||
                lambdaType.hasReturnType() != hasReturnType(functionType)) {
            return false;
        }
        for (int i = 0; i < lambdaType.parameterTypes().size(); i++) {
            if(!isAssignable(functionParameterTypes.get(i), lambdaType.parameterTypes().get(i))) {
                return false;
            }
        }
        return true;
    }

    public static LambdaType getLambdaType(LambdaExpressionContext lambda) {
        return new LambdaType(resolveParameterTypes(lambda), hasReturnType(lambda));
    }

    private static boolean isAssignable(Type from, Type to) {
        return eraseType(to).isAssignableFrom(eraseType(from));
    }

    private static Class<?> eraseType(Type type) {
        if(type instanceof Class<?> klass) {
            return klass;
        }
        if(type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        }
        return Object.class;
    }

    private static List<Type> resolveFunctionParameterTypes(Type functionType) {
        return FunctionUtil.getParameterType(eraseType(functionType));
    }

    private static boolean hasReturnType(Type functionType) {
        return FunctionUtil.getReturnType(eraseType(functionType)) != void.class;
    }

    private static List<Type> resolveParameterTypes(LambdaExpressionContext lambda) {
        return List.of();
    }

    public static boolean hasReturnType(LambdaExpressionContext lambda) {
        if(lambda.lambdaBody().expression() != null) {
            return true;
        }
        else {
            var holder = new ValuePlaceholder<Boolean>();
            StatementContextWalker.walk(
                    lambda.lambdaBody().block(),
                    stmt -> {
                        if(stmt.RETURN() != null) {
                            holder.set(stmt.expression().size () > 0);
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
            );
            return holder.isSet() && holder.get();
        }
    }

}
