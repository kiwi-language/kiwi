package tech.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Expressions;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ListOfResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES;

    static {
        var listType = TranspileUtil.createClassType(List.class);
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
        var listType = (ClassType) expressionResolver.getTypeResolver().resolve(
                methodGenerics.getSubstitutor().substitute(method.getReturnType()));
        var readWriteListType = StandardTypes.getReadWriteListKlass().getParameterized(List.of(listType.getListElementType()));
        var list = methodGenerator.createNew(
                readWriteListType.getDefaultConstructor(),
                List.of(),
                true
        );
        for (PsiExpression expression : methodCallExpression.getArgumentList().getExpressions()) {
            var value = expressionResolver.resolve(expression);
            var addMethod = readWriteListType.getMethodByCodeAndParamTypes("add", List.of(listType.getListElementType()));
            methodGenerator.createMethodCall(
                    Expressions.node(list),
                    addMethod,
                    List.of(value)
            );
        }
        return Expressions.node(list);
    }
}
