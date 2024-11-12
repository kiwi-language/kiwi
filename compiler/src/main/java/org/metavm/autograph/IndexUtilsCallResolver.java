package org.metavm.autograph;

import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.api.lang.Indices;
import org.metavm.flow.NodeRT;
import org.metavm.object.type.Index;
import org.metavm.util.ReflectionUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class IndexUtilsCallResolver implements org.metavm.autograph.MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            org.metavm.autograph.MethodSignature.createStatic(
                    TranspileUtils.createClassType(Indices.class),
                    "count",
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "count", org.metavm.api.Index.class, org.metavm.api.Index.class),
                            1
                    ),
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "count", org.metavm.api.Index.class, org.metavm.api.Index.class),
                            1
                    )
            ),
            org.metavm.autograph.MethodSignature.createStatic(
                    TranspileUtils.createClassType(Indices.class),
                    "scan",
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "scan", org.metavm.api.Index.class, org.metavm.api.Index.class),
                            1
                    ),
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "scan", org.metavm.api.Index.class, org.metavm.api.Index.class),
                            1
                    )
            ),
            org.metavm.autograph.MethodSignature.createStatic(
                    TranspileUtils.createClassType(Indices.class),
                    "select",
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "select", org.metavm.api.Index.class),
                            1
                    )
            ),
            org.metavm.autograph.MethodSignature.createStatic(
                    TranspileUtils.createClassType(Indices.class),
                    "selectFirst",
                    TranspileUtils.createVariableType(
                            ReflectionUtils.getMethod(Indices.class, "selectFirst", org.metavm.api.Index.class),
                            1
                    )
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public NodeRT resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var indexPsiClassType = requireNonNull(((PsiClassType) requireNonNull(
                methodGenerics.getSubstitutor().substitute(method.getTypeParameters()[1])))
                .resolve());
        var methodName = requireNonNull(methodCallExpression.getMethodExpression().getReferenceName());
        var index = requireNonNull(indexPsiClassType.getUserData(org.metavm.autograph.Keys.INDEX));
        var args = methodCallExpression.getArgumentList().getExpressions();
        if (methodName.equals("select")) {
            resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            return methodGenerator.createIndexSelect(index);
        } else if (methodName.equals("selectFirst")) {
            resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            return methodGenerator.createIndexSelectFirst(index);
        } else {
            resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            resolveIndexQueryKey(index, (PsiCallExpression) args[1], expressionResolver);
            return methodName.equals("count") ?
                    methodGenerator.createIndexCount(index) :
                    methodGenerator.createIndexScan(index);
        }
    }

    private void resolveIndexQueryKey(Index index, PsiCallExpression callExpression, org.metavm.autograph.ExpressionResolver expressionResolver) {
        for (int i = 0; i < index.getFields().size(); i++) {
            expressionResolver.resolve(requireNonNull(callExpression.getArgumentList()).getExpressions()[i]);
        }
    }

}
