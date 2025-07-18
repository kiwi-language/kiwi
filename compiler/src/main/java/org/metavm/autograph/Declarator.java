package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.Bean;
import org.metavm.api.Component;
import org.metavm.api.Configuration;
import org.metavm.api.Resource;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.flow.Method;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.util.CompilerConfig;
import org.metavm.util.DebugEnv;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.*;

public class Declarator extends VisitorBase {

    public static final Logger logger = LoggerFactory.getLogger(Declarator.class);

    private final boolean tracing = DebugEnv.traceCompilation;

    private final PsiClass psiClass;

    private final TypeResolver typeResolver;

    private final LinkedList<ClassInfo> classStack = new LinkedList<>();

    public Declarator(PsiClass psiClass, TypeResolver typeResolver) {
        this.psiClass = psiClass;
        this.typeResolver = typeResolver;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        if(TranspileUtils.isDiscarded(psiClass))
            return;
        if (psiClass != this.psiClass)
            return;
        var klass = typeResolver.getKlass(psiClass);
        if (tracing)
            logger.trace("Declaring klass {}", klass.getTypeDesc());
        var parent = TranspileUtils.getProperParent(psiClass, Set.of(PsiMethod.class, PsiClass.class));
        if(parent instanceof PsiMethod parentMethod)
            requireNonNull(parent.getUserData(Keys.Method),
                    () -> "Cannot find parent method for class: " + psiClass.getName() + " inside " + TranspileUtils.getQualifiedName(parentMethod) + " in file " + psiClass.getContainingFile().getName())
                    .addLocalKlass(klass);
        klass.setKlasses(Utils.map(psiClass.getInnerClasses(),
                k -> Objects.requireNonNull(k.getUserData(Keys.MV_CLASS), () -> "Cannot find metavm class for class " + k.getQualifiedName())));
        if (psiClass.getSuperClass() != null &&
                !Objects.equals(psiClass.getSuperClass().getQualifiedName(), Object.class.getName())) {
            klass.setSuperType(((ClassType) typeResolver.resolveTypeOnly(TranspileUtils.getSuperClassType(psiClass))));
        }
        else
            klass.setSuperType(null);
        klass.setInterfaces(
                Utils.map(
                        TranspileUtils.getInterfaceTypes(psiClass),
                        it -> ((ClassType) typeResolver.resolveTypeOnly(it))
                )
        );
        klass.setStage(ResolutionStage.DECLARATION);
        var classInfo = new ClassInfo(klass);
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
        var removedFields = Utils.exclude(klass.getFields(), classInfo.visitedFields::contains);
        removedFields.forEach(f -> {
            f.setInitializer(null);
            f.setMetadataRemoved();
            f.resetTypeIndex();
        });
        var removedStaticFields = Utils.exclude(klass.getStaticFields(), classInfo.visitedFields::contains);
        removedStaticFields.forEach(klass::removeField);
        var fieldIndices = new HashMap<String, Integer>();
        for (int i = 0; i < psiClass.getFields().length; i++) {
            fieldIndices.put(psiClass.getFields()[i].getName(), i);
        }
        klass.sortFields(Comparator.comparingInt(f -> fieldIndices.getOrDefault(f.getName(), Integer.MAX_VALUE)));
        if (klass.isEnum()) {
            int ordinal = 0;
            for (Field ec : klass.getEnumConstants()) {
                ec.setOrdinal(ordinal++);
            }
        }
        var methodIndices = new HashMap<Method, Integer>();
        for (int i = 0; i < psiClass.getMethods().length; i++) {
            var method = psiClass.getMethods()[i].getUserData(Keys.Method);
            if (method != null)
                methodIndices.put(method, i);
        }
        klass.sortMethods(Comparator.comparingInt(m -> methodIndices.getOrDefault(m, -1)));
        Utils.exclude(klass.getMethods(), classInfo.visitedMethods::contains).forEach(klass::removeMethod);
        for (PsiField psiField : psiClass.getFields()) {
            var initializer = psiField.getUserData(Keys.INITIALIZER);
            if (initializer != null) {
                var field = Objects.requireNonNull(psiField.getUserData(Keys.FIELD));
                field.setInitializer(Objects.requireNonNull(initializer.getUserData(Keys.Method)));
            }
        }
//        metaClass.setStage(ResolutionStage.DECLARATION);
    }

    private String getDefaultBeanName(Klass klass) {
        return NamingUtils.firstCharsToLowerCase(klass.getName());
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (CompilerConfig.isMethodBlacklisted(method))
            return;
        var klass = currentClass().klass;
        var internalName = TranspileUtils.getInternalName(method);
        var flow = Utils.find(klass.getMethods(), f -> f.getInternalName(null).equals(internalName));
        if (flow != null)
            method.putUserData(Keys.Method, flow);
        var access = resolveAccess(method.getModifierList());
        var isStatic = method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
        var isAbstract = method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT);
        if (flow == null) {
            flow = MethodBuilder.newBuilder(klass, getFlowName(method))
                    .id(TmpId.random())
                    .isConstructor(method.isConstructor())
                    .isStatic(isStatic)
                    .access(access)
                    .isAbstract(isAbstract)
                    .build();
            method.putUserData(Keys.Method, flow);
        } else {
            flow.clearContent();
            flow.setName(getFlowName(method));
            flow.setAccess(access);
            flow.setStatic(isStatic);
            flow.setAbstract(isAbstract);
            flow.setKlasses(List.of());
        }
        flow.setTypeParameters(Utils.map(
                method.getTypeParameters(),
                t -> typeResolver.resolveTypeVariable(t).getVariable()
        ));
        var parameters = processParameters(method.getParameterList(), flow);
        flow.update(parameters, getReturnType(method));

