package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.Method;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Values;
import tech.metavm.object.type.*;
import tech.metavm.util.CompilerConfig;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.*;

public class Declarator extends CodeGenVisitor {

    private final TypeResolver typeResolver;

    private final IEntityContext context;

    private final LinkedList<ClassType> classStack = new LinkedList<>();

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
        if (TranspileUtil.getAnnotation(psiClass, EntityIndex.class) != null) {
            Index index = NncUtils.find(currentClass().getIndices(), idx -> Objects.equals(idx.getCode(), psiClass.getName()));
            if (index == null) {
                index = new Index(
                        currentClass(),
                        TranspileUtil.getIndexName(psiClass),
                        psiClass.getName(),
                        "",
                        TranspileUtil.isUniqueIndex(psiClass)
                );
            } else {
                index.setName(TranspileUtil.getIndexName(psiClass));
            }
            currentIndex = index;
            psiClass.putUserData(Keys.INDEX, index);
            super.visitClass(psiClass);
            return;
        }
        visitedFields.clear();
        visitedMethods.clear();
        var metaClass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        metaClass.setStage(ResolutionStage.DECLARATION);
        if (!metaClass.isInterface()) {
            if (metaClass.findSelfMethodByCode("<init>") == null) {
                MethodBuilder.newBuilder(metaClass, "实例初始化", "<init>", context.getFunctionTypeContext())
                        .access(Access.PRIVATE)
                        .build();
            }
            if (metaClass.findSelfMethodByCode("<cinit>") == null) {
                MethodBuilder.newBuilder(metaClass, "类型初始化", "<cinit>", context.getFunctionTypeContext())
                        .isStatic(true)
                        .access(Access.PRIVATE)
                        .build();
            }
            var initMethod = Objects.requireNonNull(metaClass.findSelfMethodByCode("<init>"));
            var cinitMethod = Objects.requireNonNull(metaClass.findSelfMethodByCode("<cinit>"));
            initMethod.clearNodes();
            cinitMethod.clearNodes();
            visitedMethods.add(initMethod);
            visitedMethods.add(cinitMethod);
        }
        classStack.push(metaClass);
        super.visitClass(psiClass);
        classStack.pop();
        var removedFields = NncUtils.exclude(metaClass.getFields(), visitedFields::contains);
        removedFields.forEach(metaClass::removeField);
        var removedMethods = NncUtils.filter(metaClass.getMethods(),
                m -> !visitedMethods.contains(m) && !m.isSynthetic());
        removedMethods.forEach(metaClass::removeMethod);
//        metaClass.setStage(ResolutionStage.DECLARATION);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (CompilerConfig.isMethodBlacklisted(method))
            return;
        var psiClass = requireNonNull(method.getContainingClass());
        if(TranspileUtil.getAnnotation(psiClass, EntityIndex.class) != null)
            return;
        List<PsiMethod> overriddenMethods = TranspileUtil.getOverriddenMethods(method);
        List<Method> overridden = new ArrayList<>();
        for (PsiMethod overriddenMethod : overriddenMethods) {
            var overriddenMethodCls = NncUtils.requireNonNull(overriddenMethod.getContainingClass());
            if (Object.class.getName().equals(overriddenMethodCls.getQualifiedName()))
                continue;
            var overriddenMethodType = TranspileUtil.createTemplateType(overriddenMethodCls);
            overridden.add(TranspileUtil.getMethidByJavaMethod(
                    (ClassType) typeResolver.resolveDeclaration(overriddenMethodType),
                    overriddenMethod, typeResolver)
            );
        }
        var flow = NncUtils.find(currentClass().getMethods(),
                f -> f.getInternalName(null).equals(TranspileUtil.getInternalName(method)));
        if (flow != null)
            method.putUserData(Keys.Method, flow);
        List<Parameter> resolvedParams = new ArrayList<>();
        if (method.isConstructor() && currentClass().isEnum())
            resolvedParams.addAll(getEnumConstructorParams());
        resolvedParams.addAll(processParameters(method.getParameterList()));
        if (flow == null) {
            flow = MethodBuilder.newBuilder(currentClass(), getFlowName(method), getFlowCode(method), context.getFunctionTypeContext())
                    .isConstructor(method.isConstructor())
                    .isStatic(method.getModifierList().hasModifierProperty(PsiModifier.STATIC))
                    .access(resolveAccess(method.getModifierList()))
                    .isAbstract(method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT))
                    .parameters(resolvedParams)
                    .returnType(getReturnType(method))
                    .overridden(overridden)
                    .build();
            method.putUserData(Keys.Method, flow);
        } else {
            flow.setName(getFlowName(method));
            NncUtils.biForEach(
                    flow.getParameters(), resolvedParams,
                    (param, resolvedParam) -> param.setName(resolvedParam.getName())
            );
            flow.update(
                    flow.getParameters(),
                    getReturnType(method),
                    overridden,
                    context.getFunctionTypeContext()
            );
        }
        visitedMethods.add(flow);
        for (PsiTypeParameter typeParameter : method.getTypeParameters()) {
            var typeVar = typeResolver.resolveTypeVariable(typeParameter);
            if (typeVar.getGenericDeclaration() != flow)
                typeVar.setGenericDeclaration(flow);
        }
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
                new Parameter(null, "名称", "name", StandardTypes.getStringType()),
                new Parameter(null, "序号", "ordinal", StandardTypes.getLongType())
        );
    }

    private List<Parameter> processParameters(PsiParameterList parameterList) {
        return NncUtils.map(
                parameterList.getParameters(),
                param -> new Parameter(null, getFlowParamName(param), param.getName(), resolveParameterType(param))
        );
    }

    private Type resolveParameterType(PsiParameter parameter) {
        var type = resolveType(parameter.getType());
        if (TranspileUtil.getAnnotation(parameter, Nullable.class) != null)
            type = context.getNullableType(type);
        return type;
    }


    @Override
    public void visitField(PsiField psiField) {
        var psiClass = requireNonNull(((PsiMember) psiField).getContainingClass());
        if (TranspileUtil.getAnnotation(psiClass, EntityIndex.class) != null) {
            var index = requireNonNull(currentIndex);
            var indexField = NncUtils.find(index.getFields(), f -> Objects.equals(f.getName(), psiField.getName()));
            if (indexField == null)
                new IndexField(index, getBizFieldName(psiField), psiField.getName(), Values.nullValue());
            return;
        }
        var type = resolveType(psiField.getType());
        if (TranspileUtil.getAnnotation(psiField, Nullable.class) != null)
            type = context.getNullableType(type);
        var klass = currentClass();
        var field = TranspileUtil.isStatic(psiField) ?
                klass.findSelfStaticFieldByCode(psiField.getName())
                : klass.findSelfFieldByCode(psiField.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getBizFieldName(psiField), psiField.getName(), currentClass(), type)
                    .access(getAccess(psiField))
                    .unique(TranspileUtil.isUnique(psiField))
                    .isChild(TranspileUtil.isChild(psiField))
                    .isStatic(requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC))
                    .build();
        } else {
            field.setName(getBizFieldName(psiField));
            field.setType(type);
            field.setAccess(getAccess(psiField));
            field.setUnique(TranspileUtil.isUnique(psiField));
        }
        visitedFields.add(field);
        if (TranspileUtil.isTitleField(psiField))
            currentClass().setTitleField(field);
        psiField.putUserData(Keys.FIELD, field);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var field = currentClass().findSelfStaticFieldByCode(enumConstant.getName());
        if (field == null) {
            field = FieldBuilder
                    .newBuilder(getEnumConstantName(enumConstant), enumConstant.getName(), currentClass(), currentClass())
                    .isChild(true)
                    .isStatic(true)
                    .build();
        } else
            field.setName(getEnumConstantName(enumConstant));
        enumConstant.putUserData(Keys.FIELD, field);
    }

    private ClassType currentClass() {
        return NncUtils.requireNonNull(classStack.peek());
    }

    private Type getReturnType(PsiMethod method) {
        var type = method.isConstructor() ?
                TranspileUtil.createTemplateType(requireNonNull(method.getContainingClass())) :
                method.getReturnType();
        var metaType = resolveType(type);
        if (TranspileUtil.getAnnotation(method, Nullable.class) != null)
            metaType = context.getNullableType(metaType);
        return metaType;
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType);
    }

}
