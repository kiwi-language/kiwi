package tech.metavm.autograph;

import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiForeachStatement;
import com.intellij.psi.PsiWhileStatement;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

public class ForeachTransformer extends VisitorBase {

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var scope = requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
        var iterated = Objects.requireNonNull(statement.getIteratedValue());
        var lisType = TranspileUtil.createClassType(List.class);
        var iterationParam = statement.getIterationParameter().getName();
        var isListType = lisType.isAssignableFrom(Objects.requireNonNull(iterated.getType()));
        if (isListType) {
            var listVar = namer.newName("list", scope.getAllDefined());
            var indexVar = namer.newName("i", scope.getAllDefined());
            super.visitForeachStatement(statement);
            var listDeclStmt = TranspileUtil.createStatementFromText(
                    String.format("var %s = %s;", listVar, iterated.getText()));
            var indexDeclStmt = TranspileUtil.createStatementFromText(
                    String.format("int %s = 0;", indexVar)
            );
            insertBefore(indexDeclStmt, statement);
            insertBefore(listDeclStmt, statement);
            var whileStmt = (PsiWhileStatement) TranspileUtil.createStatementFromText(
                    String.format("while (%s < %s.size()) {}", indexVar, listVar)
            );
            if (statement.getBody() != null)
                Objects.requireNonNull(whileStmt.getBody()).replace(statement.getBody());
            var codeBlock = Objects.requireNonNull((PsiBlockStatement) whileStmt.getBody()).getCodeBlock();

            codeBlock.addAfter(
                    TranspileUtil.createStatementFromText(
                            String.format("var %s = %s.get(%s++);", iterationParam, listVar, indexVar)),
                    null
            );
            statement.replace(whileStmt);
        } else {
            super.visitForeachStatement(statement);
        }
    }

}
