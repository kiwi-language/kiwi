package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class VarargsTransformer extends VisitorBase {

    @Override
    public void visitCallExpression(PsiCallExpression expression) {
        super.visitCallExpression(expression);
        var method = expression.resolveMethod();
        if (method != null && method.isVarArgs()) {
            var args = requireNonNull(expression.getArgumentList()).getExpressions();
            var params = method.getParameterList().getParameters();
            var varargParam = params[params.length - 1];
            if (args.length == params.length && varargParam.getType().isAssignableFrom(
                    requireNonNull(args[args.length - 1].getType())))
                return;
            var ordinaryArgs = Arrays.copyOf(args, params.length - 1);
            var varargs = Arrays.copyOfRange(args, params.length - 1, args.length);
            var textBuf = new StringBuilder("method(");
            for (PsiExpression ordinaryArg : ordinaryArgs) {
                textBuf.append(ordinaryArg.getText()).append(", ");
            }
            var varargType = ((PsiEllipsisType) varargParam.getType()).getComponentType();
            textBuf.append(String.format("new %s[] {", varargType.getCanonicalText()))
                    .append(NncUtils.join(List.of(varargs), PsiElement::getText, ", "))
                    .append("}").append(")");
            var dummyCallExpr = (PsiMethodCallExpression) TranspileUtil.createExpressionFromText(textBuf.toString());
            replace(expression.getArgumentList(), dummyCallExpr.getArgumentList());
        }
    }
}
