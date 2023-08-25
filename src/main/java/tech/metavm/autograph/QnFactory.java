package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.AccessMode.*;

public class QnFactory {

    public static QnAndMode getQnAndMode(PsiElement element) {
        return element.getUserData(Keys.QN_AND_MODE);
    }

    public static boolean hasQn(PsiElement element) {
        return getOrCreateQn(element) != null;
    }

    public static QualifiedName getOrCreateQn(PsiElement element) {
        var qnAndMode = element.getUserData(Keys.QN_AND_MODE);
        if (qnAndMode != null) return qnAndMode.qualifiedName();
        var qn = switch (element) {
            case PsiReferenceExpression ref -> getReferenceQn(ref);
            case PsiArrayAccessExpression arrayAccess -> getArrayAccessQn(arrayAccess);
            case PsiThisExpression thisExpr -> getThisExprQn(thisExpr);
            case PsiLocalVariable localVariable -> getLocalVariableQn(localVariable);
            case PsiParameter parameter -> getParameterQn(parameter);
            case PsiField field -> getFieldQn(field, null);
            default -> null;
        };
        if (qn == null) return null;
        qnAndMode = new QnAndMode(qn, getAccessMode(element));
        element.putUserData(Keys.QN_AND_MODE, qnAndMode);
        return qn;
    }

    private static QualifiedName getThisExprQn(PsiThisExpression expression) {
        return new AtomicQualifiedName("this", expression.getType());
    }

    private static QualifiedName getReferenceQn(PsiReferenceExpression reference) {
        var target = reference.resolve();
        return switch (target) {
            case null -> null;
            case PsiField field -> getFieldQn(field, reference.getQualifierExpression());
            case PsiLocalVariable localVariable -> getLocalVariableQn(localVariable);
            case PsiParameter parameter -> getParameterQn(parameter);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        };
    }

    private static QualifiedName getPackageQn(PsiPackage pkg) {
        return pkg.getParentPackage() == null || pkg.getParentPackage().getName() == null ?
                new AtomicQualifiedName(pkg.getName(), null) :
                new AttributeQualifiedName(getPackageQn(pkg.getParentPackage()), pkg.getName(), null);
    }

    private static QualifiedName getArrayAccessQn(PsiArrayAccessExpression arrayAccess) {
        var arrayQn = getOrCreateQn(arrayAccess);
        if (arrayQn != null && arrayAccess.getIndexExpression() instanceof PsiLiteral idxLiteral) {
            return new AttributeQualifiedName(arrayQn, requireNonNull(idxLiteral.getValue()).toString(),
                    arrayAccess.getType());
        } else return null;
    }


    public static AccessMode getAccessMode(PsiElement element) {
        if (element instanceof PsiLocalVariable localVariable) {
            return localVariable.getInitializer() != null ? DEFINE_WRITE : DEFINE;
        }
        if (element instanceof PsiField || element instanceof PsiMethod
                || element instanceof PsiClass || element instanceof PsiParameter) {
            return DEFINE_WRITE;
        }
        var context = element.getContext();
        if (context instanceof PsiAssignmentExpression assignment) {
            return getAssignmentAccMode(assignment, element);
        }
        if (context instanceof PsiPostfixExpression) {
            return READ_WRITE;
        }
        if (context instanceof PsiPrefixExpression prefixExpr) {
            var op = prefixExpr.getOperationSign();
            if (op == JavaTokenType.MINUSMINUS || op == JavaTokenType.PLUSPLUS) return READ_WRITE;
            else return READ;
        } else return READ;
    }

    public static AccessMode getAssignmentAccMode(PsiAssignmentExpression assignment, PsiElement element) {
        if (assignment.getLExpression() == element) {
            return assignment.getOperationSign() == JavaTokenType.EQ ? WRITE : READ_WRITE;
        } else return READ;
    }

    private static QualifiedName getLocalVariableQn(PsiLocalVariable localVariable) {
        return new AtomicQualifiedName(localVariable.getName(), localVariable.getType());
    }

    private static QualifiedName getFieldQn(PsiField field, PsiExpression qualifier) {
        var modifiers = requireNonNull(field.getModifierList());

        var declaringClass = requireNonNull(field.getContainingClass());
        String className = declaringClass.getQualifiedName();
        if (modifiers.hasModifierProperty(PsiModifier.STATIC)) {
            return new AtomicQualifiedName(
                    className + "." + field.getName(), field.getType());
        } else {
            if (qualifier == null) {
                var selfType = TranspileUtil.getTemplateType(requireNonNull(field.getContainingClass()));
                return new AttributeQualifiedName(
                        new AtomicQualifiedName(className + ".this", selfType),
                        field.getName(), field.getType());
            } else {
                var qualifierQn = getOrCreateQn(qualifier);
                if (qualifierQn != null) {
                    return new AttributeQualifiedName(qualifierQn, field.getName(), field.getType());
                } else return null;
            }
        }
    }

    public static QualifiedName getParameterQn(PsiParameter parameter) {
        return new AtomicQualifiedName(parameter.getName(), parameter.getType());
    }

}
