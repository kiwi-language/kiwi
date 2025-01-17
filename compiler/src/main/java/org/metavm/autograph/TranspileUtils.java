package org.metavm.autograph;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightRecordCanonicalConstructor;
import com.intellij.psi.impl.source.JavaDummyHolder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Enum;
import org.metavm.api.Index;
import org.metavm.api.*;
import org.metavm.entity.natives.StandardStaticMethods;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.type.Type;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class TranspileUtils {

    public static final Logger logger = LoggerFactory.getLogger(TranspileUtils.class);
    public static PsiPrimitiveType intType;

    private static final String DUMMY_FILE_NAME = "_Dummy_." + JavaFileType.INSTANCE.getDefaultExtension();

    public static PsiElementFactory elementFactory;
    private static Project project;

    public static PsiElementFactory getElementFactory() {
        return elementFactory;
    }

    public static PsiCodeBlock createCodeBlock() {
        return elementFactory.createCodeBlockFromText("{}", null);
    }

    public static String getCanonicalName(PsiTypeParameter typeParameter) {
        var owner = Objects.requireNonNull(typeParameter.getOwner());
        return getCanonicalName(owner) + "-" + typeParameter.getName();
    }

    public static PsiStatement getLastStatement(PsiCodeBlock codeBlock) {
        Utils.require(!codeBlock.isEmpty(), "Code block is empty");
        return codeBlock.getStatements()[codeBlock.getStatementCount() - 1];
    }

    public static PsiType getLambdaReturnType(PsiLambdaExpression lambdaExpression) {
        var funcTypeGenerics = ((PsiClassType) requireNonNull(lambdaExpression.getFunctionalInterfaceType(),
                () -> "Cannot find functional interface for lambda expression: " + lambdaExpression.getText()))
                .resolveGenerics();
        var funcClass = funcTypeGenerics.getElement();
        var funcMethod = Utils.find(requireNonNull(funcClass).getAllMethods(),
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

    public static MethodSignature getSignature(PsiMethod method) {
        return getSignature(method, null);
    }

    public static MethodSignature getSignature(PsiMethod method, @Nullable PsiClassType qualifierType) {
        var declaringClass = qualifierType != null ? qualifierType :
                createType(requireNonNull(method.getContainingClass()));
        var paramClasses = Utils.map(method.getParameterList().getParameters(), PsiParameter::getType);
        var isStatic = method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
        return new MethodSignature(declaringClass, isStatic, method.getName(), paramClasses);
    }

    public static MethodSignature getMethodSignature(Method method) {
        return new MethodSignature(
                createClassType(method.getDeclaringClass()),
                Modifier.isStatic(method.getModifiers()),
                method.getName(),
                Utils.map(method.getParameters(), p -> createType(p.getParameterizedType(), p.isVarArgs()))
        );
    }

    public static PsiMethod getMethodByName(PsiClass klass, String name) {
        return Utils.findRequired(klass.getMethods(), m -> m.getName().equals(name));
    }

    public static String getMethodQualifiedName(PsiMethod method) {
        return Objects.requireNonNull(method.getContainingClass()).getQualifiedName() + "." + method.getName();
    }

    public static PsiType createType(java.lang.reflect.Type javaType) {
        return createType(javaType, false);
    }

    public static PsiType createType(java.lang.reflect.Type javaType, boolean ellipsis) {
        return switch (javaType) {
            case Class<?> klass -> {
                if (klass.isPrimitive())
                    yield createPrimitiveType(klass);
                else if (klass.isArray())
                    yield ellipsis ? createEllipsisType(klass) : createArrayType(klass);
                else
                    yield createClassType(klass);
            }
            case ParameterizedType parameterizedType -> createParameterizedType(parameterizedType);
            case WildcardType wildcardType -> createWildcardType(wildcardType);
            case TypeVariable<?> typeVariable -> createVariableType(typeVariable);
            case GenericArrayType genericArrayType ->
                    ellipsis ? createEllipsisType(genericArrayType) : createArrayType(genericArrayType);
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
        if ((wildcardType.getUpperBounds().length == 0 ||
                wildcardType.getUpperBounds().length == 1 && wildcardType.getUpperBounds()[0].equals(Object.class)
                        && wildcardType.getLowerBounds().length == 0
        )) {
            return PsiWildcardType.createUnbounded(psiManager);
        }
        if (wildcardType.getLowerBounds().length > 0) {
            return PsiWildcardType.createSuper(psiManager, createType(wildcardType.getLowerBounds()[0]));
        } else
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
                var klass = Objects.requireNonNull(classType.resolve());
                yield Types.parameterizedName(
                        getClassCanonicalName(klass),
                        Utils.map(
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
        return getClassCanonicalName(Objects.requireNonNull(method.getContainingClass())) + "."
                + method.getName() + "("
                + Utils.join(Objects.requireNonNull(method.getParameterList()).getParameters(),
                param -> getCanonicalName(param.getType()))
                + ")";
    }

    public static boolean isObjectClass(PsiClass psiClass) {
        return Object.class.getName().equals(psiClass.getQualifiedName());
    }

    public static boolean isEnumClass(PsiClass psiClass) {
        return Enum.class.getName().equals(psiClass.getQualifiedName());
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
        return type == PsiType.VOID;
    }

    public static boolean isNullType(PsiType type) {
        return type.equals(PsiType.NULL);
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
        var declaringClass = Objects.requireNonNull(method.getContainingClass());
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
        var overrideDeclClass = Objects.requireNonNull(method.getContainingClass());
        var overriddenDeclClass = Objects.requireNonNull(overridden.getContainingClass());
        if (!overrideDeclClass.isInheritor(overriddenDeclClass, true)) {
            return false;
        }
        int paramCount = method.getParameterList().getParametersCount();
        if (overridden.getParameterList().getParametersCount() != paramCount
                || method.getTypeParameters().length != overridden.getTypeParameters().length) {
            return false;
        }
        var pipeline = getSubstitutorPipeline(createType(method.getContainingClass()), overridden.getContainingClass());
        var subst = PsiSubstitutor.createSubstitutor(
                Utils.zip(List.of(overridden.getTypeParameters()),
                        Utils.map(List.of(method.getTypeParameters()), TranspileUtils::createType))
        );
        pipeline.append(new SubstitutorPipeline(subst));
        for (int i = 0; i < paramCount; i++) {
            var paramType = Objects.requireNonNull(method.getParameterList().getParameter(i)).getType();
            var overriddenParamType = pipeline.substitute(Objects.requireNonNull(overridden.getParameterList().getParameter(i)).getType());
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
        return Utils.map(primitiveClasses, TranspileUtils::createPrimitiveType);
    }

    public static PsiPrimitiveType createPrimitiveType(Class<?> klass) {
        Utils.require(klass.isPrimitive());
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
            case KlassType classType -> {
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
                Utils.mapArray(parameterizedType.getActualTypeArguments(), TranspileUtils::createType, PsiType[]::new)
        );
    }

    public static PsiArrayType createArrayType(Class<?> klass) {
        Utils.require(klass.isArray());
        return new PsiArrayType(createType(klass.getComponentType()));
    }

    public static PsiArrayType createArrayType(GenericArrayType genericArrayType) {
        return new PsiArrayType(createType(genericArrayType.getGenericComponentType()));
    }

    public static PsiEllipsisType createEllipsisType(GenericArrayType genericArrayType) {
        return new PsiEllipsisType(createType(genericArrayType.getGenericComponentType()));
    }

    public static PsiArrayType createEllipsisType(Class<?> klass) {
        Utils.require(klass.isArray());
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
        return createType(requireNonNull(createClassType(rawClass).resolve()), typeArguments);
    }

    public static PsiClassType createType(PsiClass rawClass, PsiType...typeArguments) {
        return elementFactory.createType(rawClass, typeArguments);
    }

    public static PsiClassType createVariableType(java.lang.reflect.TypeVariable<?> typeVariable) {
        var genDecl = typeVariable.getGenericDeclaration();
        var index = List.of(genDecl.getTypeParameters()).indexOf(typeVariable);
        if (genDecl instanceof Class<?> klass)
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
        var psiMethod = Utils.find(psiClass.getMethods(), m -> matchMethod(m, method));
        if(psiMethod == null)
            throw new NullPointerException("Failed to find method " + ReflectionUtils.getMethodSignature(method)
                    + " in class " + psiClass.getQualifiedName());
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

    public static List<PsiClass> getOwnerClasses(PsiClass psiClass) {
        var list = new org.metavm.util.LinkedList<PsiClass>();
        var k = psiClass;
        do {
            list.addFirst(k);
            if(isStatic(k))
                break;
            k = k.getContainingClass();
        } while (k != null);
        return list;
    }

    public static List<PsiClass> getEnclosingClasses(PsiClass psiClass) {
        var list = new org.metavm.util.LinkedList<PsiClass>();
        var k = psiClass;
        do {
            list.addFirst(k);
            k = k.getContainingClass();
        } while (k != null);
        return list;
    }

    public static PsiElement createIdentifier(String text) {
        return elementFactory.createIdentifier(text);
    }

    public static void executeCommand(Runnable command) {
        CommandProcessor.getInstance().executeCommand(
                null,
                () -> {
                    try {
                        command.run();
                    } catch (RuntimeException e) {
                        CodeGenerator.logger.error("Fail to run compile command", e);
                        throw e;
                    }
                },
                null, null
        );
    }

    public static List<PsiStatement> extractBody(@Nullable PsiStatement body) {
        if (body == null) {
            return List.of();
        }
        if (body instanceof PsiBlockStatement block) {
            return List.of(block.getCodeBlock().getStatements());
        } else {
            return List.of(body);
        }
    }

    public static PsiStatement getLastBodyStatement(PsiLoopStatement loopStatement) {
        var body = extractBody(loopStatement.getBody());
        return body.get(body.size() - 1);
    }

    public static @Nullable PsiClass getNewExpressionClass(PsiNewExpression newExpression) {
        if(newExpression.getAnonymousClass() != null)
            return newExpression.getAnonymousClass();
        else if(newExpression.getClassReference() != null)
            return (PsiClass) Objects.requireNonNull(newExpression.getClassReference().resolve());
        else
            return null;
    }

    public static PsiPrimitiveType createIntType() {
        return elementFactory.createPrimitiveType("int");
    }

    public static boolean isFloatWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Float".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isShortWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Short".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isCharWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Character".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isByteWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Byte".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isBooleanWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Boolean".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isShortType(PsiType type) {
        return type.equals(PsiType.SHORT);
    }

    public static boolean isByteType(PsiType type) {
        return type.equals(PsiType.BYTE);
    }

    public static boolean isCharType(PsiType type) {
        return type.equals(PsiType.CHAR);
    }

    public static boolean isBooleanType(PsiType type) {
        return type.equals(PsiType.BOOLEAN);
    }

    public static boolean isStringType(PsiType type) {
        return type.getCanonicalText().equals("java.lang.String");
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
        return Utils.findRequired(superTypes, superType -> Objects.equals(superType.resolve(), superClass));
    }

    public static boolean matchType(PsiType type, Class<?> klass) {
        return matchType(type, klass, false);
    }

    public static boolean matchType(PsiType type, Class<?> klass, boolean erase) {
        if (type instanceof PsiClassType classType) {
            var resolved = Objects.requireNonNull(classType.resolve());
            if (erase) {
                resolved = eraseClass(resolved);
            }
            return matchClass(resolved, klass);
        }
        if (type instanceof PsiPrimitiveType primitiveType)
            return klass.isPrimitive() && primitiveType.getName().equals(klass.getName());
        if(type instanceof PsiArrayType arrayType)
            return klass.isArray() && matchType(arrayType.getComponentType(), klass.getComponentType(), true);
        return false;
    }

    public static boolean matchClass(PsiClass psiClass, Class<?> klass) {
        return Objects.equals(psiClass.getQualifiedName(), klass.getName());
    }

    public static void init(PsiElementFactory elementFactory, Project project) {
        TranspileUtils.elementFactory = elementFactory;
        TranspileUtils.project = project;
        intType = createIntType();
        nativeFunctionCallResolvers.clear();
        for (StdFunction def : StdFunction.values()) {
            for (Method javaMethod : def.getJavaMethods()) {
                nativeFunctionCallResolvers.add(new NativeFunctionCallResolver(getMethodSignature(javaMethod), def.get()));
            }
        }
        for (var def : StandardStaticMethods.getDefs()) {
            nativeFunctionCallResolvers.add(new NativeFunctionCallResolver(getMethodSignature(def.getMethod()), def.get()));
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
        return Objects.requireNonNull(
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
        return Objects.requireNonNull((PsiStatement) element,
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
        return findParent(element, Set.of(parentClasses));
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

    public static <T extends PsiElement> T getParent(PsiElement element, Class<T> parentClass) {
        return requireNonNull(findParent(element, parentClass));
    }

    public static @Nullable <T extends PsiElement> T findParent(PsiElement element, Class<T> parentClass) {
        return parentClass.cast(findParent(element, Set.of(parentClass)));
    }

    public static @Nullable <T extends PsiElement> T getProperParent(PsiElement element, Class<T> parentClass) {
        var p = element.getParent();
        return p != null ? parentClass.cast(findParent(p, Set.of(parentClass))) : null;
    }

    public static @NotNull <T extends PsiElement> T getParentNotNull(PsiElement element, Class<T> parentClass) {
        return Objects.requireNonNull(
                parentClass.cast(findParent(element, Set.of(parentClass))),
                () -> "Cannot find parent of type " + parentClass.getName() + " of element " + element.getText()
        );
    }

    public static boolean isLocalClass(PsiClass klass) {
        if(klass instanceof PsiAnonymousClass || klass instanceof PsiTypeParameter)
            return false;
        var parent = klass.getParent();
        while (parent != null) {
            if(parent instanceof PsiClass)
                return false;
            if(parent instanceof PsiMethod || parent instanceof PsiClassInitializer || parent instanceof PsiLambdaExpression)
                return true;
            parent = parent.getParent();
        }
        return false;
    }

    public static @Nullable PsiMethod getEnclosingMethod(PsiClass klass) {
        if(klass instanceof PsiTypeParameter)
            return null;
        var parent = klass.getParent();
        while (parent != null) {
            if(parent instanceof PsiClass)
                return null;
            if(parent instanceof PsiMethod method)
                return method;
            parent = parent.getParent();
        }
        return null;
    }

    public static @Nullable PsiElement getProperParent(PsiElement element, Set<Class<?>> parentClasses) {
        var parent = element.getParent();
        return parent != null ? findParent(parent, parentClasses) : null;
    }

    public static PsiElement getParent(PsiElement element, Set<Class<?>> parentClasses) {
        return Objects.requireNonNull(
                findParent(element, parentClasses),
                () -> "Cannot find parent of element '" + element.getText() + "' with classes " + parentClasses
        );
    }

    public static @Nullable PsiElement findParent(PsiElement element, Set<Class<?>> parentClasses) {
        PsiElement current = element;
        while (current != null && !ReflectionUtils.isInstance(parentClasses, current)) {
            current = current.getParent();
        }
        return current;
    }

    public static PsiElement getParentNotNull(PsiElement element, Set<Class<?>> parentClasses) {
        return requireNonNull(findParent(element, parentClasses));
    }

    public static Scope getBodyScope(PsiElement element) {
        return requireNonNull(element.getUserData(Keys.BODY_SCOPE));
    }

    public static PsiExpression createExpressionFromText(String text) {
        return elementFactory.createExpressionFromText(text, null);
    }

    public static PsiExpression createExpressionFromText(String text, PsiElement context) {
        return elementFactory.createExpressionFromText(text, context);
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
        return klass.getMethodByNameAndParamTypes(
                psiMethod.getName(),
                Utils.map(
                        psiMethod.getParameterList().getParameters(),
                        param -> resolveParameterType(param, typeResolver)
                )
        );
    }

    public static Type resolveParameterType(PsiParameter parameter, TypeResolver typeResolver) {
        return typeResolver.resolveNullable(parameter.getType(), ResolutionStage.INIT);
    }

    public static boolean isAnnotatedWithNullable(PsiModifierListOwner element) {
        return isAnnotationPresent(element, Nullable.class) || isAnnotationPresent(element, org.jetbrains.annotations.Nullable.class);
    }

    public static PsiClassType createTemplateType(PsiClass klass) {
        var paramList = klass.getTypeParameterList();
        List<PsiType> paramTypes = paramList != null ? Utils.map(
                paramList.getTypeParameters(),
                elementFactory::createType
        ) : List.of();
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
        return Objects.requireNonNull(catchSection.getParameter());
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
            current = current.getParent();
        }
        return false;
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

    public static boolean isSearchable(PsiClass psiClass) {
        Boolean ephemeral = (Boolean) getEntityAnnotationAttr(psiClass, "searchable");
        return ephemeral == Boolean.TRUE;
    }

    public static @Nullable Object getEntityAnnotationAttr(PsiClass psiClass, String attributeName) {
        return getEntityAnnotationAttr(psiClass, attributeName, null);
    }

    public static Object getEntityAnnotationAttr(PsiClass psiClass, String attributeName, Object defaultValue) {
        var value = getAnnotationAttribute(psiClass, Entity.class, attributeName);
        if (value != null)
            return value;
        if ((value = getAnnotationAttribute(psiClass, Value.class, attributeName)) != null)
            return value;
        if ((value = getAnnotationAttribute(psiClass, EntityStruct.class, attributeName)) != null)
            return value;
        if ((value =  getAnnotationAttribute(psiClass, ValueStruct.class, attributeName)) != null)
            return value;
        return defaultValue;
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
        return Utils.isNotBlank(bizName) ? bizName : klass.getName();
    }

    public static boolean isStruct(PsiClass psiClass) {
        return psiClass.isRecord()
                && Utils.count(psiClass.getConstructors(), m -> !(m instanceof LightRecordCanonicalConstructor)) == 0
                || psiClass.hasAnnotation(EntityStruct.class.getName()) || psiClass.hasAnnotation(ValueStruct.class.getName());
    }

    public static boolean isStatic(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isFinal(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.FINAL);
    }

    public static boolean isTransient(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.TRANSIENT);
    }

    public static boolean isAbstract(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.ABSTRACT);
    }

    public static boolean isDefault(PsiModifierListOwner modifierListOwner) {
        return modifierListOwner.hasModifierProperty(PsiModifier.DEFAULT);
    }

    public static boolean isNonStaticInnerClass(PsiClass psiClass) {
        return psiClass.getContainingClass() != null
                && !isStatic(psiClass) && !psiClass.isInterface() && !psiClass.isEnum() && !psiClass.isRecord();
    }

    public static boolean isInnerClassCopy(PsiClass psiClass) {
        return Boolean.TRUE.equals(psiClass.getUserData(Keys.INNER_CLASS_COPY));
    }

    public static String getQualifiedName(PsiMethod psiMethod) {
        return getQualifiedName(requireNonNull(psiMethod.getContainingClass())) + "." + psiMethod.getName();
    }

    public static String getQualifiedName(PsiField psiField) {
        return getQualifiedName(requireNonNull(psiField.getContainingClass())) + "." + psiField.getName();
    }

    public static String getFlowName(PsiMethod method) {
        String bizName = tryGetNameFromAnnotation(method, EntityFlow.class);
        return bizName != null ? bizName : getFlowCode(method);
    }

    public static String getFlowCode(PsiMethod method) {
        if (method.isConstructor()) {
            return requireNonNull(method.getContainingClass()).getName();
        } else {
            return method.getName();
        }
    }

    private static String tryGetNameFromAnnotation(PsiModifierListOwner element, Class<? extends Annotation> annotationClass) {
        var value = (String) getAnnotationAttribute(element, annotationClass, "value");
        return Utils.isNotBlank(value) ? value : null;
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

    public static boolean isAnnotationPresent(PsiModifierListOwner element, Class<? extends Annotation> annotationClass) {
        return getAnnotation(element, annotationClass) != null;
    }

    public static @Nullable PsiMethod findCanonicalConstructor(PsiClass psiClass) {
        var fieldTypes = Utils.map(getAllInstanceFields(psiClass), PsiField::getType);
        return Utils.find(psiClass.getMethods(),
                m -> m.isConstructor() && Utils.map(m.getParameterList().getParameters(), PsiParameter::getType).equals(fieldTypes));
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

    public static boolean isAnnotationPresent(PsiAnnotationOwner element, Class<? extends Annotation> annotationClass) {
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
        return getAnnotationAttribute(element, annotationClass, attributeName, null);
    }

    public static Object getAnnotationAttribute(PsiModifierListOwner element, Class<? extends Annotation> annotationClass, String attributeName, Object defaultValue) {
        var annotation = getAnnotation(element, annotationClass);
        if (annotation != null)
            return getAnnotationAttribute(annotation, attributeName, defaultValue);
        return defaultValue;
    }

    public static Object getFieldAnnotationAttribute(PsiField field, String attributeName, Object defaultValue) {
        var entityField = getAnnotation(field, EntityField.class);
        if(entityField != null)
            return getAnnotationAttribute(entityField, attributeName, defaultValue);
        var childEntity = getAnnotation(field, ChildEntity.class);
        if(childEntity != null)
            return getAnnotationAttribute(childEntity, attributeName, defaultValue);
        return defaultValue;
    }

    public static Object getAnnotationAttribute(PsiAnnotation annotation, String attributeName, @Nullable Object defaultValue) {
        var attr = Utils.find(annotation.getAttributes(), a -> a.getAttributeName().equals(attributeName));
        if (attr != null) {
            JvmAnnotationConstantValue value = (JvmAnnotationConstantValue) attr.getAttributeValue();
            var constValue = requireNonNull(value).getConstantValue();
            if (!isNullOrBlank(constValue))
                return constValue;
        }
        return defaultValue;
    }

    private static boolean isNullOrBlank(Object value) {
        if (value == null)
            return true;
        if (value instanceof String s)
            return s.isEmpty();
        return false;
    }

    private static final Map<String, String> typeNameMap = Map.ofEntries(
            Map.entry(String.class.getName(), "String"),
            Map.entry(Character.class.getName(), "Char"),
            Map.entry(Long.class.getName(), "Long"),
            Map.entry(Integer.class.getName(), "Int"),
            Map.entry(Short.class.getName(), "Short"),
            Map.entry(Byte.class.getName(), "Byte"),
            Map.entry(Double.class.getName(), "Double"),
            Map.entry(Float.class.getName(), "Float"),
            Map.entry(Boolean.class.getName(), "Boolean"),
            Map.entry(Date.class.getName(), "Time"),
            Map.entry(Object.class.getName(), "Any"),
            Map.entry(LinkedList.class.getName(), ArrayList.class.getName())
    );

    public static String getInternalName(PsiMethod method) {
        var paramTypeNames = new ArrayList<>(Utils.map(method.getParameterList().getParameters(),
                p -> getInternalName(p.getType(), true, method)));
        return getInternalName(createType(method.getContainingClass()), null) + "." +
                method.getName() + "(" + Utils.join(paramTypeNames, ",") + ")";
    }


    private static String getInternalName(PsiTypeParameterListOwner typeParameterOwner, PsiMethod current) {
        return switch (typeParameterOwner) {
            case PsiClass psiClass -> getInternalName(createType(psiClass), current);
            case PsiMethod method -> getInternalName(method);
            default -> throw new IllegalStateException("Unexpected value: " + typeParameterOwner);
        };
    }

    private static String getInternalName(PsiType type, boolean nullable, PsiMethod current) {
        if (nullable && !(type instanceof PsiPrimitiveType)) {
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
            var className = Objects.requireNonNullElse(psiClass.getQualifiedName(), psiClass.getName());
            if (classType.getParameters().length > 0) {
                var typeArgs = Utils.map(classType.getParameters(), t -> getInternalName(t, current));
                return Types.parameterizedName(typeNameMap.getOrDefault(className, className), typeArgs);
            } else
                return typeNameMap.getOrDefault(className, className);
        }
        if (type instanceof PsiPrimitiveType primitiveType) {
            return typeNameMap.get(primitiveType.getBoxedTypeName());
        }
        if (type instanceof PsiWildcardType wildcardType) {
            if (wildcardType.isSuper())
                return "[" + getInternalName(wildcardType.getBound(), current) + ",Any]";
            else if(wildcardType.getBound() != null)
                return "[Never," + getInternalName(wildcardType.getBound(), current) + "]";
            else
                return "[Never,Any]";
        }
        if(type instanceof PsiArrayType arrayType) {
            return getInternalName(arrayType.getComponentType(), true, current)  +"[]";
        }
        return type.getCanonicalText();
    }

    public static boolean isColonSwitch(PsiSwitchBlock statement) {
        var stmts = Objects.requireNonNull(statement.getBody()).getStatements();
        return stmts.length > 0 && stmts[0] instanceof PsiSwitchLabelStatement;
    }

    public static List<PsiField> getEnumConstants(PsiClass psiClass) {
        return Utils.filter(List.of(psiClass.getFields()), TranspileUtils::isEnumConstant);
    }

    public static PsiClass resolvePsiClass(PsiClassType classType) {
        return Objects.requireNonNull(classType.resolve(), () -> "Failed to resolve class " + classType.getCanonicalText());
    }

    private final static List<NativeFunctionCallResolver> nativeFunctionCallResolvers = new ArrayList<>();

    public static List<NativeFunctionCallResolver> getNativeFunctionCallResolvers() {
        return Collections.unmodifiableList(nativeFunctionCallResolvers);
    }

    public static boolean isEnum(PsiType psiType) {
        return psiType instanceof PsiClassType psiClassType && isEnum(requireNonNull(psiClassType.resolve()));
    }

    public static boolean isEnum(PsiClass psiClass) {
        return psiClass.isEnum() || isAnnotationPresent(psiClass, Enum.class);
    }

    public static SubstitutorPipeline getSubstitutorPipeline(PsiClassType type, PsiClass ancestor) {
        var pType = type.isRaw() ? createTemplateType(Objects.requireNonNull(type.resolve())) : type;
        return Objects.requireNonNull(findSubstitutorPipeline(pType, ancestor),
                () -> "Class " + ancestor.getQualifiedName() + " is not an ancestor of " + pType.getCanonicalText());
    }

    public static @Nullable SubstitutorPipeline findSubstitutorPipeline(PsiClassType type, PsiClass ancestor) {
        var generics = type.resolveGenerics();
        var klass = Objects.requireNonNull(generics.getElement());
        if(klass == ancestor)
            return new SubstitutorPipeline(generics.getSubstitutor());
        else {
            for (PsiClassType implement : klass.getSuperTypes()) {
                var r = findSubstitutorPipeline(implement, ancestor);
                if(r != null) {
                    r.append(new SubstitutorPipeline(generics.getSubstitutor()));
                    return r;
                }
            }
            return null;
        }
    }

    public static PsiClass getClass(Class<?> javaClass) {
        return Objects.requireNonNull(createClassType(javaClass).resolve());
    }

    public static PsiMethod getMethod(Method javaMethod) {
        var psiClass = getClass(javaMethod.getDeclaringClass());
        return Utils.findRequired(psiClass.getMethods(), m -> matchMethod(m, javaMethod));
    }

    public static PsiParameter getParameter(Parameter parameter) {
        var psiMethod = getMethod((Method) parameter.getDeclaringExecutable());
        var index = Utils.indexOf(parameter.getDeclaringExecutable().getParameters(), parameter);
        return psiMethod.getParameterList().getParameters()[index];
    }

    public static PsiParameter createParameter(String name, PsiType type, PsiElement context) {
        return elementFactory.createParameter(name, type, context);
    }

    public static PsiTypeParameter createTypeParameter(String name, PsiClassType[] superTypes) {
        return elementFactory.createTypeParameter(name, superTypes);
    }

    public static PsiTypeElement createTypeElement(String text, PsiElement context) {
        return elementFactory.createTypeElementFromText(text, context);
    }

    public static PsiJavaCodeReferenceElement createReferenceElement(String text, PsiElement context) {
        return elementFactory.createReferenceFromText(text, context);
    }

    public static boolean isAssignable(PsiClass base, PsiClass descendant) {
        return base == descendant || descendant.isInheritor(base, true);
    }

    public static Class<?> getJavaClass(PsiClass psiClass) {
        return ReflectionUtils.classForName(getJavaClassName(psiClass));
    }

    /**
    @return the class name the can be used for Class.forName
     */
    public static String getJavaClassName(PsiClass psiClass) {
        if(psiClass.getContainingClass() != null)
            return getJavaClassName(psiClass.getContainingClass()) + "$" + psiClass.getName();
        else
            return psiClass.getQualifiedName();
    }

    public static PsiParameter createParameterFromText(String text) {
        return elementFactory.createParameterFromText(text, null);
    }

    public static boolean isDiscarded(PsiElement element) {
        return Boolean.TRUE.equals(element.getUserData(Keys.DISCARDED));
    }

    public static List<PsiType> getAllTypeArgumentsForInnerClass(PsiClass psiClass, PsiSubstitutor substitutor) {
        var ownerClasses = getOwnerClasses(psiClass);
        var typeArgs = new ArrayList<PsiType>();
        for (PsiClass k : ownerClasses) {
            for (PsiTypeParameter typeParam : k.getTypeParameters()) {
                typeArgs.add(Objects.requireNonNull(substitutor.substitute(typeParam)));
            }
        }
        return typeArgs;
    }

    public static List<PsiClass> getAllClasses(PsiJavaFile file) {
        var queue = new LinkedList<>(List.of(file.getClasses()));
        var result = new ArrayList<PsiClass>();
        while (!queue.isEmpty()) {
            var k = queue.poll();
            result.add(k);
            for (PsiClass innerClass : k.getInnerClasses()) {
                queue.offer(innerClass);
            }
        }
        return result;
    }

    public static void printContexts(PsiElement element) {
        PsiElement e = element;
        do {
            logger.info("{}", getElementDesc(e));
            e = e.getParent();
        } while (e != null);
    }

    public static String getElementDesc(PsiElement element) {
        return switch (element) {
            case PsiJavaFile file -> "file: "+ file.getName();
            case PsiClass psiClass -> "class: " + psiClass.getQualifiedName();
            case PsiMethod method -> "method: " + method.getName();
            case PsiForeachStatement foreachStatement -> "<foreach statement>";
            case PsiForStatement forStatement -> "<for statement>";
            case PsiWhileStatement whileStatement -> "<while statement>";
            case PsiDoWhileStatement doWhileStatement -> "<do-while statement>";
            case PsiIfStatement ifStatement -> "<if statement>";
            case PsiCodeBlock codeBlock -> "<code block>";
            default -> element.getText();
        };
    }

    public static PsiBlockStatement createBlockStatement(PsiStatement...statements) {
        var blockStmt = (PsiBlockStatement) createStatementFromText("{}");
        var block = blockStmt.getCodeBlock();
        for (PsiStatement statement : statements) {
            block.add(statement);
        }
        return blockStmt;
    }

    public static boolean isLoop(PsiElement element) {
        return element instanceof PsiForStatement
                || element instanceof PsiForeachStatement
                || element instanceof PsiWhileStatement
                || element instanceof PsiDoWhileStatement;
    }

    public static boolean isBreakable(PsiElement element) {
        return isLabeledStatement(element) || isLoop(element);
    }

    public static boolean isLabeledStatement(PsiElement element) {
        return element.getParent() instanceof PsiLabeledStatement;
    }

    public static boolean isBlockStatement(PsiElement element) {
        return element instanceof PsiSwitchStatement || element instanceof PsiSwitchExpression
                || element instanceof PsiIfStatement || isLoop(element);
    }

    public static boolean isBlockStatementBody(PsiElement element) {
        return element instanceof PsiBlockStatement && isBlockStatement(element.getParent());
    }

    public static @Nullable String getLabel(PsiStatement statement) {
        return statement.getParent() instanceof PsiLabeledStatement labeledStatement ?
                labeledStatement.getLabelIdentifier().getText() : null;
    }

    public static PsiComment createComment(String comment) {
        return elementFactory.createCommentFromText(comment, null);
    }

    public static boolean isSuperCall(PsiStatement statement) {
        return statement instanceof PsiExpressionStatement exprStmt
                && exprStmt.getExpression() instanceof PsiMethodCallExpression callExpr
                && "super".equals(callExpr.getMethodExpression().getReferenceName());
    }


    public static boolean isObjectSuperCall(PsiStatement statement) {
        return statement instanceof PsiExpressionStatement exprStmt
                && exprStmt.getExpression() instanceof PsiMethodCallExpression callExpr
                && "super".equals(callExpr.getMethodExpression().getReferenceName())
                && isObjectClass(requireNonNull(requireNonNull(callExpr.resolveMethod()).getContainingClass()));
    }

    public static boolean isThisCall(PsiStatement statement) {
        return statement instanceof PsiExpressionStatement exprStmt
                && exprStmt.getExpression() instanceof PsiMethodCallExpression callExpr
                && "this".equals(callExpr.getMethodExpression().getReferenceName());
    }

    public static int getVariableIndex(PsiVariable variable) {
        return Objects.requireNonNull(variable.getUserData(Keys.VARIABLE_INDEX));
    }

    public static int getMaxLocals(PsiElement element) {
        if(element.getUserData(Keys.MAX_LOCALS) == null)
            printContexts(element);
        return Objects.requireNonNull(element.getUserData(Keys.MAX_LOCALS));
    }

    /**
     *
     * @return the index of the context where the variable is defined, or -1 if it's the variable is not captured
     */
    public static int getContextIndex(PsiVariable variable, PsiElement element) {
        var context = requireNonNull(findParent(variable, Set.of(PsiMethod.class, PsiLambdaExpression.class)));
        int idx = -1;
        PsiElement e = element;
        while (e != null && e != context) {
            if(e instanceof PsiLambdaExpression || e instanceof PsiMethod)
                idx++;
            e = e.getParent();
        }
        if(e == null)
            throw new IllegalStateException("Variable " + variable.getName() + " is not defined in an enclosing context of the " +
                    "lambda expression");
        return idx;
    }

    public static int getMethodContextIndex(PsiLambdaExpression expression) {
        PsiElement e = expression;
        int idx = 0;
        while (e != null && !(e instanceof PsiMethod) && !(e instanceof PsiClassInitializer)) {
            e = e.getParent();
            if(e instanceof PsiLambdaExpression)
                idx++;
        }
        Objects.requireNonNull(e, "Lambda expression is not enclosed by a method or class initializer");
        return idx;
    }

    public static boolean isAnnotationInstanceOf(PsiAnnotation annotation, Class<? extends Annotation> klass) {
        var aName = annotation.getQualifiedName();
        if(aName != null)
            return aName.equals(klass.getSimpleName()) || aName.equals(klass.getName());
        else
            return false;
    }

    public static PsiElement getEnclosingCallable(PsiClass klass) {
        var parent = findParent(klass, Set.of(PsiClass.class, PsiMethod.class, PsiClassInitializer.class));
        return parent instanceof PsiClass ? null : parent;
    }

    public static String getQualifiedName(PsiClass klass) {
        var containingClass = klass.getContainingClass();
        if (containingClass != null && !(containingClass instanceof PsiAnonymousClass))
            return getQualifiedName(containingClass) + "." + klass.getName();
        else
            return Objects.requireNonNullElseGet(klass.getQualifiedName(), klass::getName);
    }

    public static void insertAfterSuperCall(PsiStatement statement, PsiMethod constructor) {
        var block = requireNonNull(constructor.getBody());
        if(block.getStatements().length == 0 || isSuperCall(block.getStatements()[0]))
            block.add(statement);
        else if(!isThisCall(block.getStatements()[0]))
            block.addAfter(statement, block.getStatements()[0]);
    }

    public static String getClassName(PsiClass klass) {
        return Objects.requireNonNullElse(klass.getQualifiedName(), klass.getName());
    }

    public static boolean isIntegerType(PsiType type) {
        if (type instanceof PsiPrimitiveType t)
            return t.equals(PsiType.BYTE) || t.equals(PsiType.SHORT) || t.equals(PsiType.INT) || t.equals(PsiType.LONG);
        else
            return false;
    }

    public static boolean isFloatType(PsiType type) {
       return type.equals(PsiType.FLOAT);
    }

    public static boolean isIntType(PsiType type) {
        return type.equals(PsiType.INT);
    }

    public static String getIndexName(PsiNewExpression expression) {
        var argList = Objects.requireNonNull(expression.getArgumentList());
        return (String) getConstant(argList.getExpressions()[0]);
    }

    public static boolean isUniqueIndex(PsiNewExpression expression) {
        var argList = Objects.requireNonNull(expression.getArgumentList());
        return (boolean) getConstant(argList.getExpressions()[1]);
    }

    public static Object getConstant(PsiExpression expression) {
        return switch (expression) {
            case PsiLiteralExpression literalExpression -> literalExpression.getValue();
            case PsiReferenceExpression referenceExpression
                    when referenceExpression.resolve() instanceof PsiField field && field.getInitializer() != null ->
                getConstant(field.getInitializer());
            default -> throw new IllegalStateException("Cannot get constant from expression: " + expression);
        };
    }

    public static boolean isLongType(PsiType type) {
        return type.equals(PsiType.LONG);
    }

    public static boolean isDoubleType(PsiType type) {
        return type.equals(PsiType.DOUBLE);
    }

    public static boolean isLongWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Long".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isDoubleWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Double".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isIntWrapperType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var k = requireNonNull(classType.resolve());
            return "java.lang.Integer".equals(k.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isIndexType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            var klass = Objects.requireNonNull(classType.resolve());
            return Index.class.getName().equals(klass.getQualifiedName());
        }
        else
            return false;
    }

    public static boolean isEnumConstant(PsiField field) {
        return field instanceof PsiEnumConstant || isAnnotationPresent(field, EnumConstant.class);
    }

    public static PsiAnnotation createAnnotationFromText(String text) {
        return elementFactory.createAnnotationFromText(text, null);
    }

}
