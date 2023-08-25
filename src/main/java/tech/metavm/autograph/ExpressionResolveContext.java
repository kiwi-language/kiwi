package tech.metavm.autograph;

import com.intellij.psi.PsiExpression;
import tech.metavm.expression.Expression;

public interface ExpressionResolveContext {

    Expression substitute(PsiExpression expression);

}
