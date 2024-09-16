package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.*;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.*;
import org.metavm.object.type.Index;
import org.metavm.object.type.*;
import org.metavm.util.CompilerConfig;
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

    private final LinkedList<ClassInfo> classStack = new LinkedList<>();

    private @Nullable Index currentIndex;

    public Declarator(TypeResolver typeResolver, IEntityContext context) {
        this.typeResolver = typeResolver;
        this.context = context;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        if(TranspileUtils.isDiscarded(psiClass))
            return;
        if (TranspileUtils.hasAnnotation(psiClass, EntityIndex.class)) {
            var klass = currentClass().klass;
            Index index = NncUtils.find(klass.getIndices(), idx -> Objects.equals(idx.getCode(), psiClass.getName()));
            if (index == null) {
                index = new Index(
                        klass,
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
        var klass = typeResolver.getKlass(psiClass);
        klass.setStage(ResolutionStage.DECLARATION);
        var classInfo = new ClassInfo(klass);
        if (!klass.isInterface()) {
            if (klass.findSelfMethodByCode("<init>") == null) {
                MethodBuilder.newBuilder(klass, "<init>", "<init>")
                        .access(Access.PRIVATE)
                        .build();
            }
            var initMethod = Objects.requireNonNull(klass.findSelfMethodByCode("<init>"));
            initMethod.clearContent();
            classInfo.visitedMethods.add(initMethod);
        }
        if (klass.findSelfMethodByCode("<cinit>") == null) {
            MethodBuilder.newBuilder(klass, "<cinit>", "<cinit>")
                    .isStatic(true)
                    .access(Access.PRIVATE)
                    .build();
        }
        var cinitMethod = Objects.requireNonNull(klass.findSelfMethodByCode("<cinit>"));
        cinitMethod.clearContent();
        classInfo.visitedMethods.add(cinitMethod);
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
        classStack.push(classInfo);
        super.visitClass(psiClass);
        classStack.pop();
        if(klass.isEnum())
            classInfo.visitedMethods.add(Flows.saveValuesMethod(klass));
        var removedFields = NncUtils.exclude(klass.getFields(), classInfo.visitedFields::contains);
        removedFields.forEach(Field::setMetadataRemoved);
        var removedMethods = NncUtils.filter(klass.getMethods(),
                m -> !classInfo.visitedMethods.contains(m));
        var fieldIndices = new HashMap<String, Integer>();
        for (int i = 0; i < psiClass.getFields().length; i++) {
            fieldIndices.put(psiClass.getFields()[i].getName(), i);
        }
        klass.sortFields(Comparator.comparingInt(f -> fieldIndices.getOrDefault(f.getCode(), Integer.MAX_VALUE)));
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
        var klass = currentClass().klass;
        List<Method> overridden = new ArrayList<>();
        for (PsiMethod overriddenMethod : overriddenMethods) {
            var overriddenMethodCls = NncUtils.requireNonNull(overriddenMethod.getContainingClass());
            if (Object.class.getName().equals(overriddenMethodCls.getQualifiedName()))
                continue;
            var overriddenMethodType = TranspileUtils.createTemplateType(overriddenMethodCls);
            var k = Types.resolveKlass(typeResolver.resolveDeclaration(overriddenMethodType));
            var o = TranspileUtils.getMethidByJavaMethod(k, overriddenMethod, typeResolver);
            var k1 = Objects.requireNonNull(klass.findAncestorByTemplate(k));
            var o1 = k1.getMethod(m -> m.getEffectiveVerticalTemplate() == o);
            overridden.add(o1);
        }
        List<PsiType> implicitTypeArgs = method.isConstructor() && klass.isEnum() ?
                List.of(TranspileUtils.createType(String.class), TranspileUtils.createPrimitiveType(int.class)) : List.of();
        var internalName = TranspileUtils.getInternalName(method, implicitTypeArgs);
        var flow = NncUtils.find(klass.getMethods(), f -> f.getInternalName(null).equals(internalName));
        if (flow != null)
            method.putUserData(Keys.Method, flow);
        var access = resolveAccess(method.getModifierList());
        var isStatic = method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
        var isAbstract = method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT);
        if (flow == null) {
            flow = MethodBuilder.newBuilder(klass, getFlowName(method), getFlowCode(method))
                    .isConstructor(method.isConstructor())
                    .isStatic(isStatic)
                    .access(access)
                    .isAbstract(isAbstract)
                    .build();
            method.putUserData(Keys.Method, flow);
        } else {
            flow.setName(getFlowName(method));
            flow.setCode(getFlowCode(method));
            flow.setAccess(access);
            flow.setStatic(isStatic);
            flow.setAbstract(isAbstract);
        }
        flow.setTypeParameters(NncUtils.map(
                method.getTypeParameters(),
                t -> typeResolver.resolveTypeVariable(t).getVariable()
        ));
        List<Parameter> parameters = new ArrayList<>();
        if (method.isConstructor() && klass.isEnum())
            parameters.addAll(getEnumConstructorParams(flow));
        parameters.addAll(processParameters(method.getParameterList(), flow));
        flow.setParameters(parameters);
        flow.setReturnType(getReturnType(method));

        flow.clearAttributes();
        var beanAnnotation = TranspileUtils.getAnnotation(method, Bean.class);
        if (beanAnnotation != null) {
            var beanName = (String) TranspileUtils.getAnnotationAttribute(beanAnnotation, "value", flow.getCodeNotNull());
            flow.setAttribute(AttributeNames.BEAN_NAME, beanName);
        }
        currentClass().visitedMethods.add(flow);
        flow.setOverridden(overridden);
    }

    private Access resolveAccess(PsiModifierList modifierList) {
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC))
            return Access.PUBLIC;
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE))
            return Access.PRIVATE;
        return Access.PACKAGE;
    }

    private List<Parameter> getEnumConstructorParams(Method method) {
        var nameParam = method.findParameter(p -> "__name__".equals(p.getCode()));
        if(nameParam == null)
            nameParam = new Parameter(null, "name", "__name__", Types.getStringType());
        var ordinalParam = method.findParameter(p -> "__ordinal__".equals(p.getCode()));
        if(ordinalParam == null)
            ordinalParam = new Parameter(null, "ordinal", "__ordinal__", Types.getLongType());
        return List.of(nameParam, ordinalParam);
    }

    private List<Parameter> processParameters(PsiParameterList parameterList, Method method) {
        return NncUtils.map(
                parameterList.getParameters(),
                param -> {
                    var p = method.findParameter(p1 -> param.getName().equals(p1.getCode()));
                    var name = getFlowParamName(param);
                    var type = resolveParameterType(param);
                    if(p == null)
                        p = new Parameter(null, name, param.getName(), type);
                    else {
                        p.setName(name);
                        p.setType(type);
                    }
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
        var klass = currentClass().klass;
        var isStatic = TranspileUtils.isStatic(psiField);
        var fieldTag = (int) TranspileUtils.getFieldAnnotationAttribute(psiField, "tag", -1);
        Field field;
        if(fieldTag == -1) {
            field = isStatic ? klass.findSelfStaticFieldByCode(psiField.getName())
                    :klass.findSelfFieldByCode(psiField.getName());
        }
        else {
            field = isStatic ? klass.findSelfStaticField(f -> Objects.equals(f.getSourceCodeTag(), fieldTag))
                    : klass.findSelfField(f -> Objects.equals(f.getSourceCodeTag(), fieldTag));
        }
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getBizFieldName(psiField), psiField.getName(), klass, type)
                    .access(getAccess(psiField))
                    .unique(TranspileUtils.isUnique(psiField))
                    .isChild(TranspileUtils.isChild(psiField))
                    .isStatic(requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC))
                    .sourceCodeTag(fieldTag != -1 ? fieldTag : null)
                    .build();
        } else {
            field.setName(getBizFieldName(psiField));
            field.setCode(psiField.getName());
            field.setType(type);
            field.setAccess(getAccess(psiField));
            field.setUnique(TranspileUtils.isUnique(psiField));
            field.setChild(TranspileUtils.isChild(psiField));
        }
        currentClass().visitedFields.add(field);
        if (TranspileUtils.isTitleField(psiField))
            klass.setTitleField(field);
        else if(klass.getTitleField() == field)
            klass.setTitleField(null);
        if((Boolean) TranspileUtils.getFieldAnnotationAttribute(psiField, "removed", false))
            field.setMetadataRemoved();
        else
            field.setState(MetadataState.READY);
        psiField.putUserData(Keys.FIELD, field);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var classInfo = currentClass();
        var klass = classInfo.klass;
        var field = klass.findSelfStaticFieldByCode(enumConstant.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getEnumConstantName(enumConstant), enumConstant.getName(), klass, klass.getType())
                    .isChild(true)
                    .isStatic(true)
                    .build();
        } else
            field.setName(getEnumConstantName(enumConstant));
        var ecd = klass.findEnumConstantDef(e -> e.getName().equals(enumConstant.getName()));
        if(ecd == null)
            ecd = new EnumConstantDef(klass, enumConstant.getName(), classInfo.nextEnumConstantOrdinal(), List.of());
        else
            ecd.setOrdinal(classInfo.nextEnumConstantOrdinal());
        enumConstant.putUserData(Keys.FIELD, field);
        enumConstant.putUserData(Keys.ENUM_CONSTANT_DEF, ecd);
    }

    private ClassInfo currentClass() {
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

    private static class ClassInfo {
        private final Klass klass;
        private int nextEnumConstantOrdinal;
        private final Set<Field> visitedFields = new HashSet<>();
        private final Set<Method> visitedMethods = new HashSet<>();

        private ClassInfo(Klass klass) {
            this.klass = klass;
        }

        private int nextEnumConstantOrdinal() {
            return nextEnumConstantOrdinal++;
        }
    }

}
