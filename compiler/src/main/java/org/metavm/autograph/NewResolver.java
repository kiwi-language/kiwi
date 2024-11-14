package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.flow.Node;

import java.util.List;

public interface NewResolver {

    List<MethodSignature> getSignatures();

    Node resolve(PsiNewExpression expression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator);

}
