package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;

import java.util.List;

public interface MethodCallResolver {

    List<MethodSignature> getSignatures();

    Expression resolve(PsiMethodCallExpression methodCallExpression,
                       ExpressionResolver expressionResolver,
                       MethodGenerator methodGenerator);

}
