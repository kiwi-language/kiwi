package tech.metavm.autograph;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import tech.metavm.expression.ArrayExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.createType;

public class ListOfResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES;

    static {
        var listType = createType(List.class);
        var listClass = requireNonNull(listType.resolve());
        var methods = NncUtils.filter(List.of(listClass.getMethods()),
                m -> m.getModifierList().hasModifierProperty(PsiModifier.STATIC) && m.getName().equals("of"));
        var signatures = new ArrayList<MethodSignature>();
        for (PsiMethod method : methods) {
            signatures.add(TranspileUtil.getSignature(method, listType));
        }
        SIGNATURES = Collections.unmodifiableList(signatures);
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var arrayType = (ArrayType) expressionResolver.getTypeResolver().resolve(
                        methodGenerics.getSubstitutor().substitute(method.getReturnType()));
        var initialValues = NncUtils.map(
                methodCallExpression.getArgumentList().getExpressions(),
                expressionResolver::resolve
        );
        return new NodeExpression(methodGenerator.createNewArray(arrayType, new ArrayExpression(initialValues, arrayType)));
    }
}
