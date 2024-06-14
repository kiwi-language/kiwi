package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.expression.Expression;

import java.util.List;

public interface NewResolver {

    List<MethodSignature> getSignatures();

    Expression resolve(PsiNewExpression expression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator);

}
