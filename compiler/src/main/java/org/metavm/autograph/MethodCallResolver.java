package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.flow.Value;

import java.util.List;

public interface MethodCallResolver {

    List<MethodSignature> getSignatures();

    Value resolve(PsiMethodCallExpression methodCallExpression,
                  ExpressionResolver expressionResolver,
                  MethodGenerator methodGenerator);

}
