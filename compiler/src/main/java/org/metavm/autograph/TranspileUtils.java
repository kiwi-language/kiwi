package org.metavm.autograph;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightRecordCanonicalConstructor;
import com.intellij.psi.impl.source.JavaDummyHolder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.*;
import org.metavm.api.builtin.IndexDef;
import org.metavm.entity.FlowParam;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.type.Type;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class TranspileUtils {

    private static final String DUMMY_FILE_NAME = "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension();

    private static PsiElementFactory elementFactory;
    private static Project project;

    public static PsiElementFactory getElementFactory() {
        return elementFactory;
    }

    public static PsiCodeBlock createCodeBlock() {
        return elementFactory.createCodeBlockFromText("{}", null);
    }

    public static String getCanonicalName(PsiTypeParameter typeParameter) {
        var owner = NncUtils.requireNonNull(typeParameter.getOwner());
        return getCanonicalName(owner) + "-" + typeParameter.getName();
    }

    public static boolean isIndexDefField(PsiField psiField) {
        return requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC)
                && TranspileUtils.getRawType(psiField.getType()).equals(TranspileUtils.createClassType(IndexDef.class));
    }

    public static PsiStatement getLastStatement(PsiCodeBlock codeBlock) {
        NncUtils.requireFalse(codeBlock.isEmpty(), "Code block is empty");
        return codeBlock.getStatements()[codeBlock.getStatementCount() - 1];
    }

    public static PsiType getLambdaReturnType(PsiLambdaExpression lambdaExpression) {
        var funcTypeGenerics = ((PsiClassType) requireNonNull(lambdaExpression.getFunctionalInterfaceType()))
                .resolveGenerics();
        var funcClass = funcTypeGenerics.getElement();
        var funcMethod = NncUtils.find(requireNonNull(funcClass).getAllMethods(),
                m -> m.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT));
        return funcTypeGenerics.getSubstitutor().substitute(requireNonNull(funcMethod).getReturnType());
    }

    private static String getCanonicalName(PsiTypeParameterListOwner typeParameterOwner) {
        return switch (typeParameterOwner) {
            case PsiClass psiClass -> getClassCanonicalName(psiClass);
            case PsiMethod method -> getMethodCanonicalName(method);
            default -> throw new IllegalStateException("Unexpected value: " + typeParameterOwner);
        };
    }

    public static PsiType getRawType(PsiType psiType) {
        return switch (psiType) {
            case PsiClassType psiClassType -> psiClassType.rawType();
            default -> psiType;
        };
    }

    public static MethodSignature getSignature(PsiMethod method, @Nullable PsiClassType qualifierType) {
        var declaringClass = qualifierType != null ? qualifierType :
                createType(requireNonNull(method.getContainingClass()));
        var paramClasses = NncUtils.map(method.getParameterList().getParameters(), PsiParameter::getType);
        var isStatic = method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
        return new MethodSignature(declaringClass, isStatic, method.getName(), paramClasses);
    }

    public static MethodSignature getMethodSignature(Method method) {
        return new MethodSignature(
                createClassType(method.getDeclaringClass()),
                Modifier.isStatic(method.getModifiers()),
                method.getName(),
                NncUtils.map(method.getParameters(), p -> createType(p.getParameterizedType(), p.isVarArgs()))
        );
    }

    public static PsiType createType(java.lang.reflect.Type javaType) {
        return createType(javaType, false);
    }

    public static PsiType createType(java.lang.reflect.Type javaType, boolean ellipsis) {
        return switch (javaType) {
            case Class<?> klass -> {
                if(klass.isPrimitive())
                    yield createPrimitiveType(klass);
                else if(klass.isArray())
                    yield ellipsis ? createEllipsisType(klass) : createArrayType(klass);
                else
                    yield createClassType(klass);
            }
            case ParameterizedType parameterizedType -> createParameterizedType(parameterizedType);
            case WildcardType wildcardType  -> createWildcardType(wildcardType);
            case TypeVariable<?> typeVariable -> createVariableType(typeVariable);
            case GenericArrayType genericArrayType -> ellipsis ? createEllipsisType(genericArrayType) : createArrayType(genericArrayType);
            default -> throw new IllegalStateException("Unexpected type: " + javaType);
        };
    }

    private static Class<?> getJavaClass(PsiType psiType) {
        return switch (psiType) {
            case PsiClassType psiClassType ->
                    ReflectionUtils.classForName(requireNonNull(psiClassType.resolve()).getQualifiedName());
            case PsiArrayType psiArrayType -> getJavaClass(psiArrayType.getComponentType()).arrayType();
            case PsiPrimitiveType primitiveType ->
                    ReflectionUtils.getPrimitiveClass(primitiveType.getKind().getBinaryName());
            default -> throw new IllegalStateException("Unexpected value: " + psiType);
        };
    }

    public static PsiWildcardType createWildcardType(WildcardType wildcardType) {
        var psiManager = PsiManager.getInstance(project);
        if((wildcardType.getUpperBounds().length == 0 ||
                wildcardType.getUpperBounds().length == 1 && wildcardType.getUpperBounds()[0].equals(Object.class)
                && wildcardType.getLowerBounds().length == 0
        )) {
            return PsiWildcardType.createUnbounded(psiManager);
        }
        if(wildcardType.getLowerBounds().length > 0) {
            return PsiWildcardType.createSuper(psiManager, createType(wildcardType.getLowerBounds()[0]));
        }
        else
            return PsiWildcardType.createExtends(psiManager, createType(wildcardType.getUpperBounds()[0]));
    }

    public static PsiWildcardType createExtendsWildcardType(PsiType bound) {
        var psiManager = PsiManager.getInstance(project);
        return PsiWildcardType.createExtends(psiManager, bound);
    }

    public static PsiWildcardType createSuperWildcardType(PsiType bound) {
        var psiManager = PsiManager.getInstance(project);
        return PsiWildcardType.createSuper(psiManager, bound);
    }

    public static String getCanonicalName(PsiType type) {
        return switch (type) {
            case PsiClassType classType -> {
                var klass = NncUtils.requireNonNull(classType.resolve());
                yield Types.parameterizedName(
                        getClassCanonicalName(klass),
                        NncUtils.map(
                                classType.getParameters(),
                                TranspileUtils::getCanonicalName
                        )
                );
            }
            case PsiArrayType arrayType -> getCanonicalName(arrayType.getComponentType()) + "[]";
            case PsiPrimitiveType primitiveType -> primitiveType.getBoxedTypeName();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static <T> @Nullable T getNextElement(PsiElement element, Class<T> klass) {
        PsiElement next = element.getNextSibling();
        while (next != null && !klass.isInstance(next)) {
            next = next.getNextSibling();
        }
        return klass.cast(next);
    }

    public static @Nullable PsiStatement getNextStatement(PsiElement element) {
        return getNextElement(element, PsiStatement.class);
    }

    private static String getClassCanonicalName(PsiClass psiClass) {
        if (psiClass instanceof PsiTypeParameter typeParameter) {
            return getCanonicalName(typeParameter.getExtendsListTypes()[0]);
        } else {
            return psiClass.getQualifiedName();
        }
    }

    private static String getMethodCanonicalName(PsiMethod method) {
        return getClassCanonicalName(NncUtils.requireNonNull(method.getContainingClass())) + "."
                + method.getName() + "("
                + NncUtils.join(NncUtils.requireNonNull(method.getParameterList()).getParameters(),
                param -> getCanonicalName(param.getType()))
                + ")";
    }

    public static boolean isObjectClass(PsiClass psiClass) {
        return Object.class.getName().equals(psiClass.getQualifiedName());
    }

    public static @Nullable PsiClassType getSuperClassType(PsiClass psiClass) {
        var superTypes = psiClass.getSuperTypes();
        if (superTypes.length > 0) {
            return superTypes[0];
        } else {
            return null;
        }
    }

    public static List<PsiClassType> getInterfaceTypes(PsiClass psiClass) {
        var superTypes = psiClass.getSuperTypes();
        if (superTypes.length > 1) {
            return List.of(Arrays.copyOfRange(superTypes, 1, superTypes.length));
        } else {
            return List.of();
        }
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
        if (klass instanceof PsiTypeParameter typeParameter) {
            return typeParameter.getSuperClass();
        } else {
            return klass;
        }
    }

    public static boolean matchMethod(PsiMethod psiMethod, Method method) {
        var psiType = createType(requireNonNull(psiMethod.getContainingClass()));
        var type = createClassType(method.getDeclaringClass());
        if (!type.isAssignableFrom(psiType) || !psiMethod.getName().equals(method.getName())) {
            return false;
        }
        var psiParams = psiMethod.getParameterList().getParameters();
        var params = method.getParameters();
        if (psiParams.length != params.length) {
            return false;
        }
        for (int i = 0; i < psiParams.length; i++) {
            if (!matchType(psiParams[i].getType(), params[i].getType(), true)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    public static List<PsiMethod> getOverriddenMethods(PsiMethod method) {
        var declaringClass = NncUtils.requireNonNull(method.getContainingClass());
        Queue<PsiClass> queue = new LinkedList<>();
        queue.offer(declaringClass);
        Set<String> visited = new HashSet<>();
        visited.add(declaringClass.getQualifiedName());
        List<PsiMethod> overriddenMethods = new ArrayList<>();
        while (!queue.isEmpty()) {
            var klass = queue.poll();
            if (klass != declaringClass) {
                var superMethods = klass.getMethods();
                for (PsiMethod superMethod : superMethods) {
                    if (isOverrideOf(method, superMethod)) {
                        overriddenMethods.add(superMethod);
                    }
                }
            }
            var superClass = klass.getSuperClass();
            if (superClass != null) {
                if (!visited.contains(superClass.getQualifiedName())) {
                    visited.add(superClass.getQualifiedName());
                    queue.offer(superClass);
                }
            }
            for (PsiClass it : klass.getInterfaces()) {
                if (!visited.contains(it.getQualifiedName())) {
                    visited.add(it.getQualifiedName());
                    queue.offer(it);
                }
            }
        }
        return overriddenMethods;
    }

    public static boolean isOverrideOf(PsiMethod method, PsiMethod overridden) {
        if (!method.getName().equals(overridden.getName())) {
            return false;
        }
        var overrideDeclClass = NncUtils.requireNonNull(method.getContainingClass());
        var overriddenDeclClass = NncUtils.requireNonNull(overridden.getContainingClass());
        if (!overrideDeclClass.isInheritor(overriddenDeclClass, true)) {
            return false;
        }
        int paramCount = method.getParameterList().getParametersCount();
        if (overridden.getParameterList().getParametersCount() != paramCount
                || method.getTypeParameters().length != overridden.getTypeParameters().length) {
            return false;
        }
        var subst = PsiSubstitutor.createSubstitutor(
                NncUtils.zip(List.of(overridden.getTypeParameters()),
                        NncUtils.map(List.of(method.getTypeParameters()), TranspileUtils::createType))
        );
        for (int i = 0; i < paramCount; i++) {
            var paramType = NncUtils.requireNonNull(method.getParameterList().getParameter(i)).getType();
            var overriddenParamType = subst.substitute(NncUtils.requireNonNull(overridden.getParameterList().getParameter(i)).getType());
            if (!paramType.equals(overriddenParamType)) {
                return false;
            }
        }
        return true;
    }

    private static final List<Class<?>> primitiveClasses = List.of(
            int.class, short.class, byte.class, long.class, float.class, double.class,
            boolean.class, char.class
    );

    public static List<PsiPrimitiveType> getPrimitiveTypes() {
        return NncUtils.map(primitiveClasses, TranspileUtils::createPrimitiveType);
    }

    public static PsiPrimitiveType createPrimitiveType(Class<?> klass) {
        NncUtils.requireTrue(klass.isPrimitive());
        return elementFactory.createPrimitiveType(klass.getName());
    }

    public static void forEachCapturedTypePairs(PsiType psiType, Type type, BiConsumer<PsiCapturedWildcardType, CapturedType> action) {
        switch (type) {
            case CapturedType capturedType -> {
                if (psiType instanceof PsiCapturedWildcardType psiCapturedWildcardType)
                    action.accept(psiCapturedWildcardType, capturedType);
            }
            case UncertainType uncertainType -> {
                var psiWildcardType = (PsiWildcardType) psiType;
                if (psiWildcardType.isSuper())
                    forEachCapturedTypePairs(psiWildcardType.getSuperBound(), uncertainType.getLowerBound(), action);
                else
                    forEachCapturedTypePairs(psiWildcardType.getExtendsBound(), uncertainType.getUnderlyingType(), action);
            }
            case ClassType classType -> {
                var psiClassType = (PsiClassType) psiType;
                for (int i = 0; i < classType.getTypeArguments().size(); i++) {
                    forEachCapturedTypePairs(psiClassType.getParameters()[i], classType.getTypeArguments().get(i), action);
                }
            }
            default -> {
            }
        }
    }

    public static PsiClassType createClassType(Class<?> klass) {
        return elementFactory.createTypeByFQClassName(klass.getName());
    }

    public static PsiClassType createParameterizedType(ParameterizedType parameterizedType) {
        return elementFactory.createType(
                Objects.requireNonNull(createClassType(((Class<?>) parameterizedType.getRawType())).resolve()),
                NncUtils.mapArray(parameterizedType.getActualTypeArguments(), TranspileUtils::createType, PsiType[]::new)
        );
    }

    public static PsiArrayType createArrayType(Class<?> klass) {
        NncUtils.requireTrue(klass.isArray());
        return new PsiArrayType(createType(klass.getComponentType()));
    }

    public static PsiArrayType createArrayType(GenericArrayType genericArrayType) {
        return new PsiArrayType(createType(genericArrayType.getGenericComponentType()));
    }

    public static PsiEllipsisType createEllipsisType(GenericArrayType genericArrayType) {
        return new PsiEllipsisType(createType(genericArrayType.getGenericComponentType()));
    }

    public static PsiArrayType createEllipsisType(Class<?> klass) {
        NncUtils.requireTrue(klass.isArray());
        return new PsiEllipsisType(createType(klass.getComponentType()));
    }

    public static PsiType createType(Class<?> klass) {
        if (klass.isPrimitive())
            return createPrimitiveType(klass);
        else if (klass.isArray())
            return createArrayType(klass);
        else
            return createClassType(klass);
    }

    public static PsiClassType createType(Class<?> rawClass, List<PsiType> typeArguments) {
        PsiType[] typeArgs = new PsiType[typeArguments.size()];
        typeArguments.toArray(typeArgs);
        return createType(rawClass, typeArgs);
    }

    public static PsiClassType createType(Class<?> rawClass, PsiType... typeArguments) {
        return elementFactory.createType(
                requireNonNull(createClassType(rawClass).resolve()),
                typeArguments
        );
    }

    public static PsiClassType createVariableType(java.lang.reflect.TypeVariable<?> typeVariable) {
        var genDecl = typeVariable.getGenericDeclaration();
        var index = List.of(genDecl.getTypeParameters()).indexOf(typeVariable);
        if(genDecl instanceof Class<?> klass)
            return createVariableType(klass, index);
        else
            return createVariableType((Method) genDecl, index);
    }

    public static PsiClassType createVariableType(Class<?> rawClass, int typeParameterIndex) {
        var psiClass = Objects.requireNonNull(createClassType(rawClass).resolve());
        return createType(Objects.requireNonNull(psiClass.getTypeParameters())[typeParameterIndex]);
    }

    public static PsiClassType createVariableType(Method method, int typeParameterIndex) {
        var psiClass = requireNonNull(createClassType(method.getDeclaringClass()).resolve());
        var psiMethod = NncUtils.find(psiClass.getMethods(), m -> matchMethod(m, method));
        return createType(Objects.requireNonNull(psiMethod).getTypeParameters()[typeParameterIndex]);
    }

    public static PsiClassType getSuperType(PsiType type, Class<?> superClass) {
        Queue<PsiType> queue = new LinkedList<>(List.of(type));
        while (!queue.isEmpty()) {
            var t = queue.poll();
            if (matchType(t, superClass))
                return (PsiClassType) t;
            for (PsiType s : t.getSuperTypes())
                queue.offer(s);
        }
        throw new InternalException("Can not find super type '" + superClass.getName()
                + "' in the hierarchy of '" + type.getCanonicalText() + "'");
    }

    public static boolean isPublic(PsiClass psiClass) {
        return requireNonNull(psiClass.getModifierList()).hasModifierProperty(PsiModifier.PUBLIC);
    }

    public static PsiMethod createGetter(String name, PsiType type) {
        String text = String.format(
                "public %s %s() { return this.%s; }",
                type.getCanonicalText(), name, name
        );
        return elementFactory.createMethodFromText(text, null);
    }

    public static PsiMethod createMethodFromText(String text) {
        return elementFactory.createMethodFromText(text, null);
    }

    public static boolean isPrivate(PsiClass psiClass) {
        return requireNonNull(psiClass.getModifierList()).hasModifierProperty(PsiModifier.PRIVATE);
    }

    public static boolean isProtected(PsiClass psiClass) {
        return requireNonNull(psiClass.getModifierList()).hasModifierProperty(PsiModifier.PROTECTED);
    }

    private static class UpwardsClassVisitor extends JavaElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            if (aClass.getSuperClass() != null)
                aClass.getSuperClass().accept(this);
            for (PsiClass it : aClass.getInterfaces())
                it.accept(this);
            super.visitClass(aClass);
        }

    }

    public static PsiClassType getSuperType(PsiClass psiClass, PsiClass superClass) {
        if (Objects.equals(psiClass.getQualifiedName(), superClass.getName())) {
            return createType(psiClass);
        }
        var superTypes = psiClass.getSuperTypes();
        return NncUtils.findRequired(superTypes, superType -> Objects.equals(superType.resolve(), superClass));
    }

    public static boolean matchType(PsiType type, Class<?> klass) {
        return matchType(type, klass, false);
    }

    public static boolean matchType(PsiType type, Class<?> klass, boolean erase) {
        if (type instanceof PsiClassType classType) {
            var resolved = NncUtils.requireNonNull(classType.resolve());
            if (erase) {
                resolved = eraseClass(resolved);
            }
            return matchClass(resolved, klass);
        }
        if (type instanceof PsiPrimitiveType primitiveType) {
            return klass.isPrimitive() && primitiveType.getName().equals(klass.getName());
        }
        return false;
    }

    public static boolean matchClass(PsiClass psiClass, Class<?> klass) {
        return Objects.equals(psiClass.getQualifiedName(), klass.getName());
    }

    public static void init(PsiElementFactory elementFactory, Project project) {
        TranspileUtils.elementFactory = elementFactory;
        TranspileUtils.project = project;
        nativeFunctionCallResolvers.clear();
        for (StdFunction def : StdFunction.values()) {
            for (Method javaMethod : def.getJavaMethods()) {
                nativeFunctionCallResolvers.add(new NativeFunctionCallResolver(getMethodSignature(javaMethod), def.get()));
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static PsiExpression replaceForCondition(PsiForStatement statement, PsiExpression condition) {
        var currentCond = statement.getCondition();
        if (currentCond != null) {
            return (PsiExpression) currentCond.replace(TranspileUtils.and(currentCond, condition));
        } else {
            var semiColon = TranspileUtils.findFirstTokenRequired(statement, JavaTokenType.SEMICOLON);
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

    public static PsiClass createClassFromText(String text) {
        var file = (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText(DUMMY_FILE_NAME, JavaFileType.INSTANCE, text);
        return file.getClasses()[0];
    }

    public static PsiClass createClass(String name, boolean isPublic) {
        if (isPublic)
            return elementFactory.createClassFromText(String.format("public class %s {}", name), null);
        else
            return elementFactory.createClassFromText(String.format("class %s {}", name), null);
    }

    public static PsiField createFieldFromText(String text) {
        return elementFactory.createFieldFromText(text, null);
    }

    public static PsiField createField(String name, PsiType type, boolean isFinal, @Nullable String access) {
        if (isFinal) {
            if (access != null)
                return elementFactory.createFieldFromText(String.format("%s final %s %s;", access, type.getCanonicalText(), name), null);
            else
                return elementFactory.createFieldFromText(String.format("final %s %s;", type.getCanonicalText(), name), null);
        } else {
            if (access != null)
                return elementFactory.createFieldFromText(String.format("%s %s %s;", access, type.getCanonicalText(), name), null);
            else
                return elementFactory.createFieldFromText(String.format("%s %s;", type.getCanonicalText(), name), null);
        }
    }

    public static PsiMethod createConstructor(String name, boolean isPublic) {
        if (isPublic)
            return elementFactory.createMethodFromText(String.format("public %s() {}", name), null);
        else
            return elementFactory.createMethodFromText(String.format("%s() {}", name), null);
    }

    public static PsiStatement getEnclosingStatement(PsiExpression expression) {
        PsiElement element = expression;
        while (element != null && !(element instanceof PsiStatement)) {
            element = element.getParent();
        }
        return NncUtils.requireNonNull((PsiStatement) element,
                "Can not find a enclosing statement for expression: " + expression);
    }

    public static @Nullable List<PsiElement> getAncestorPath(PsiElement element, Class<?>... ancestorClasses) {
        return getAncestorPath(element, Set.of(ancestorClasses));
    }

    public static <T> List<T> getEnclosingElements(PsiElement element, Class<T> klass, Set<Class<?>> terminalClasses) {
        var current = element.getParent();
        List<T> result = new ArrayList<>();
        while (current != null && !ReflectionUtils.isInstance(terminalClasses, current)) {
            if (klass.isInstance(current)) {
                result.add(klass.cast(current));
            }
            current = current.getParent();
        }
        return result;
    }

    public static @Nullable List<PsiElement> getAncestorPath(PsiElement element, Set<Class<?>> ancestorClasses) {
        List<PsiElement> path = new ArrayList<>();
        var current = element.getParent();
        if (current != null && !ReflectionUtils.isInstance(ancestorClasses, current)) {
            path.add(current);
        }
        if (current == null) {
            return null;
        } else {
            path.add(current);
            Collections.reverse(path);
            return path;
        }
    }


    public static @Nullable PsiElement getAncestor(PsiElement element, Class<?>... parentClasses) {
        return getParent(element, Set.of(parentClasses));
    }

    public static @Nullable PsiStatement getPrevStatement(PsiStatement statement) {
        var current = statement.getPrevSibling();
        while (current != null && !(current instanceof PsiStatement)) {
            current = current.getPrevSibling();
        }
        return (PsiStatement) current;
    }

    public static boolean isUnderDummyHolder(PsiElement element) {
        return getAncestor(element, JavaDummyHolder.class) != null;
    }

    public static @Nullable <T extends PsiElement> T getParent(PsiElement element, Class<T> parentClass) {
        return parentClass.cast(getParent(element, Set.of(parentClass)));
    }

    public static @Nullable PsiElement getParent(PsiElement element, Set<Class<?>> parentClasses) {
        PsiElement current = element;
        while (current != null && !ReflectionUtils.isInstance(parentClasses, current)) {
            current = current.getParent();
        }
        return current;
    }

    public static PsiElement getParentRequired(PsiElement element, Set<Class<?>> parentClasses) {
        return requireNonNull(getParent(element, parentClasses));
    }

    public static Scope getBodyScope(PsiElement element) {
        return requireNonNull(element.getUserData(Keys.BODY_SCOPE));
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

    public static org.metavm.flow.Method getMethidByJavaMethod(Klass klass, PsiMethod psiMethod, TypeResolver typeResolver) {
        return klass.getMethodByCodeAndParamTypes(
                psiMethod.getName(),
                NncUtils.map(
                        psiMethod.getParameterList().getParameters(),
                        param -> resolveParameterType(param, typeResolver)
                )
        );
    }

    public static Type resolveParameterType(PsiParameter parameter, TypeResolver typeResolver) {
        var type = typeResolver.resolveTypeOnly(parameter.getType());
        if(isAnnotatedWithNullable(parameter))
            type = Types.getNullableType(type);
        return type;
    }

    public static boolean isAnnotatedWithNullable(PsiModifierListOwner element) {
        return hasAnnotation(element, Nullable.class) || hasAnnotation(element, org.jetbrains.annotations.Nullable.class);
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

    public static String getBizFieldName(PsiVariable psiField) {
        String bizName = tryGetNameFromAnnotation(psiField, EntityField.class);
        if (bizName != null)
            return bizName;
        String childName = tryGetNameFromAnnotation(psiField, ChildEntity.class);
        if (childName != null)
            return childName;
        return psiField.getName();
    }

    public static boolean isAncestor(PsiElement element, PsiElement ancestor) {
        var current = element;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    public static String getFlowParamName(PsiParameter psiParameter) {
        String paramName = tryGetNameFromAnnotation(psiParameter, FlowParam.class);
        return paramName != null ? paramName : psiParameter.getName();
    }

    public static String getEnumConstantName(PsiEnumConstant enumConstant) {
        String bizName = tryGetNameFromAnnotation(enumConstant, EnumConstant.class);
        return bizName != null ? bizName : enumConstant.getName();
    }

    public static boolean isTitleField(PsiVariable psiField) {
        Boolean asTitle = (Boolean) getAnnotationAttribute(psiField, EntityField.class, "asTitle");
        return asTitle == Boolean.TRUE;
    }

    public static boolean isUnique(PsiVariable psiField) {
        Boolean asTitle = (Boolean) getAnnotationAttribute(psiField, EntityField.class, "unique");
        return asTitle == Boolean.TRUE;
    }

    public static boolean isEphemeral(PsiClass psiClass) {
        Boolean ephemeral = (Boolean) getEntityAnnotationAttr(psiClass, "ephemeral");
        return ephemeral == Boolean.TRUE;
    }

    private static @Nullable Object getEntityAnnotationAttr(PsiClass psiClass, String attributeName) {
        var value = getAnnotationAttribute(psiClass, EntityType.class, attributeName);
        if (value != null)
            return value;
        if((value = getAnnotationAttribute(psiClass, ValueType.class, attributeName)) != null)
            return value;
        if((value = getAnnotationAttribute(psiClass, EntityStruct.class, attributeName)) != null)
            return value;
        return getAnnotationAttribute(psiClass, ValueStruct.class, attributeName);
    }

    public static Access getAccess(PsiVariable psiField) {
        if (psiField instanceof PsiRecordComponent)
            return Access.PUBLIC;
        var modifiers = Objects.requireNonNull(psiField.getModifierList());
        if (modifiers.hasModifierProperty(PsiModifier.PUBLIC))
            return Access.PUBLIC;
        if (modifiers.hasModifierProperty(PsiModifier.PROTECTED))
            return Access.PROTECTED;
        if (modifiers.hasModifierProperty(PsiModifier.PRIVATE))
            return Access.PRIVATE;
        return Access.PACKAGE;
    }

    public static boolean isChild(PsiVariable psiField) {
        return getAnnotation(psiField, ChildEntity.class) != null;
    }

    public static String getBizClassName(PsiClass klass) {
        String bizName = (String) getEntityAnnotationAttr(klass, "value");
        return NncUtils.isNotBlank(bizName) ? bizName : klass.getName();
    }

    public static boolean isStruct(PsiClass psiClass) {
        return psiClass.isRecord()
                && NncUtils.count(psiClass.getConstructors(), m -> !(m instanceof LightRecordCanonicalConstructor)) == 0
                || psiClass.hasAnnotation(EntityStruct.class.getName()) || psiClass.hasAnnotation(ValueStruct.class.getName());
    }

    public static boolean isStatic(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.STATIC);
    }

    public static String getQualifiedName(PsiMethod psiMethod) {
        return requireNonNull(psiMethod.getContainingClass()).getName() + "." + psiMethod.getName();
    }

    public static String getFlowName(PsiMethod method) {
        String bizName = tryGetNameFromAnnotation(method, EntityFlow.class);
        return bizName != null ? bizName : getFlowCode(method);
    }

    public static String getIndexName(PsiClass klass) {
        String bizName = tryGetNameFromAnnotation(klass, EntityIndex.class);
        return bizName != null ? bizName : klass.getName();
    }

    public static boolean isUniqueIndex(PsiClass klass) {
        var unique = getAnnotationAttribute(klass, EntityIndex.class, "unique");
        return unique != null ? (Boolean) unique : false;
    }

    public static String getFlowCode(PsiMethod method) {
        if (method.isConstructor()) {
            return requireNonNull(method.getContainingClass()).getName();
        } else {
            return method.getName();
        }
    }

    private static String tryGetNameFromAnnotation(PsiModifierListOwner element, Class<? extends Annotation> annotationClass) {
        var value =  (String) getAnnotationAttribute(element, annotationClass, "value");
        return NncUtils.isNotBlank(value) ? value : null;
    }


    public static PsiAnnotation getAnnotation(PsiAnnotationOwner element, Class<? extends Annotation> annotationClass) {
        var annotation = findAnnotation(element.getAnnotations(), annotationClass.getName());
        if (annotation == null) annotation = findAnnotation(element.getAnnotations(), annotationClass.getSimpleName());
        return annotation;
    }

    public static PsiAnnotation getAnnotation(PsiModifierListOwner element, Class<? extends Annotation> annotationClass) {
        var annotation = findAnnotation(element.getAnnotations(), annotationClass.getName());
        if (annotation == null) annotation = findAnnotation(element.getAnnotations(), annotationClass.getSimpleName());
        return annotation;
    }

    public static boolean hasAnnotation(PsiModifierListOwner element, Class<? extends Annotation> annotationClass) {
        return getAnnotation(element, annotationClass) != null;
    }

    public static @Nullable PsiMethod findCanonicalConstructor(PsiClass psiClass) {
        var fieldTypes = NncUtils.map(getAllInstanceFields(psiClass), PsiField::getType);
        return NncUtils.find(psiClass.getMethods(),
                m -> m.isConstructor() && NncUtils.map(m.getParameterList().getParameters(), PsiParameter::getType).equals(fieldTypes));
    }

    public static List<PsiField> getAllInstanceFields(PsiClass psiClass) {
        List<PsiField> fields = new ArrayList<>();
        LinkedList<PsiClass> classes = new LinkedList<>();
        var c = psiClass;
        while (c != null) {
            classes.push(c);
            c = c.getSuperClass();
        }
        for (PsiClass k : classes) {
            for (PsiField field : k.getFields()) {
                if (!requireNonNull(field.getModifierList()).hasModifierProperty(PsiModifier.STATIC))
                    fields.add(field);
            }
        }
        return fields;
    }

    public static boolean hasAnnotation(PsiAnnotationOwner element, Class<? extends Annotation> annotationClass) {
        return getAnnotation(element, annotationClass) != null;
    }

    private static @Nullable PsiAnnotation findAnnotation(PsiAnnotation[] annotations, String qualifiedName) {
        if (annotations.length == 0) return null;

        String shortName = StringUtil.getShortName(qualifiedName);
        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (referenceElement != null && shortName.equals(referenceElement.getReferenceName())) {
                if (qualifiedName.equals(annotation.getQualifiedName())) {
                    return annotation;
                }
            }
        }

        return null;
    }

    public static Object getAnnotationAttribute(PsiModifierListOwner element, Class<? extends Annotation> annotationClass, String attributeName) {
        var annotation = getAnnotation(element, annotationClass);
        if (annotation != null)
            return getAnnotationAttribute(annotation, attributeName, null);
        return null;
    }

    public static Object getAnnotationAttribute(PsiAnnotation annotation, String attributeName, @Nullable Object defaultValue) {
        var attr = NncUtils.find(annotation.getAttributes(), a -> a.getAttributeName().equals(attributeName));
        if (attr != null) {
            JvmAnnotationConstantValue value = (JvmAnnotationConstantValue) attr.getAttributeValue();
            var constValue = requireNonNull(value).getConstantValue();
            if(!isNullOrBlank(constValue))
                return constValue;
        }
        return defaultValue;
    }

    private static boolean isNullOrBlank(Object value) {
        if(value == null)
            return true;
        if(value instanceof String s)
            return s.isEmpty();
        return false;
    }

    private static final Map<String, String> typeNameMap = Map.ofEntries(
            Map.entry(String.class.getName(), "String"),
            Map.entry(Long.class.getName(), "Long"),
            Map.entry(Integer.class.getName(), "Long"),
            Map.entry(Short.class.getName(), "Long"),
            Map.entry(Double.class.getName(), "Double"),
            Map.entry(Float.class.getName(), "Double"),
            Map.entry(Boolean.class.getName(), "Boolean"),
            Map.entry(Date.class.getName(), "Time"),
            Map.entry(Set.class.getName(), "Set"),
            Map.entry(Map.class.getName(), "Map"),
            Map.entry(Collection.class.getName(), "Collection"),
            Map.entry(List.class.getName(), "List"),
            Map.entry(ChildList.class.getName(), "ChildList"),
            Map.entry(ArrayList.class.getName(), "ReadWriteList"),
            Map.entry(LinkedList.class.getName(), "ReadWriteList"),
            Map.entry(Object.class.getName(), "Any"),
            Map.entry(Iterable.class.getName(), "Iterable"),
            Map.entry(Consumer.class.getName(), "Consumer"),
            Map.entry(Predicate.class.getName(), "Predicate")
    );

    public static String getInternalName(PsiMethod method) {
        return getInternalName(method, List.of());
    }

    public static String getInternalName(PsiMethod method, List<PsiType> implicitParameterTypes) {
        var paramTypeNames = new ArrayList<>(
                NncUtils.map(implicitParameterTypes, t -> getInternalName(t, false, method))
        );
        paramTypeNames.addAll(
                NncUtils.map(method.getParameterList().getParameters(),
                        p -> getInternalName(p.getType(), isAnnotatedWithNullable(p), method))
        );
        return getInternalName(createType(method.getContainingClass()), null) + "." +
                method.getName() + "(" + NncUtils.join(paramTypeNames, ",") + ")";
    }


    private static String getInternalName(PsiTypeParameterListOwner typeParameterOwner, PsiMethod current) {
        return switch (typeParameterOwner) {
            case PsiClass psiClass -> getInternalName(createType(psiClass), current);
            case PsiMethod method -> getInternalName(method);
            default -> throw new IllegalStateException("Unexpected value: " + typeParameterOwner);
        };
    }

    private static String getInternalName(PsiType type, boolean nullable, PsiMethod current) {
        if (nullable) {
            var names = List.of("Null", getInternalName(type, current));
            return names.stream().sorted().collect(Collectors.joining("|"));
        } else {
            return getInternalName(type, current);
        }
    }

    private static String getInternalName(PsiType type, PsiMethod current) {
        if (type instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            if (psiClass instanceof PsiTypeParameter typeParameter) {
                if (typeParameter.getOwner() == current)
                    return "this." + typeParameter.getName();
                else
                    return getInternalName(requireNonNull(typeParameter.getOwner()), current) + "." + typeParameter.getName();
            }
            if (classType.getParameters().length > 0) {
                var typeArgs = NncUtils.map(classType.getParameters(), t -> getInternalName(t, current));
                return Types.parameterizedName(
                        typeNameMap.getOrDefault(psiClass.getQualifiedName(), psiClass.getQualifiedName()),
                        typeArgs);
            } else
                return typeNameMap.getOrDefault(psiClass.getQualifiedName(), psiClass.getQualifiedName());
        }
        if (type instanceof PsiPrimitiveType primitiveType) {
            return typeNameMap.get(primitiveType.getBoxedTypeName());
        }
        if (type instanceof PsiWildcardType wildcardType) {
            if (wildcardType.isSuper())
                return "[" + getInternalName(wildcardType.getBound(), current) + ",Any|Null]";
            else
                return "[Never," + getInternalName(wildcardType.getBound(), current) + "]";
        }
        return type.getCanonicalText();
    }

    public static boolean isColonSwitch(PsiSwitchBlock statement) {
        var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
        return stmts.length > 0 && stmts[0] instanceof PsiSwitchLabelStatement;
    }

    public static List<PsiEnumConstant> getEnumConstants(PsiClass psiClass) {
        return NncUtils.filterByType(List.of(psiClass.getFields()), PsiEnumConstant.class);
    }

    public static PsiClass resolvePsiClass(PsiClassType classType) {
        return Objects.requireNonNull(classType.resolve(), () -> "Failed to resolve class " + classType.getCanonicalText());
    }

    private final static List<NativeFunctionCallResolver> nativeFunctionCallResolvers = new ArrayList<>();

    public static List<NativeFunctionCallResolver> getNativeFunctionCallResolvers() {
        return Collections.unmodifiableList(nativeFunctionCallResolvers);
    }
}
