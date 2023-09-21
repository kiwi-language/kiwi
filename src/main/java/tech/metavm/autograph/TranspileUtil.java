package tech.metavm.autograph;

import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class TranspileUtil {

    private static PsiElementFactory elementFactory;

    public static PsiElementFactory getPsiElementFactory() {
        return elementFactory;
    }

    public static boolean isVoidType(PsiType type) {
        if (type instanceof PsiPrimitiveType primitiveType) {
            //noinspection UnstableApiUsage
            return primitiveType.getKind() == JvmPrimitiveTypeKind.VOID;
        } else {
            return false;
        }
    }

    public static PsiClass eraseClass(PsiClass klass) {
        if(klass instanceof PsiTypeParameter typeParameter) {
            return typeParameter.getSuperClass();
        }
        else {
            return klass;
        }
    }

    public static boolean matchMethod(PsiMethod psiMethod, Method method) {
        var psiType = createType(requireNonNull(psiMethod.getContainingClass()));
        var type = createType(method.getDeclaringClass());
        if(!type.isAssignableFrom(psiType) || !psiMethod.getName().equals(method.getName())) {
            return false;
        }
        var psiParams = psiMethod.getParameterList().getParameters();
        var params = method.getParameters();
        if(psiParams.length != params.length) {
            return false;
        }
        for (int i = 0; i < psiParams.length; i++) {
            if(!matchType(psiParams[i].getType(), params[i].getType(), true)) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable PsiMethod getOverriddenMethod(PsiMethod method) {
        var declaringClass = NncUtils.requireNonNull(method.getContainingClass());
        Queue<PsiClass> queue = new LinkedList<>();
        queue.offer(declaringClass);
        Set<String> visited = new HashSet<>();
        visited.add(declaringClass.getQualifiedName());
        while (!queue.isEmpty()) {
            var klass = queue.poll();
            if(klass != declaringClass) {
                var superMethods = klass.getMethods();
                for (PsiMethod superMethod : superMethods) {
                    if(isOverrideOf(method, superMethod)) {
                        return superMethod;
                    }
                }
            }
            var superClass = klass.getSuperClass();
            if(superClass != null) {
                if(!visited.contains(superClass.getQualifiedName())) {
                    visited.add(superClass.getQualifiedName());
                    queue.offer(superClass);
                }
            }
            for (PsiClass it : klass.getInterfaces()) {
                if(!visited.contains(it.getQualifiedName())) {
                    visited.add(it.getQualifiedName());
                    queue.offer(it);
                }
            }
        }
        return null;
    }

    public static boolean isOverrideOf(PsiMethod override, PsiMethod overriden) {
        if(!override.getName().equals(overriden.getName())) {
            return false;
        }
        var overrideDeclClass = NncUtils.requireNonNull(override.getContainingClass());
        var overridenDeclClass = NncUtils.requireNonNull(overriden.getContainingClass());
        if(!overrideDeclClass.isInheritor(overridenDeclClass, true)) {
            return false;
        }
        int paramCount = override.getParameterList().getParametersCount();
        if(overriden.getParameterList().getParametersCount() != paramCount) {
            return false;
        }
        for (int i = 0; i < paramCount; i++) {
            var overrideParamType = NncUtils.requireNonNull(override.getParameterList().getParameter(i)).getType();
            var overridenParamType = NncUtils.requireNonNull(overriden.getParameterList().getParameter(i)).getType();
            if(!overrideParamType.equals(overridenParamType)) {
                return false;
            }
        }
        return true;
    }

    public static PsiClassType createType(Class<?> klass) {
        return elementFactory.createTypeByFQClassName(klass.getName());
    }

    public static PsiClassType getSuperType(PsiType type, Class<?> superClass) {
        var superTypes = type.getSuperTypes();
        if(matchType(type, superClass)) {
            return (PsiClassType) type;
        }
        return (PsiClassType) NncUtils.findRequired(superTypes, superType -> matchType(superType, superClass));
    }

    public static boolean matchType(PsiType type, Class<?> klass) {
        return matchType(type, klass, false);
    }

    public static boolean matchType(PsiType type, Class<?> klass, boolean erase) {
        if (type instanceof PsiClassType classType) {
            var resolved = NncUtils.requireNonNull(classType.resolve());
            if(erase) {
                resolved = eraseClass(resolved);
            }
            return matchClass(resolved, klass);
        }
        if(type instanceof PsiPrimitiveType primitiveType) {
            return klass.isPrimitive() && primitiveType.getName().equals(klass.getName());
        }
        return false;
    }

    public static boolean matchClass(PsiClass psiClass, Class<?> klass) {
        return Objects.equals(psiClass.getQualifiedName(), klass.getName());
    }

    public static void setElementFactory(PsiElementFactory elementFactory) {
        TranspileUtil.elementFactory = elementFactory;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static PsiExpression replaceForCondition(PsiForStatement statement, PsiExpression condition) {
        var currentCond = statement.getCondition();
        if (currentCond != null) {
            return (PsiExpression) currentCond.replace(TranspileUtil.and(currentCond, condition));
        } else {
            var semiColon = TranspileUtil.findFirstTokenRequired(statement, JavaTokenType.SEMICOLON);
            return (PsiExpression) statement.addAfter(condition, semiColon);
        }
    }

    public static PsiJavaToken findFirstTokenRequired(PsiElement element, IElementType tokenType) {
        return NncUtils.requireNonNull(
                findFirstToken(element, tokenType),
                "Can not find a child token with token type: " + tokenType
        );
    }

    public static @Nullable PsiJavaToken findFirstToken(PsiElement element, IElementType tokenType) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiJavaToken token && token.getTokenType() == tokenType) {
                return token;
            }
        }
        return null;
    }

    public static boolean containsDescendant(PsiElement ancestor, PsiElement descendant) {
        for (PsiElement child : ancestor.getChildren()) {
            if (child == descendant) {
                return true;
            }
            if (containsDescendant(child, descendant)) {
                return true;
            }
        }
        return false;
    }

    public static PsiStatement createStatementFromText(String text) {
        return elementFactory.createStatementFromText(text, null);
    }

    public static PsiExpression createExpressionFromText(String text) {
        return elementFactory.createExpressionFromText(text, null);
    }

    public static PsiElement createElementFromText(String text) {
        return elementFactory.createTypeElementFromText(text, null);
    }

    public static PsiExpression and(PsiExpression first, @Nullable PsiExpression second) {
        if (second == null) {
            return first;
        }
        String text = "(" + first.getText() + ") && (" + second.getText() + ")";
        return elementFactory.createExpressionFromText(text, null);
    }

    public static PsiClassType createType(PsiClass klass) {
        return elementFactory.createType(klass);
    }

    public static Flow getFlowByMethod(ClassType klass, PsiMethod psiMethod, TypeResolver typeResolver, IEntityContext context) {
        return klass.getFlow(
                psiMethod.getName(),
                NncUtils.map(
                        psiMethod.getParameterList().getParameters(),
                        param -> typeResolver.resolveTypeOnly(param.getType(), context)
                )
        );
    }

    public static PsiClassType createTemplateType(PsiClass klass) {
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
        if (statement.getResourceList() != null) {
            return statement.getResourceList().iterator().next();
        } else {
            var body = requireNonNull(statement.getTryBlock()).getStatements();
            return body.length > 0 ? body[0] : null;
        }
    }

    public static PsiElement getCatchSectionEntry(PsiCatchSection catchSection) {
        var body = requireNonNull(catchSection.getCatchBlock()).getStatements();
        return body.length > 0 ? body[0] : null;
    }

    public static @Nullable PsiElement getForStatementEntry(PsiForStatement statement) {
        if (statement.getCondition() != null) return statement.getCondition();
        if (statement.getBody() != null) {
            if (statement.getBody() instanceof PsiBlockStatement block) {
                if (block.getCodeBlock().getStatements().length > 0) {
                    return block.getCodeBlock().getStatements()[0];
                }
            } else return statement.getBody();
        }
        return statement.getUpdate();
    }

    public static String getBizFieldName(PsiField psiField) {
        String bizName = tryGetNameFromAnnotation(psiField, EntityField.class);
        if (bizName != null) {
            return bizName;
        }
        String childName = tryGetNameFromAnnotation(psiField, ChildEntity.class);
        if (childName != null) {
            return childName;
        }
        return psiField.getName();
    }

    public static String getFlowParamName(PsiParameter psiParameter) {
        String paramName = tryGetNameFromAnnotation(psiParameter, FlowParam.class);
        return paramName != null ? paramName : psiParameter.getName();
    }

    public static String getEnumConstantName(PsiEnumConstant enumConstant) {
        String bizName = tryGetNameFromAnnotation(enumConstant, EnumConstant.class);
        return bizName != null ? bizName : enumConstant.getName();
    }

    public static boolean isTitleField(PsiField psiField) {
        Boolean asTitle = (Boolean) getAnnotationAttr(psiField, EntityField.class, "asTitle");
        return asTitle == Boolean.TRUE;
    }

    public static boolean isUnique(PsiField psiField) {
        Boolean asTitle = (Boolean) getAnnotationAttr(psiField, EntityField.class, "unique");
        return asTitle == Boolean.TRUE;
    }

    public static boolean isChild(PsiField psiField) {
        return getAnnotation(psiField, ChildEntity.class) != null;
    }

    public static String getBizClassName(PsiClass klass) {
        String bizName = tryGetNameFromAnnotation(klass, EntityType.class);
        return bizName != null ? bizName : klass.getName();
    }

    public static String getFlowName(PsiMethod method) {
        String bizName = tryGetNameFromAnnotation(method, EntityFlow.class);
        return bizName != null ? bizName : method.getName();
    }

    private static String tryGetNameFromAnnotation(PsiJvmModifiersOwner element, Class<? extends Annotation> annotationClass) {
        return (String) getAnnotationAttr(element, annotationClass, "value");
    }

    private static PsiAnnotation getAnnotation(PsiJvmModifiersOwner element, Class<? extends Annotation> annotationClass) {
        var annotation = element.getAnnotation(annotationClass.getName());
        if (annotation == null) annotation = element.getAnnotation(annotationClass.getSimpleName());
        return annotation;
    }

    private static Object getAnnotationAttr(PsiJvmModifiersOwner element, Class<? extends Annotation> annotationClass, String attributeName) {
        var annotation = getAnnotation(element, annotationClass);
        if (annotation != null) {
            var attr = NncUtils.find(annotation.getAttributes(), a -> a.getAttributeName().equals(attributeName));
            if (attr != null) {
                JvmAnnotationConstantValue value = (JvmAnnotationConstantValue) attr.getAttributeValue();
                return requireNonNull(value).getConstantValue();
            }
        }
        return null;
    }

}
