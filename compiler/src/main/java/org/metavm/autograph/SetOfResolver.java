package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.NodeRT;
import org.metavm.object.type.ClassType;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class SetOfResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES;

    static {
        var setType = TranspileUtils.createClassType(Set.class);
        var setClass = requireNonNull(setType.resolve());
        var methods = NncUtils.filter(List.of(setClass.getMethods()),
                m -> m.getModifierList().hasModifierProperty(PsiModifier.STATIC) && m.getName().equals("of"));
        var signatures = new ArrayList<MethodSignature>();
        for (PsiMethod method : methods) {
            signatures.add(TranspileUtils.getSignature(method, setType));
        }
        SIGNATURES = Collections.unmodifiableList(signatures);
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public NodeRT resolve(PsiMethodCallExpression methodCallExpression,
                          ExpressionResolver expressionResolver,
                          MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var setType = (ClassType) expressionResolver.getTypeResolver().resolve(
                methodGenerics.getSubstitutor().substitute(method.getReturnType()));
        var hashSetKlass = StdKlass.hashSet.get().getParameterized(List.of(setType.getFirstTypeArgument()));
        var set = methodGenerator.createNew(
                hashSetKlass.getDefaultConstructor(),
                false,
                true);
        var addMethod = hashSetKlass.getMethod(m -> m.getEffectiveVerticalTemplate() == StdMethod.hashSetAdd.get());
        for (PsiExpression expression : methodCallExpression.getArgumentList().getExpressions()) {
            methodGenerator.createDup();
            expressionResolver.resolve(expression);
            methodGenerator.createMethodCall(addMethod);
            methodGenerator.createPop();
        }
        return set;
    }
}
