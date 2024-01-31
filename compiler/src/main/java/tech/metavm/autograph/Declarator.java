package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.Method;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.*;

public class Declarator extends JavaRecursiveElementVisitor {

    private final TypeResolver typeResolver;

    private final IEntityContext context;

    private final LinkedList<ClassType> classStack = new LinkedList<>();

    public Declarator(TypeResolver typeResolver, IEntityContext context) {
        this.typeResolver = typeResolver;
        this.context = context;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        var metaClass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        metaClass.setStage(ResolutionStage.DECLARATION);
        if (!metaClass.isInterface()) {
            MethodBuilder.newBuilder(metaClass, "实例初始化", "<init>", context.getFunctionTypeContext())
                    .access(Access.PRIVATE)
                    .build();
            MethodBuilder.newBuilder(metaClass, "类型初始化", "<cinit>", context.getFunctionTypeContext())
                    .isStatic(true)
                    .access(Access.PRIVATE)
                    .build();
        }
        boolean hashConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
        if (!metaClass.isInterface() && !hashConstructor) {
            String constructorName = metaClass.getEffectiveTemplate().getName();
            String constructorCode = metaClass.getEffectiveTemplate().getCode();
            MethodBuilder.newBuilder(metaClass, constructorName, constructorCode, context.getFunctionTypeContext())
                    .isConstructor(true)
                    .build();
        }
        classStack.push(metaClass);
        super.visitClass(psiClass);
        classStack.pop();
//        metaClass.setStage(ResolutionStage.DECLARATION);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        List<PsiMethod> overriddenMethods = TranspileUtil.getOverriddenMethods(method);
        List<Method> overridden = new ArrayList<>();
        for (PsiMethod overriddenMethod : overriddenMethods) {
            var overriddenMethodCls = NncUtils.requireNonNull(overriddenMethod.getContainingClass());
            var overriddenMethodType = TranspileUtil.createTemplateType(overriddenMethodCls);
            overridden.add(TranspileUtil.getMethidByJavaMethod(
                    (ClassType) typeResolver.resolveDeclaration(overriddenMethodType),
                    overriddenMethod, typeResolver)
            );
        }
        List<Parameter> resolvedParams = new ArrayList<>();
        if (method.isConstructor() && currentClass().isEnum())
            resolvedParams.addAll(getEnumConstructorParams());
        resolvedParams.addAll(processParameters(method.getParameterList()));
        var paramTypes = NncUtils.map(resolvedParams, Parameter::getType);
        var flow = currentClass().findSelfMethod(method.getName(), paramTypes);
        if (flow == null) {
            flow = MethodBuilder.newBuilder(currentClass(), getFlowName(method), getFlowCode(method), context.getFunctionTypeContext())
                    .isConstructor(method.isConstructor())
                    .isStatic(method.getModifierList().hasModifierProperty(PsiModifier.STATIC))
                    .access(resolveAccess(method.getModifierList()))
                    .isAbstract(method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT))
                    .parameters(resolvedParams)
                    .returnType(getOutputType(method))
                    .overridden(overridden)
                    .build();
        } else {
            flow.setName(getFlowName(method));
            NncUtils.biForEach(
                    flow.getParameters(), resolvedParams,
                    (param, resolvedParam) -> param.setName(resolvedParam.getName())
            );
            flow.update(
                    flow.getParameters(),
                    getOutputType(method),
                    overridden,
                    context.getFunctionTypeContext()
            );
        }
        for (PsiTypeParameter typeParameter : method.getTypeParameters()) {
            typeResolver.resolveTypeVariable(typeParameter).setGenericDeclaration(flow);
        }
        method.putUserData(Keys.Method, flow);
    }

    private Access resolveAccess(PsiModifierList modifierList) {
        if(modifierList.hasModifierProperty(PsiModifier.PUBLIC))
            return Access.PUBLIC;
        if(modifierList.hasModifierProperty(PsiModifier.PRIVATE))
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
                param -> new Parameter(null, getFlowParamName(param), param.getName(), resolveType(param.getType()))
        );
    }

    @Override
    public void visitField(PsiField psiField) {
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
        if(TranspileUtil.isTitleField(psiField))
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

    private Type getOutputType(PsiMethod method) {
        var type = method.isConstructor() ?
                TranspileUtil.createTemplateType(requireNonNull(method.getContainingClass())) :
                method.getReturnType();
        return resolveType(type);
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType);
    }

}
