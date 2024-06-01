package tech.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.expression.Expression;
import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

public class Flows {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static Type getExpressionType(Expression expression, @Nullable NodeRT prev, ScopeRT scope) {
        var exprTypeMap = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
        return exprTypeMap.getType(expression);
    }

    public static boolean isConstructor(Flow flow) {
        return flow instanceof Method method && method.isConstructor();
    }

    public static @Nullable Klass getDeclaringType(Flow flow) {
        return flow instanceof Method method ? method.getDeclaringType() : null;
    }


    public static boolean isInstanceMethod(Flow flow) {
        return flow instanceof Method method && method.isInstanceMethod();
    }

    public static FunctionType getStaticType(Flow flow) {
        if (flow instanceof Method method && method.isInstanceMethod())
            return method.getStaticType();
        else
            throw new InternalException("Can not get static type of flow: " + flow);
    }
}
