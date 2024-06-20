package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.*;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Method;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.flow.Values;
import org.metavm.object.type.Index;
import org.metavm.object.type.*;
import org.metavm.util.CompilerConfig;
import org.metavm.util.IdentitySet;
import org.metavm.util.NamingUtils;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.*;

public class Declarator extends CodeGenVisitor {

    public static final Logger logger = LoggerFactory.getLogger(Declarator.class);

    private final TypeResolver typeResolver;

    private final IEntityContext context;

    private final LinkedList<Klass> classStack = new LinkedList<>();

    private @Nullable Index currentIndex;

    private final IdentitySet<Field> visitedFields = new IdentitySet<>();

    private final IdentitySet<Method> visitedMethods = new IdentitySet<>();

    public Declarator(TypeResolver typeResolver, IEntityContext context) {
        this.typeResolver = typeResolver;
        this.context = context;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        if (TranspileUtils.hasAnnotation(psiClass, EntityIndex.class)) {
            Index index = NncUtils.find(currentClass().getIndices(), idx -> Objects.equals(idx.getCode(), psiClass.getName()));
            if (index == null) {
                index = new Index(
                        currentClass(),
                        TranspileUtils.getIndexName(psiClass),
                        psiClass.getName(),
                        "",
                        TranspileUtils.isUniqueIndex(psiClass)
                );
            } else {
                index.setName(TranspileUtils.getIndexName(psiClass));
            }
            currentIndex = index;
            psiClass.putUserData(Keys.INDEX, index);
            super.visitClass(psiClass);
            return;
        }
        visitedFields.clear();
        visitedMethods.clear();
        var klass = Objects.requireNonNull(psiClass.getUserData(Keys.MV_CLASS),
                () -> "Meta class not found for '" + psiClass.getQualifiedName() + "'");
        klass.setStage(ResolutionStage.DECLARATION);
        if (!klass.isInterface()) {
            if (klass.findSelfMethodByCode("<init>") == null) {
                MethodBuilder.newBuilder(klass, "<init>", "<init>")
                        .access(Access.PRIVATE)
                        .build();
            }
            if (klass.findSelfMethodByCode("<cinit>") == null) {
                MethodBuilder.newBuilder(klass, "<cinit>", "<cinit>")
                        .isStatic(true)
                        .access(Access.PRIVATE)
                        .build();
            }
            var initMethod = Objects.requireNonNull(klass.findSelfMethodByCode("<init>"));
            var cinitMethod = Objects.requireNonNull(klass.findSelfMethodByCode("<cinit>"));
            initMethod.clearContent();
            cinitMethod.clearContent();
            visitedMethods.add(initMethod);
            visitedMethods.add(cinitMethod);
        }
        klass.clearAttributes();
        var componentAnno = TranspileUtils.getAnnotation(psiClass, Component.class);
        PsiAnnotation configurationAnno;
        if (componentAnno != null) {
            klass.setAttribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT);
            klass.setAttribute(AttributeNames.BEAN_NAME,
                    (String) TranspileUtils.getAnnotationAttribute(componentAnno, "value", getDefaultBeanName(klass)));
        } else if ((configurationAnno = TranspileUtils.getAnnotation(psiClass, Configuration.class)) != null) {
            klass.setAttribute(AttributeNames.BEAN_KIND, BeanKinds.CONFIGURATION);
            klass.setAttribute(AttributeNames.BEAN_NAME,
                    (String) TranspileUtils.getAnnotationAttribute(configurationAnno, "value", getDefaultBeanName(klass)));
        }
        classStack.push(klass);
        super.visitClass(psiClass);
        classStack.pop();
        var removedFields = NncUtils.exclude(klass.getFields(), visitedFields::contains);
        removedFields.forEach(klass::removeField);
        var removedMethods = NncUtils.filter(klass.getMethods(),
                m -> !visitedMethods.contains(m));
        var fieldIndices = new HashMap<String, Integer>();
        for (int i = 0; i < psiClass.getFields().length; i++) {
            fieldIndices.put(psiClass.getFields()[i].getName(), i);
        }
        klass.sortFields(Comparator.comparingInt(f -> fieldIndices.get(f.getCode())));
        var methodIndices = new HashMap<Method, Integer>();
        for (int i = 0; i < psiClass.getMethods().length; i++) {
            var method = psiClass.getMethods()[i].getUserData(Keys.Method);
            if (method != null)
                methodIndices.put(method, i);
        }
        klass.sortMethods(Comparator.comparingInt(m -> methodIndices.getOrDefault(m, -1)));
        removedMethods.forEach(klass::removeMethod);
//        metaClass.setStage(ResolutionStage.DECLARATION);
    }

    private String getDefaultBeanName(Klass klass) {
        var klassName = klass.getCodeNotNull();
        var idx = klassName.lastIndexOf('.');
        var simpleName = idx == -1 ? klassName : klassName.substring(idx + 1);
        return NamingUtils.firstCharToLowerCase(simpleName);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (CompilerConfig.isMethodBlacklisted(method))
            return;
        var psiClass = requireNonNull(method.getContainingClass());
        if (TranspileUtils.getAnnotation(psiClass, EntityIndex.class) != null)
            return;
        List<PsiMethod> overriddenMethods = TranspileUtils.getOverriddenMethods(method);
        List<Method> overridden = new ArrayList<>();
        for (PsiMethod overriddenMethod : overriddenMethods) {
            var overriddenMethodCls = NncUtils.requireNonNull(overriddenMethod.getContainingClass());
            if (Object.class.getName().equals(overriddenMethodCls.getQualifiedName()))
                continue;
            var overriddenMethodType = TranspileUtils.createTemplateType(overriddenMethodCls);
            overridden.add(TranspileUtils.getMethidByJavaMethod(
                    Types.resolveKlass(typeResolver.resolveDeclaration(overriddenMethodType)),
                    overriddenMethod, typeResolver)
            );
        }
        List<PsiType> implicitTypeArgs = method.isConstructor() && currentClass().isEnum() ?
                List.of(TranspileUtils.createType(String.class), TranspileUtils.createPrimitiveType(int.class)) : List.of();
        var internalName = TranspileUtils.getInternalName(method, implicitTypeArgs);
        var flow = NncUtils.find(currentClass().getMethods(), f -> f.getInternalName(null).equals(internalName));
        if (flow != null)
            method.putUserData(Keys.Method, flow);
        List<Parameter> resolvedParams = new ArrayList<>();
        if (method.isConstructor() && currentClass().isEnum())
            resolvedParams.addAll(getEnumConstructorParams());
        resolvedParams.addAll(processParameters(method.getParameterList()));
        if (flow == null) {
            flow = MethodBuilder.newBuilder(currentClass(), getFlowName(method), getFlowCode(method))
                    .isConstructor(method.isConstructor())
                    .isStatic(method.getModifierList().hasModifierProperty(PsiModifier.STATIC))
                    .access(resolveAccess(method.getModifierList()))
                    .isAbstract(method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT))
                    .parameters(resolvedParams)
                    .returnType(getReturnType(method))
                    .build();
            method.putUserData(Keys.Method, flow);
        } else {
            flow.setName(getFlowName(method));
            NncUtils.biForEach(
                    flow.getParameters(), resolvedParams,
                    (param, resolvedParam) -> {
                        param.setName(resolvedParam.getName());
                        param.setAttributes(resolvedParam.getAttributes());
                    }
            );
            flow.setReturnType(getReturnType(method));
        }
        flow.clearAttributes();
        var beanAnnotation = TranspileUtils.getAnnotation(method, Bean.class);
        if (beanAnnotation != null) {
            var beanName = (String) TranspileUtils.getAnnotationAttribute(beanAnnotation, "value", flow.getCodeNotNull());
            flow.setAttribute(AttributeNames.BEAN_NAME, beanName);
        }
        visitedMethods.add(flow);
        for (PsiTypeParameter typeParameter : method.getTypeParameters()) {
            var typeVar = typeResolver.resolveTypeVariable(typeParameter).getVariable();
            if (typeVar.getGenericDeclaration() != flow)
                typeVar.setGenericDeclaration(flow);
        }
        flow.setOverridden(overridden);
    }

    private Access resolveAccess(PsiModifierList modifierList) {
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC))
            return Access.PUBLIC;
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE))
            return Access.PRIVATE;
        return Access.PACKAGE;
    }

    private List<Parameter> getEnumConstructorParams() {
        return List.of(
                new Parameter(null, "name", "__name__", Types.getStringType()),
                new Parameter(null, "ordinal", "__ordinal__", Types.getLongType())
        );
    }

    private List<Parameter> processParameters(PsiParameterList parameterList) {
        return NncUtils.map(
                parameterList.getParameters(),
                param -> {
                    var p = new Parameter(null, getFlowParamName(param), param.getName(), resolveParameterType(param));
                    var beanName = (String) TranspileUtils.getAnnotationAttribute(param, Resource.class, "value");
                    if (beanName != null)
                        p.setAttribute(AttributeNames.BEAN_NAME, beanName);
                    return p;
                }
        );
    }

    private Type resolveParameterType(PsiParameter parameter) {
        var type = resolveType(parameter.getType());
        if (TranspileUtils.getAnnotation(parameter, Nullable.class) != null)
            type = Types.getNullableType(type);
        return type;
    }


    @Override
    public void visitField(PsiField psiField) {
        var psiClass = requireNonNull(((PsiMember) psiField).getContainingClass());
        if (TranspileUtils.getAnnotation(psiClass, EntityIndex.class) != null) {
            var index = requireNonNull(currentIndex);
            var indexField = NncUtils.find(index.getFields(), f -> Objects.equals(f.getName(), psiField.getName()));
            if (indexField == null)
                new IndexField(index, getBizFieldName(psiField), psiField.getName(), Values.nullValue());
            return;
        }
        var type = resolveType(psiField.getType());
        if (TranspileUtils.getAnnotation(psiField, Nullable.class) != null)
            type = Types.getNullableType(type);
        var klass = currentClass();
        var field = TranspileUtils.isStatic(psiField) ?
                klass.findSelfStaticFieldByCode(psiField.getName())
                : klass.findSelfFieldByCode(psiField.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getBizFieldName(psiField), psiField.getName(), currentClass(), type)
                    .access(getAccess(psiField))
                    .unique(TranspileUtils.isUnique(psiField))
                    .isChild(TranspileUtils.isChild(psiField))
                    .isStatic(requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC))
                    .build();
        } else {
            field.setName(getBizFieldName(psiField));
            field.setType(type);
            field.setAccess(getAccess(psiField));
            field.setUnique(TranspileUtils.isUnique(psiField));
        }
        visitedFields.add(field);
        if (TranspileUtils.isTitleField(psiField))
            currentClass().setTitleField(field);
        psiField.putUserData(Keys.FIELD, field);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var field = currentClass().findSelfStaticFieldByCode(enumConstant.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getEnumConstantName(enumConstant), enumConstant.getName(), currentClass(), currentClass().getType())
                    .isChild(true)
                    .isStatic(true)
                    .build();
        } else
            field.setName(getEnumConstantName(enumConstant));
        enumConstant.putUserData(Keys.FIELD, field);
    }

    private Klass currentClass() {
        return NncUtils.requireNonNull(classStack.peek());
    }

    private Type getReturnType(PsiMethod method) {
        var type = method.isConstructor() ?
                TranspileUtils.createTemplateType(requireNonNull(method.getContainingClass())) :
                method.getReturnType();
        var metaType = resolveType(type);
        if (TranspileUtils.getAnnotation(method, Nullable.class) != null ||
                method.getReturnType() != null && TranspileUtils.getAnnotation(method.getReturnType(), Nullable.class) != null)
            metaType = Types.getNullableType(metaType);
        return metaType;
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType);
    }

}
