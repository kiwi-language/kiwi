package tech.metavm.autograph;

import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.psi.*;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class TranspileUtil {

    private static PsiElementFactory elementFactory;

    public static PsiElementFactory getPsiElementFactory() {
        return elementFactory;
    }

    public static void setElementFactory(PsiElementFactory elementFactory) {
        TranspileUtil.elementFactory = elementFactory;
    }

    public static PsiClassType getTemplateType(PsiClass klass) {
        var elementFactory = TranspileUtil.getPsiElementFactory();
        var paramList = requireNonNull(klass.getTypeParameterList());
        List<PsiType> paramTypes = NncUtils.map(
                paramList.getTypeParameters(),
                elementFactory::createType
        );
        PsiType[] paramTypeArray = new PsiType[paramTypes.size()];
        paramTypes.toArray(paramTypeArray);
        return elementFactory.createType(klass, paramTypeArray);
    }

    public static PsiClassType getRawType(PsiClass klass) {
        return elementFactory.createType(klass);
    }

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

    public static String getBizFieldName(PsiField psiField) {
        String bizName = tryGetNameFromAnnotation(psiField, EntityField.class);
        return bizName != null ? bizName : psiField.getName();
    }

    public static String getBizClassName(PsiClass klass) {
        String bizName = tryGetNameFromAnnotation(klass, EntityType.class);
        return bizName != null ? bizName : klass.getName();
    }

    public static String getFlowName(PsiMethod method) {
        // TODO add flow annotation and get flow name from annotation value
        return method.getName();
    }

    private static String tryGetNameFromAnnotation(PsiJvmModifiersOwner element, Class<? extends Annotation> annotationClass) {
        var annotation = element.getAnnotation(annotationClass.getName());
        if(annotation == null) annotation = element.getAnnotation(annotationClass.getSimpleName());
        if(annotation != null) {
            var attr = NncUtils.find(annotation.getAttributes(), a -> a.getAttributeName().equals("value"));
            if(attr != null) {
                JvmAnnotationConstantValue value = (JvmAnnotationConstantValue) attr.getAttributeValue();
                return (String) requireNonNull(value).getConstantValue();
            }
        }
        return null;
    }

}
