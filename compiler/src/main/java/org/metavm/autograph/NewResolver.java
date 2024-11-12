package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.flow.NodeRT;

import java.util.List;

public interface NewResolver {

    List<MethodSignature> getSignatures();

    NodeRT resolve(PsiNewExpression expression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator);

}
