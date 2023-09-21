package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.ClassBuilder;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.FieldBuilder;
import tech.metavm.object.meta.Type;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.*;
import static tech.metavm.entity.ModelDefRegistry.getType;

public class Declarator extends JavaRecursiveElementVisitor {

    private final TypeResolver typeResolver;

    private final IEntityContext context;

    private final LinkedList<ClassType> classStack = new LinkedList<>();

    public Declarator(TypeResolver typeResolver, IEntityContext context) {
        this.typeResolver = typeResolver;
        this.context = context;
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        var metaClass = psiClass.getUserData(Keys.META_CLASS);
        psiClass.putUserData(Keys.RESOLVE_STAGE, 1);
        classStack.push(metaClass);
        super.visitClass(psiClass);
        classStack.pop();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        PsiMethod overridenMethod = TranspileUtil.getOverriddenMethod(method);
        Flow overridenFlow;
        if (overridenMethod != null) {
            var overridenMethodCls = NncUtils.requireNonNull(overridenMethod.getContainingClass());
            var overridenMethodType = TranspileUtil.createTemplateType(overridenMethodCls);
            overridenFlow = TranspileUtil.getFlowByMethod(
                    (ClassType) typeResolver.resolveDeclaration(overridenMethodType, context),
                    overridenMethod, typeResolver, context
            );
        } else {
            overridenFlow = null;
        }
        ClassType inputType;
        if (overridenFlow != null) {
            inputType = null;
        } else {
            inputType = ClassBuilder
                    .newBuilder(getFlowName(method) + "输入", method.getName() + "Input")
                    .temporary().build();
            if (method.isConstructor() && currentClass().isEnum()) {
                addEnumConstructorParams(inputType);
            }
            processParameters(method.getParameterList(), inputType);
        }
        var flow = new Flow(null,
                currentClass(),
                getFlowName(method),
                method.getName(),
                method.isConstructor(),
                method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT),
                false,
                inputType,
                getOutputType(method, overridenFlow),
                overridenFlow
        );
        method.putUserData(Keys.FLOW, flow);
    }

    private void addEnumConstructorParams(ClassType inputType) {
        FieldBuilder.newBuilder("名称", "name", inputType, getType(String.class)).build();
        FieldBuilder.newBuilder("序号", "ordinal", inputType, getType(long.class)).build();
    }

    private void processParameters(PsiParameterList parameterList, ClassType inputType) {
        for (PsiParameter parameter : parameterList.getParameters()) {
            processParameter(parameter, inputType);
        }
    }

    private void processParameter(PsiParameter parameter, ClassType inputType) {
        FieldBuilder
                .newBuilder(getFlowParamName(parameter), parameter.getName(),
                        inputType, resolveType(parameter.getType()))
                .build();
    }

    @Override
    public void visitField(PsiField psiField) {
        var field = FieldBuilder
                .newBuilder(getBizFieldName(psiField), psiField.getName(), currentClass(), resolveType(psiField.getType()))
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

    //    @SuppressWarnings("UnstableApiUsage")
    private Type getOutputType(PsiMethod method, @Nullable Flow overridenFlow) {
//        ClassType outputType = ClassBuilder
//                .newBuilder(getFlowName(method) + "输出", method.getName() + "Output")
//                .temporary()
//                .interfaces(overridenFlow != null ? List.of(overridenFlow.getOutputType()) : List.of())
//                .build();
        var type = method.isConstructor() ?
                TranspileUtil.getPsiElementFactory().createType(requireNonNull(method.getContainingClass())) :
                method.getReturnType();
//        if (type instanceof PsiPrimitiveType primitiveType
//                && primitiveType.getKind() == JvmPrimitiveTypeKind.VOID) {
//            return outputType;
//        }
        /*Type valueType =*/
        return resolveType(type);
//        FieldBuilder.newBuilder("值", "value", outputType, valueType).build();
//        return outputType;
    }

    private Type resolveType(PsiType psiType) {
        return typeResolver.resolveTypeOnly(psiType, context);
    }

}
