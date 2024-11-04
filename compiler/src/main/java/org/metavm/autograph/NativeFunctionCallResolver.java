package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.flow.Function;
import org.metavm.flow.Value;
import org.metavm.flow.Values;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
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
    public Value resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var function = this.function;
        if (function.isTemplate()) {
            var methodGenerics = methodCallExpression.resolveMethodGenerics();
            var subst = methodGenerics.getSubstitutor();
            var method = (PsiMethod) Objects.requireNonNull(methodGenerics.getElement());
            var typeArgs = NncUtils.map(method.getTypeParameters(),
                    tp -> methodGenerator.getTypeResolver().resolve(subst.substitute(tp))
            );
            function = function.getParameterized(typeArgs);
        }
        var arguments = new ArrayList<Value>();
        if (!signature.isStatic())
            arguments.add(expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()));
        for (PsiExpression expression : methodCallExpression.getArgumentList().getExpressions()) {
            arguments.add(expressionResolver.resolve(expression));
        }
        var convertedArgs = new ArrayList<Value>();
        NncUtils.biForEach(function.getParameters(), arguments, (param, arg) -> {
            if(param.getType().isNotNull() && methodGenerator.getExpressionType(arg.getExpression()).isNullable())
                convertedArgs.add(Values.node(methodGenerator.createNonNull("nonNull", arg)));
            else
                convertedArgs.add(arg);
        });
        var node = methodGenerator.createFunctionCall(function, convertedArgs);
        expressionResolver.setCapturedExpressions(node, null);
        return function.getReturnType().isVoid() ? null : Values.node(node);
    }

}