        flow.clearAttributes();
        var beanAnnotation = TranspileUtils.getAnnotation(method, Bean.class);
        if (beanAnnotation != null) {
            var beanName = (String) TranspileUtils.getAnnotationAttribute(beanAnnotation, "value", flow.getName());
            flow.setAttribute(AttributeNames.BEAN_NAME, beanName);
        }
        currentClass().visitedMethods.add(flow);
    }

    private Access resolveAccess(PsiModifierList modifierList) {
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC))
            return Access.PUBLIC;
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE))
            return Access.PRIVATE;
        return Access.PACKAGE;
    }

    private List<Parameter> getEnumConstructorParams(Method method) {
        var nameParam = method.findParameter(p -> "__name__".equals(p.getName()));
        if(nameParam == null)
            nameParam = new Parameter(TmpId.random(), "name", Types.getStringType(), method);
        else
            nameParam.setType(Types.getStringType()); // Adding type constant into constant pool
        var ordinalParam = method.findParameter(p -> "__ordinal__".equals(p.getName()));
        if(ordinalParam == null)
            ordinalParam = new Parameter(TmpId.random(), "ordinal", Types.getIntType(), method);
        else
            ordinalParam.setType(Types.getIntType());
        return List.of(nameParam, ordinalParam);
    }

    private List<Parameter> processParameters(PsiParameterList parameterList, Method method) {
        return Utils.map(
                parameterList.getParameters(),
                param -> {
                    var p = method.findParameter(p1 -> param.getName().equals(p1.getName()));
                    var name = param.getName();
                    var type = resolveParameterType(param);
                    if(p == null)
                        p = new Parameter(TmpId.random(), name, type, method);
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
        return resolveType(parameter.getType(), TranspileUtils.isAnnotationPresent(parameter, Nonnull.class));
    }

    @Override
    public void visitField(PsiField psiField) {
        var type = resolveNullableType(psiField.getType());
        var classInfo = currentClass();
        var klass = classInfo.klass;
        var isStatic = TranspileUtils.isStatic(psiField);
        var fieldTag = (int) TranspileUtils.getFieldAnnotationAttribute(psiField, "tag", -1);
        Field field;
        if(fieldTag == -1) {
            field = isStatic ? klass.findSelfStaticFieldByName(psiField.getName())
                    :klass.findSelfInstanceFieldByName(psiField.getName());
        }
        else {
            field = isStatic ? klass.findSelfStaticField(f -> Objects.equals(f.getSourceTag(), fieldTag))
                    : klass.findSelfInstanceField(f -> Objects.equals(f.getSourceTag(), fieldTag));
        }
        var modList = requireNonNull(psiField.getModifierList());
        var isTransient = modList.hasModifierProperty(PsiModifier.TRANSIENT);
        if (field == null) {
            field = FieldBuilder.newBuilder(psiField.getName(), klass, type)
                    .id(TmpId.random())
                    .access(getAccess(psiField))
                    .isStatic(modList.hasModifierProperty(PsiModifier.STATIC))
                    .isTransient(isTransient)
                    .sourceTag(fieldTag != -1 ? fieldTag : null)
                    .build();
        } else {
            field.setName(getBizFieldName(psiField));
            field.setType(type);
            field.setAccess(getAccess(psiField));
            field.setTransient(isTransient);
        }
        currentClass().visitedFields.add(field);
        if (TranspileUtils.isTitleField(psiField))
            klass.setTitleField(field);
        else if(klass.getSelfTitleField() == field)
            klass.setTitleField(null);
        if((Boolean) TranspileUtils.getFieldAnnotationAttribute(psiField, "removed", false))
            field.setMetadataRemoved();
        else
            field.setState(MetadataState.READY);
        psiField.putUserData(Keys.FIELD, field);
        field.setEnumConstant(TranspileUtils.isEnumConstant(psiField));
    }

    private ClassInfo currentClass() {
        return Objects.requireNonNull(classStack.peek());
    }

    private Type getReturnType(PsiMethod method) {
        if(method.isConstructor())
            return Types.getVoidType();
        else
            return resolveNullableType(method.getReturnType());
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType);
    }

    private Type resolveType(PsiType psiType, boolean nonnull) {
        return nonnull ? typeResolver.resolve(psiType, ResolutionStage.INIT) :
                typeResolver.resolveNullable(psiType, ResolutionStage.INIT);
    }

    private Type resolveNullableType(PsiType psiType) {
        return typeResolver.resolveNullable(psiType, ResolutionStage.INIT);
    }

    private static class ClassInfo {
        private final Klass klass;
        private final Set<Field> visitedFields = new HashSet<>();
        private final Set<Method> visitedMethods = new HashSet<>();

        private ClassInfo(Klass klass) {
            this.klass = klass;
        }

    }

}
