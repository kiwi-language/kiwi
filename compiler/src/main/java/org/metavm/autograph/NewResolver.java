package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.flow.Value;

import java.util.List;

public interface NewResolver {

    List<MethodSignature> getSignatures();

    Value resolve(PsiNewExpression expression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator);

}
