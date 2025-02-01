package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class MethodReferenceTransformer extends VisitorBase {

    @Override
    public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
        var parentCall = TranspileUtils.getProperParent(expression, PsiNewExpression.class);
        if (parentCall != null) {
            var m = Objects.requireNonNull(parentCall.resolveConstructor());
            if (m.getName().equals("Index") && "org.metavm.api.Index".equals(Objects.requireNonNull(m.getContainingClass()).getQualifiedName())) {
                return;
            }
        }
        var ct = ((PsiClassType) Objects.requireNonNull(expression.getFunctionalInterfaceType()));
        var klass = Objects.requireNonNull(ct.resolve());
        var sam = TranspileUtils.getSAM(klass);
        var paramCount = sam.getParameterList().getParametersCount();
        var qualifier = Objects.requireNonNull(expression.getQualifier());

        var method = (PsiMethod) Objects.requireNonNull(expression.resolve(),
                () -> "Failed to resolve method for call expression: " + expression.getText());
        var qualifiedByClass = !(qualifier instanceof PsiExpression qualExpr) || qualExpr.getType() == null;
        var paramList = buildArgumentList(0, paramCount);
        String callExpr;
        if (qualifiedByClass && !method.getModifierList().hasModifierProperty(PsiModifier.STATIC))
            callExpr = "__arg0__." + method.getName() + buildArgumentList(1, paramCount);
        else
            callExpr = qualifier.getText() + "." + method.getName() + paramList;
        replace(expression, TranspileUtils.createExpressionFromText(paramList + " -> " + callExpr));
    }

    private String buildArgumentList(int offset, int n) {
        var sb = new StringBuilder("(");
        if (n > offset) {
            sb.append("__arg").append(offset).append("__");
            for(int i = offset + 1; i < n; i++) {
                sb.append(", __arg").append(i).append("__");
            }
        }
        sb.append(")");
        return sb.toString();
    }

}
