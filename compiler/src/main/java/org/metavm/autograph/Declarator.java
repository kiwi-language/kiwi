package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.Bean;
import org.metavm.api.Component;
import org.metavm.api.Configuration;
import org.metavm.api.Resource;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
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

public class Declarator extends VisitorBase {

    public static final Logger logger = LoggerFactory.getLogger(Declarator.class);

    private final PsiClass psiClass;

    private final TypeResolver typeResolver;

    private final IEntityContext context;

    private final LinkedList<ClassInfo> classStack = new LinkedList<>();

    private @Nullable Index currentIndex;

    public Declarator(PsiClass psiClass, TypeResolver typeResolver, IEntityContext context) {
        this.psiClass = psiClass;
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
        if (psiClass != this.psiClass)
            return;
        var klass = typeResolver.getKlass(psiClass);
        var parent = TranspileUtils.getProperParent(psiClass, Set.of(PsiMethod.class, PsiClass.class));
        if(parent instanceof PsiMethod parentMethod)
            requireNonNull(parent.getUserData(Keys.Method),
                    () -> "Cannot find parent method for class: " + psiClass.getName() + " inside " + TranspileUtils.getQualifiedName(parentMethod) + " in file " + psiClass.getContainingFile().getName())
                    .addLocalKlass(klass);
        klass.setKlasses(NncUtils.map(psiClass.getInnerClasses(),
                k -> Objects.requireNonNull(k.getUserData(Keys.MV_CLASS), () -> "Cannot find metavm class for class " + k.getQualifiedName())));
        if (psiClass.getSuperClass() != null &&
                !Objects.equals(psiClass.getSuperClass().getQualifiedName(), Object.class.getName())) {
            klass.setSuperType(((ClassType) typeResolver.resolveTypeOnly(TranspileUtils.getSuperClassType(psiClass))));
        }
        else if(!klass.isEnum())
            klass.setSuperType(null);
        klass.setInterfaces(
                NncUtils.map(
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
        if(klass.isEnum())
            classInfo.visitedMethods.add(Flows.saveValuesMethod(klass));
        var removedFields = NncUtils.exclude(klass.getFields(), classInfo.visitedFields::contains);
        removedFields.forEach(f -> {
            f.setInitializer(null);
            f.setMetadataRemoved();
            f.resetTypeIndex();
        });
        var removedStaticFields = NncUtils.exclude(klass.getStaticFields(), classInfo.visitedFields::contains);
        removedStaticFields.forEach(klass::removeField);
        var fieldIndices = new HashMap<String, Integer>();
        for (int i = 0; i < psiClass.getFields().length; i++) {
            fieldIndices.put(psiClass.getFields()[i].getName(), i);
        }
        klass.sortFields(Comparator.comparingInt(f -> fieldIndices.getOrDefault(f.getName(), Integer.MAX_VALUE)));
        var methodIndices = new HashMap<Method, Integer>();
        for (int i = 0; i < psiClass.getMethods().length; i++) {
            var method = psiClass.getMethods()[i].getUserData(Keys.Method);
            if (method != null)
                methodIndices.put(method, i);
        }
        klass.sortMethods(Comparator.comparingInt(m -> methodIndices.getOrDefault(m, -1)));
        NncUtils.exclude(klass.getMethods(), classInfo.visitedMethods::contains).forEach(klass::removeMethod);
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
        return NamingUtils.firstCharToLowerCase(klass.getName());
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (CompilerConfig.isMethodBlacklisted(method))
            return;
        var klass = currentClass().klass;
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
            flow = MethodBuilder.newBuilder(klass, getFlowName(method))
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
        flow.setTypeParameters(NncUtils.map(
                method.getTypeParameters(),
                t -> typeResolver.resolveTypeVariable(t).getVariable()
        ));
        List<Parameter> parameters = new ArrayList<>();
        if (method.isConstructor() && klass.isEnum())
            parameters.addAll(getEnumConstructorParams(flow));
        parameters.addAll(processParameters(method.getParameterList(), flow));
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
            nameParam = new Parameter(null, "name", Types.getStringType(), method);
        else
            nameParam.setType(Types.getStringType()); // Adding type constant into constant pool
        var ordinalParam = method.findParameter(p -> "__ordinal__".equals(p.getName()));
        if(ordinalParam == null)
            ordinalParam = new Parameter(null, "ordinal", Types.getIntType(), method);
        else
            ordinalParam.setType(Types.getIntType());
        return List.of(nameParam, ordinalParam);
    }

    private List<Parameter> processParameters(PsiParameterList parameterList, Method method) {
        return NncUtils.map(
                parameterList.getParameters(),
                param -> {
                    var p = method.findParameter(p1 -> param.getName().equals(p1.getName()));
                    var name = param.getName();
                    var type = resolveParameterType(param);
                    if(p == null)
                        p = new Parameter(null, name, type, method);
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
        return resolveNullableType(parameter.getType());
    }

    @Override
    public void visitField(PsiField psiField) {
        var type = resolveNullableType(psiField.getType());
        var klass = currentClass().klass;
        var isStatic = TranspileUtils.isStatic(psiField);
        var fieldTag = (int) TranspileUtils.getFieldAnnotationAttribute(psiField, "tag", -1);
        Field field;
        if(fieldTag == -1) {
            field = isStatic ? klass.findSelfStaticFieldByName(psiField.getName())
                    :klass.findSelfFieldByName(psiField.getName());
        }
        else {
            field = isStatic ? klass.findSelfStaticField(f -> Objects.equals(f.getSourceTag(), fieldTag))
                    : klass.findSelfField(f -> Objects.equals(f.getSourceTag(), fieldTag));
        }
        var modList = requireNonNull(psiField.getModifierList());
        var isTransient = modList.hasModifierProperty(PsiModifier.TRANSIENT);
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(psiField.getName(), klass, type)
                    .access(getAccess(psiField))
                    .unique(TranspileUtils.isUnique(psiField))
                    .isChild(TranspileUtils.isChild(psiField))
                    .isStatic(modList.hasModifierProperty(PsiModifier.STATIC))
                    .isTransient(isTransient)
                    .sourceTag(fieldTag != -1 ? fieldTag : null)
                    .build();
        } else {
            field.setName(getBizFieldName(psiField));
            field.setType(type);
            field.setAccess(getAccess(psiField));
            field.setUnique(TranspileUtils.isUnique(psiField));
            field.setChild(TranspileUtils.isChild(psiField));
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
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var classInfo = currentClass();
        var klass = classInfo.klass;
        var field = klass.findSelfStaticFieldByName(enumConstant.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(enumConstant.getName(), klass, klass.getType())
                    .isChild(true)
                    .isStatic(true)
                    .build();
        } else {
            field.setName(getEnumConstantName(enumConstant));
            field.resetTypeIndex();
        }
        classInfo.visitedFields.add(field);
        var ecd = klass.findEnumConstantDef(e -> e.getName().equals(enumConstant.getName()));
        if(ecd == null) {
            var name = enumConstant.getName();
            var initializer = MethodBuilder.newBuilder(klass, "$" + name)
                    .isStatic(true)
                    .returnType(klass.getType())
                    .build();
            ecd = new EnumConstantDef(klass, name, classInfo.nextEnumConstantOrdinal(), initializer);
        } else
            ecd.setOrdinal(classInfo.nextEnumConstantOrdinal());
        classInfo.visitedMethods.add(ecd.getInitializer());
        enumConstant.putUserData(Keys.FIELD, field);
        enumConstant.putUserData(Keys.ENUM_CONSTANT_DEF, ecd);
    }

    private ClassInfo currentClass() {
        return NncUtils.requireNonNull(classStack.peek());
    }

    private Type getReturnType(PsiMethod method) {
        if(method.isConstructor())
            return resolveType(TranspileUtils.createTemplateType(requireNonNull(method.getContainingClass())));
        else
            return resolveNullableType(method.getReturnType());
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType);
    }

    private Type resolveNullableType(PsiType psiType) {
        return typeResolver.resolveNullable(psiType, ResolutionStage.INIT);
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
