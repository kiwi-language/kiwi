package tech.metavm.autograph;

import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.IndexUtils;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;
import tech.metavm.flow.IndexQueryKey;
import tech.metavm.flow.IndexQueryKeyItem;
import tech.metavm.flow.Values;
import tech.metavm.object.type.Index;
import tech.metavm.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class IndexUtilsCallResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(IndexUtils.class),
                    "count",
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "count", tech.metavm.entity.Index.class, tech.metavm.entity.Index.class),
                            1
                    ),
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "count", tech.metavm.entity.Index.class, tech.metavm.entity.Index.class),
                            1
                    )
            ),
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(IndexUtils.class),
                    "scan",
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "scan", tech.metavm.entity.Index.class, tech.metavm.entity.Index.class),
                            1
                    ),
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "scan", tech.metavm.entity.Index.class, tech.metavm.entity.Index.class),
                            1
                    )
            ),
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(IndexUtils.class),
                    "select",
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "select", tech.metavm.entity.Index.class),
                            1
                    )
            ),
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(IndexUtils.class),
                    "selectFirst",
                    TranspileUtil.createTypeVariableType(
                            ReflectionUtils.getMethod(IndexUtils.class, "selectFirst", tech.metavm.entity.Index.class),
                            1
                    )
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var indexPsiClassType = requireNonNull(((PsiClassType) requireNonNull(
                methodGenerics.getSubstitutor().substitute(method.getTypeParameters()[1])))
                .resolve());
        var methodName = requireNonNull(methodCallExpression.getMethodExpression().getReferenceName());
        var index = requireNonNull(indexPsiClassType.getUserData(Keys.INDEX));
        var args = methodCallExpression.getArgumentList().getExpressions();
        if (methodName.equals("select")) {
            var key = resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            return new NodeExpression(methodGenerator.createIndexSelect(index, key));
        } else if (methodName.equals("selectFirst")) {
            var key = resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            return new NodeExpression(methodGenerator.createIndexSelectFirst(index, key));
        } else {
            var from = resolveIndexQueryKey(index, (PsiCallExpression) args[0], expressionResolver);
            var to = resolveIndexQueryKey(index, (PsiCallExpression) args[1], expressionResolver);
            var node = methodName.equals("count") ?
                    methodGenerator.createIndexCount(index, from, to) :
                    methodGenerator.createIndexScan(index, from, to);
            return new NodeExpression(node);
        }
    }

    private IndexQueryKey resolveIndexQueryKey(Index index, PsiCallExpression callExpression, ExpressionResolver expressionResolver) {
        var items = new ArrayList<IndexQueryKeyItem>();
        for (int i = 0; i < index.getFields().size(); i++) {
            var indexField = index.getFields().get(i);
            var valueExpr = expressionResolver.resolve(requireNonNull(callExpression.getArgumentList()).getExpressions()[i]);
            items.add(new IndexQueryKeyItem(indexField, Values.expression(valueExpr)));
        }
        return new IndexQueryKey(index, items);
    }

}
