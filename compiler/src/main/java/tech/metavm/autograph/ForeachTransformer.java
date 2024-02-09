package tech.metavm.autograph;

import com.intellij.psi.*;

import java.util.List;
import java.util.Objects;

public class ForeachTransformer extends VisitorBase {

    private final NameTracker nameTracker = new NameTracker();

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {

        var iterated = Objects.requireNonNull(statement.getIteratedValue());
        var lisType = TranspileUtil.createType(List.class);
        var iterationParam = statement.getIterationParameter().getName();
        var isListType = lisType.isAssignableFrom(Objects.requireNonNull(iterated.getType()));
        if (isListType) {
            var listVar = nameTracker.nextName("list");
            var indexVar = nameTracker.nextName("i");
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

    @Override
    public void visitVariable(PsiVariable variable) {
        if (!(variable instanceof PsiMember))
            nameTracker.addName(variable.getName());
        super.visitVariable(variable);
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        nameTracker.enterBlock();
        super.visitCodeBlock(block);
        nameTracker.exitBlock();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        nameTracker.enterMethod();
        super.visitMethod(method);
        nameTracker.exitMethod();
    }
}
