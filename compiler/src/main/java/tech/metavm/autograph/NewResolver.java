package tech.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import tech.metavm.expression.Expression;

import java.util.List;

public interface NewResolver {

    List<MethodSignature> getSignatures();

    Expression resolve(PsiNewExpression expression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator);

}
