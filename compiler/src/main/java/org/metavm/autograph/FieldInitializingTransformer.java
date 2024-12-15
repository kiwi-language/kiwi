package org.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FieldInitializingTransformer extends VisitorBase {

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if (method.isConstructor()) {
            var block = requireNonNull(method.getBody());
            var firstStmt = block.getStatements().length > 0 ? block.getStatements()[0] : null;
            if (TranspileUtils.isThisCall(firstStmt))
                return;
            PsiStatement anchor = TranspileUtils.isSuperCall(firstStmt) ? firstStmt : null;
            var declaringClass = Objects.requireNonNull(method.getContainingClass());
            for (PsiField field : declaringClass.getFields()) {
                if (!TranspileUtils.isStatic(field)) {
                    var initializer = field.getUserData(Keys.INITIALIZER);
                    if (initializer != null) {
                        anchor = (PsiStatement) block.addAfter(
                                TranspileUtils.createStatementFromText(
                                        "this." + field.getName() + " = " + initializer.getName() + "();"
                                ),
                                anchor
                        );
                    }
                }
            }
        }
    }
}
