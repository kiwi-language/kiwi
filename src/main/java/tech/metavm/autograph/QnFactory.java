package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.AccessMode.*;

public class QnFactory {

    public static QnAndMode getQnAndMode(PsiElement element) {
        getQn(element);
        return element.getUserData(Keys.QN_AND_MODE);
    }

    public static boolean hasQn(PsiElement element) {
        return getQn(element) != null;
    }

    public static QualifiedName getQn(PsiElement element) {
        var qnAndMode = element.getUserData(Keys.QN_AND_MODE);
        if (qnAndMode != null) return qnAndMode.qualifiedName();
        var qn = switch (element) {
            case PsiReferenceExpression reference -> getReferenceQn(reference);
            case PsiArrayAccessExpression arrayAccess -> getArrayAccessQn(arrayAccess);
            case PsiLocalVariable localVariable -> getLocalVariableQn(localVariable);
            case PsiParameter parameter -> getParameterQn(parameter);
            case PsiField field -> getFieldQn(field, null);
            default -> null;
        };
        if(qn == null) return null;
        qnAndMode = new QnAndMode(qn, getAccessMode(element));
        element.putUserData(Keys.QN_AND_MODE, qnAndMode);
        return qn;
    }

    private static QualifiedName getArrayAccessQn(PsiArrayAccessExpression arrayAccess) {
        var arrayQn = getQn(arrayAccess);
        if (arrayQn != null && arrayAccess.getIndexExpression() instanceof PsiLiteral idxLiteral) {
            return new AttributeQualifiedName(arrayQn, requireNonNull(idxLiteral.getValue()).toString());
        } else return null;
    }

    public static QualifiedName getReferenceQn(PsiReferenceExpression reference) {
        var target = requireNonNull(reference.resolve());
        return switch (target) {
            case PsiField field -> getFieldQn(field, reference.getQualifier());
            case PsiLocalVariable localVariable -> getLocalVariableQn(localVariable);
            case PsiParameter parameter -> getParameterQn(parameter);
            case PsiMethod method -> getMethodQn(method);
            case PsiClass klass -> getClassQn(klass);
            default -> throw new IllegalStateException("Unexpected value: " + target);
        };
    }

    public static AccessMode getAccessMode(PsiElement element) {
        if(element instanceof PsiLocalVariable localVariable) {
            return localVariable.getInitializer() != null ? DEFINE_WRITE : DEFINE;
        }
        if(element instanceof PsiField || element instanceof PsiMethod
                || element instanceof PsiClass || element instanceof PsiParameter) {
            return DEFINE_WRITE;
        }
        var context = element.getContext();
        if(context instanceof PsiAssignmentExpression assignment) {
            return getAssignmentAccMode(assignment, element);
        }
        if(context instanceof PsiPostfixExpression) {
            return READ_WRITE;
        }
        if(context instanceof PsiPrefixExpression prefixExpr) {
            var op = prefixExpr.getOperationSign();
            if(op == JavaTokenType.MINUSMINUS || op == JavaTokenType.PLUSPLUS) return READ_WRITE;
            else return READ;
        }
        else return READ;
    }

    public static AccessMode getAssignmentAccMode(PsiAssignmentExpression assignment, PsiElement element) {
        if(assignment.getLExpression() == element) {
            return assignment.getOperationSign() == JavaTokenType.EQ ? WRITE : READ_WRITE;
        }
        else return READ;
    }

    private static QualifiedName getLocalVariableQn(PsiLocalVariable localVariable) {
        return new AtomicQualifiedName(localVariable.getName());
    }

    private static QualifiedName getFieldQn(PsiField field, PsiElement qualifier) {
        var modifiers = requireNonNull(field.getModifierList());
        if (modifiers.hasModifierProperty(PsiModifier.STATIC)) {
            return new AttributeQualifiedName(
                    getClassQn(requireNonNull(field.getContainingClass())), field.getName());
        } else {
            if (qualifier == null) {
                return new AttributeQualifiedName(
                        new AttributeQualifiedName(getClassQn(requireNonNull(field.getContainingClass())),
                                "this"), field.getName());
            } else {
                var qualifierQn = getQn(qualifier);
                if (qualifierQn != null) return new AttributeQualifiedName(qualifierQn, field.getName());
                else return null;
            }
        }
    }

    public static QualifiedName getParameterQn(PsiParameter parameter) {
        return new AtomicQualifiedName(parameter.getName());
    }

    public static QualifiedName getMethodQn(PsiMethod method) {
        return new MethodQualifiedName(
                getClassQn(requireNonNull(method.getContainingClass())),
                method.getName(),
                getParameterQns(method.getParameterList())
        );
    }

    public static QualifiedName getClassQn(PsiClass psiClass) {
        if (psiClass.getContainingClass() != null) {
            return new AttributeQualifiedName(getClassQn(psiClass.getContainingClass()), psiClass.getName());
        } else {
            return new AtomicQualifiedName(psiClass.getQualifiedName());
        }
    }

    private static List<QualifiedName> getParameterQns(PsiParameterList parameterList) {
        List<QualifiedName> qualifiedNames = new ArrayList<>();
        for (PsiParameter parameter : parameterList.getParameters()) {
            qualifiedNames.add(getTypeQn(parameter.getType()));
        }
        return qualifiedNames;
    }

    public static QualifiedName getTypeQn(PsiType psiType) {
        return switch (psiType) {
            case PsiClassType classType -> getClassTypeRefQn(classType);
            case PsiPrimitiveType primitiveType -> getPrimitiveTypeQn(primitiveType);
            case PsiArrayType arrayType -> getArrayTypeQn(arrayType);
            default -> throw new IllegalStateException("Unexpected value: " + psiType);
        };
    }

    private static QualifiedName getPrimitiveTypeQn(PsiPrimitiveType primitiveType) {
        return new AtomicQualifiedName(primitiveType.getName());
    }

    private static QualifiedName getArrayTypeQn(PsiArrayType arrayType) {
        return new AttributeQualifiedName(getTypeQn(arrayType.getComponentType()), "[]");
    }

    private static QualifiedName getClassTypeRefQn(PsiClassType classType) {
        var psiClass = requireNonNull(classType.resolve());
        if (classType.getParameterCount() == 0) return getClassQn(psiClass);
        else {
            return new PTypeQualifiedName(
                    getClassQn(psiClass),
                    NncUtils.map(classType.getParameters(), QnFactory::getTypeQn)
            );
        }
    }


}
