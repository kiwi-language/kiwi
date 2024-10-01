package org.metavm.autograph;

import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

import java.util.Objects;

public class SuperCallInserter extends VisitorBase {

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if(method.isConstructor()) {
            var klass = Objects.requireNonNull(method.getContainingClass());
            var superClass = klass.getSuperClass();
            if(superClass == null
                    || TranspileUtils.isObjectClass(superClass) || TranspileUtils.isEnumClass(superClass))
                return;
            var body = Objects.requireNonNull(method.getBody());
            if (body.getStatements().length > 0) {
                var firstStmt = body.getStatements()[0];
                if (firstStmt instanceof PsiExpressionStatement exprStmt && exprStmt.getExpression() instanceof PsiMethodCallExpression callExpr) {
                    var refName = callExpr.getMethodExpression().getReferenceName();
                    if ("this".equals(refName) || "super".equals(refName))
                        return;
                }
            }
            body.addAfter(TranspileUtils.createStatementFromText("super();"), null);
        }
    }
}
