package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.flow.Node;

import java.util.List;

public interface MethodCallResolver {

    List<MethodSignature> getSignatures();

    Node resolve(PsiMethodCallExpression methodCallExpression,
                 ExpressionResolver expressionResolver,
                 MethodGenerator methodGenerator);

}
