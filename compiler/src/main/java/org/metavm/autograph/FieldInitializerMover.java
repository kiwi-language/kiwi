package org.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiStatement;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FieldInitializerMover extends VisitorBase {

    private final Set<PsiStatement> movedInitializers = new HashSet<>();

    @Override
    public void visitField(PsiField field) {
        super.visitField(field);
        if(!TranspileUtils.isStatic(field) && field.getInitializer() != null) {
            var initializer = field.getInitializer();
            initializer.accept(new InitializerTransformer());
            var klass = requireNonNull(field.getContainingClass());
            for (PsiMethod c : klass.getConstructors()) {
                var body = requireNonNull(c.getBody());
                var statements = body.getStatements();
                if(statements.length > 0 && TranspileUtils.isThisCall(statements[0]))
                    continue;
                PsiStatement anchor;
                if(statements.length > 0) {
                    int i = TranspileUtils.isSuperCall(statements[0]) ? 1 : 0;
                    //noinspection StatementWithEmptyBody
                    for (; i < statements.length && movedInitializers.contains(statements[i]); i++);
                    anchor = i > 0 ? statements[i-1] : null;
                }
                else
                    anchor = null;
                movedInitializers.add(
                        (PsiStatement) body.addAfter(
                                TranspileUtils.createStatementFromText(
                                        "this." + field.getName() + "=" + initializer.getText() + ";"
                                ),
                                anchor
                        )
                );
            }
            field.setInitializer(null);
        }
    }

    private static class InitializerTransformer extends VisitorBase {

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            super.visitReferenceExpression(expression);
            if(expression.getQualifier() == null && expression.resolve() instanceof PsiField field) {
                var klass = requireNonNull(field.getContainingClass());
                if(TranspileUtils.isStatic(field))
                    expression.setQualifierExpression(TranspileUtils.createExpressionFromText(klass.getQualifiedName()));
                else
                    expression.setQualifierExpression(TranspileUtils.createExpressionFromText("this"));
            }
        }
    }

}
