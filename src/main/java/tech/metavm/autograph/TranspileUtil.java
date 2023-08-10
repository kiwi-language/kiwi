package tech.metavm.autograph;

import com.intellij.psi.*;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class TranspileUtil {

    public static boolean isNameExpression(PsiExpression expression) {
        return expression instanceof PsiReferenceExpression ||
                expression instanceof PsiArrayAccessExpression;
    }

    public static PsiElement getTryStatementEntry(PsiTryStatement statement) {
        if(statement.getResourceList() != null) {
            return statement.getResourceList().iterator().next();
        }
        else {
            var body = requireNonNull(statement.getTryBlock()).getStatements();
            return body.length > 0 ? body[0] : null;
        }
    }

    public static PsiElement getCatchSectionEntry(PsiCatchSection catchSection) {
        var body = requireNonNull(catchSection.getCatchBlock()).getStatements();
        return body.length > 0 ? body[0] : null;
    }

    public static @Nullable PsiElement getForStatementEntry(PsiForStatement statement) {
        if(statement.getCondition() != null) return statement.getCondition();
        if(statement.getBody() != null) {
            if(statement.getBody() instanceof PsiBlockStatement block) {
                if(block.getCodeBlock().getStatements().length > 0) {
                    return block.getCodeBlock().getStatements()[0];
                }
            }
            else return statement.getBody();
        }
        return statement.getUpdate();
    }

}
