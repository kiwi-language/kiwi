package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import org.metavm.expression.Expression;

public interface ExpressionResolveContext {

    Expression substitute(PsiExpression expression);

}
