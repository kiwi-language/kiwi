package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.InternalException;
import org.metavm.util.NamingUtils;

import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
public class NewIndexTransformer extends VisitorBase {

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (TranspileUtils.isIndexType(expression.getType())) {
            var context = (PsiMember) TranspileUtils.getParent(expression,
                    Set.of(PsiMethod.class, PsiClassInitializer.class, PsiField.class));
            if (!TranspileUtils.isStatic(context))
                throw new InternalException("IndexMap can only be created in a static context");
            var classType = (PsiClassType) expression.getType();
            var keyType = classType.getParameters()[0];
            var argList = Objects.requireNonNull(expression.getArgumentList());
            if (argList.getExpressionCount() == 2) {
                argList.addBefore(
                        TranspileUtils.createExpressionFromText("\"" + getField(expression).getName() + "\""),
                        argList.getExpressions()[0]
                );
            }
            var computer = argList.getExpressions()[argList.getExpressionCount() - 1];
            if (computer instanceof PsiLambdaExpression lambdaExpr) {
                var name = (String) TranspileUtils.getConstant(argList.getExpressions()[0]);
                var klass = TranspileUtils.getParent(expression, PsiClass.class);
                var valueType = classType.getParameters()[1];
                var computeMethod = createKeyComputeMethod(name, keyType, valueType, lambdaExpr, klass);
                computer.replace(TranspileUtils.createExpressionFromText(klass.getName() + "::" + computeMethod.getName()));
            }
        }
    }

    private PsiField getField(PsiNewExpression expression) {
        if (expression.getParent() instanceof PsiField f)
            return f;
        else if (expression.getParent() instanceof PsiAssignmentExpression assignment
                && assignment.getLExpression() instanceof PsiReferenceExpression ref
                && ref.resolve() instanceof PsiField f) {
            return f;
        }
        else
            throw new InternalException("Invalid context for IndexMap creation: " + expression.getParent());
    }

    private PsiMethod createKeyComputeMethod(String name, PsiType keyType, PsiType valueType,
                                             PsiLambdaExpression lambdaExpression, PsiClass psiClass) {
        var paramName = requireNonNull(lambdaExpression.getParameterList().getParameter(0)).getName();
        var text = "private static " + keyType.getCanonicalText() + " $compute" + NamingUtils.firstCharToUpperCase(name)
                + "Key(" + valueType.getCanonicalText() + " " + paramName + ") {}";
        var method = TranspileUtils.createMethodFromText(text);
        var body = requireNonNull(method.getBody());
        if (lambdaExpression.getBody() instanceof PsiExpression expr) {
            body.add(TranspileUtils.createStatementFromText("return " + expr.getText() + ";"));
        } else {
            var block = (PsiCodeBlock) requireNonNull(lambdaExpression.getBody());
            for (PsiStatement stmt : block.getStatements()) {
                body.add(stmt.copy());
            }
        }
        return (PsiMethod) psiClass.addBefore(method, null);
    }

}
