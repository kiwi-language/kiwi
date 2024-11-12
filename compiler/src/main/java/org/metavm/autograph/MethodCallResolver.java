package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.flow.NodeRT;

import java.util.List;

public interface MethodCallResolver {

    List<MethodSignature> getSignatures();

    NodeRT resolve(PsiMethodCallExpression methodCallExpression,
                   ExpressionResolver expressionResolver,
                   MethodGenerator methodGenerator);

}
