package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.flow.Function;
import org.metavm.flow.Node;
import org.metavm.object.type.Type;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Objects;

public class NativeFunctionCallResolver implements MethodCallResolver {

    private final MethodSignature signature;
    private final Function function;

    public NativeFunctionCallResolver(MethodSignature signature, Function function) {
        this.signature = signature;
        this.function = function;
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return List.of(signature);
    }

    @Override
    public Node resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var function = this.function;
        List<Type> typeArgs;
        if (function.isTemplate()) {
            var methodGenerics = methodCallExpression.resolveMethodGenerics();
            var subst = methodGenerics.getSubstitutor();
            var method = (PsiMethod) Objects.requireNonNull(methodGenerics.getElement());
            typeArgs = Utils.map(method.getTypeParameters(),
                    tp -> methodGenerator.getTypeResolver().resolve(subst.substitute(tp))
            );
        }
        else
            typeArgs = List.of();
        var paramIt = function.getParameters().iterator();
        if (!signature.isStatic()) {
            expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression());
            if(paramIt.next().getType().isNotNull())
                methodGenerator.createNonNull();
        }
        for (PsiExpression expression : methodCallExpression.getArgumentList().getExpressions()) {
            expressionResolver.resolve(expression);
            if(paramIt.next().getType().isNotNull())
                methodGenerator.createNonNull();
        }
        return methodGenerator.createInvokeFunction(function, typeArgs);
    }

}
