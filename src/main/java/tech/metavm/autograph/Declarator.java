package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.meta.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
        var metaClass = NncUtils.requireNonNull(psiClass.getUserData(Keys.META_CLASS));
        metaClass.setStage(ResolutionStage.DECLARATION);
        if(!metaClass.isInterface()) {
            FlowBuilder.newBuilder(metaClass, "实例初始化", "<init>", context.getFunctionTypeContext())
                    .build();
            FlowBuilder.newBuilder(metaClass, "类型初始化", "<cinit>", context.getFunctionTypeContext()).build();
        }
        boolean hashConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
        if(!hashConstructor) {
            String constructorName = metaClass.getEffectiveTemplate().getName();
            String constructorCode = metaClass.getEffectiveTemplate().getCode();
            FlowBuilder.newBuilder(metaClass, constructorName, constructorCode, context.getFunctionTypeContext())
                    .isConstructor(true)
                    .build();
        }
        classStack.push(metaClass);
        super.visitClass(psiClass);
        classStack.pop();
//        metaClass.setStage(ResolutionStage.DECLARATION);
    }

    private ClassType createEmptyType(String namePrefix) {
        return ClassBuilder.newBuilder(namePrefix, null).temporary().build();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        List<PsiMethod> overridenMethods = TranspileUtil.getOverriddenMethods(method);
        List<Flow> overriden = new ArrayList<>();
        for (PsiMethod overridenMethod : overridenMethods) {
            var overridenMethodCls = NncUtils.requireNonNull(overridenMethod.getContainingClass());
            var overridenMethodType = TranspileUtil.createTemplateType(overridenMethodCls);
            overriden.add(TranspileUtil.getFlowByMethod(
                    (ClassType) typeResolver.resolveDeclaration(overridenMethodType),
                    overridenMethod, typeResolver)
            );
        }
        List<Parameter> parameters = new ArrayList<>();
            if (method.isConstructor() && currentClass().isEnum()) {
                parameters.addAll(getEnumConstructorParams());
            }
            parameters.addAll(processParameters(method.getParameterList()));
        var flow = FlowBuilder.newBuilder(currentClass(), getFlowName(method), getFlowCode(method), context.getFunctionTypeContext())
                .isConstructor(method.isConstructor())
                .isAbstract(method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT))
                .parameters(parameters)
                .returnType(getOutputType(method))
                .overriden(overriden)
                .build();
        for (PsiTypeParameter typeParameter : method.getTypeParameters()) {
            typeResolver.resolveTypeVariable(typeParameter).setGenericDeclaration(flow);
        }
        method.putUserData(Keys.FLOW, flow);
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
        if(TranspileUtil.getAnnotation(psiField, Nullable.class) != null) {
            type = context.getNullableType(type);
        }
        var field = FieldBuilder
                .newBuilder(getBizFieldName(psiField), psiField.getName(), currentClass(), type)
                .unique(TranspileUtil.isUnique(psiField))
                .asTitle(TranspileUtil.isTitleField(psiField))
                .isChild(TranspileUtil.isChild(psiField))
                .isStatic(requireNonNull(psiField.getModifierList()).hasModifierProperty(PsiModifier.STATIC))
                .build();
        psiField.putUserData(Keys.FIELD, field);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var field = FieldBuilder
                .newBuilder(getEnumConstantName(enumConstant), enumConstant.getName(), currentClass(), currentClass())
                .isChild(true)
                .isStatic(true)
                .build();
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
