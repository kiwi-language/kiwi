package tech.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.object.type.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;

public class Flows {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static Type getExpressionType(Expression expression, @Nullable NodeRT prev, ScopeRT scope) {
        var exprTypeMap = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
        return exprTypeMap.getType(expression);
    }

    public static void retransformFlowIfRequired(Flow flow, IEntityContext context) {
        if (flow instanceof Method method) {
            if (method.getDeclaringType().isTemplate() && context.isPersisted(method.getDeclaringType())) {
                flow.analyze();
                var templateInstances = context.getTemplateInstances(method.getDeclaringType());
                for (ClassType templateInstance : templateInstances) {
                    context.getGenericContext().retransformMethod(method, templateInstance);
                }
            }
        }
        if (flow.isTemplate()) {
            for (Flow horizontalInstance : context.selectByKey(Flow.IDX_HORIZONTAL_TEMPLATE, flow)) {
                if (DebugEnv.debugging)
                    debugLogger.info("retransforming " + EntityUtils.getEntityPath(horizontalInstance));
                context.getGenericContext().retransformHorizontalFlowInstances(flow, horizontalInstance);
            }
        }
    }

    public static Flow getParameterizedFlow(Flow template, List<Type> typeArguments, ResolutionStage stage, SaveTypeBatch batch) {
        return batch.getContext().getGenericContext().getParameterizedFlow(template, typeArguments, stage, batch);
    }

    public static boolean isConstructor(Flow flow) {
        return flow instanceof Method method && method.isConstructor();
    }

    public static @Nullable ClassType getDeclaringType(Flow flow) {
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
