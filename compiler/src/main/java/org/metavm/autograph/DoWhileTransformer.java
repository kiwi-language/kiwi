package org.metavm.autograph;

import com.intellij.psi.PsiDoWhileStatement;

import java.util.Objects;

public class DoWhileTransformer extends VisitorBase {

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        super.visitDoWhileStatement(statement);
        var body = Objects.requireNonNull(statement.getBody());
        insertBefore(body, statement);
        var whileStmt = TranspileUtils.createStatementFromText(
                "while (" + Objects.requireNonNull(statement.getCondition()).getText() + ")"
                + body.getText()
        );
        replace(statement, whileStmt);
    }

}
