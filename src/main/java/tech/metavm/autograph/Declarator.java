package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

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
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        var metaClass = NncUtils.requireNonNull(psiClass.getUserData(Keys.META_CLASS));
        psiClass.putUserData(Keys.RESOLVE_STAGE, 1);
        if(!metaClass.isInterface()) {
            FlowBuilder.newBuilder(metaClass, "实例初始化", "<init>")
                    .isConstructor(true)
                    .inputType(createEmptyType("init_input"))
                    .build();
            FlowBuilder.newBuilder(metaClass, "类型初始化", "<cinit>")
                    .inputType(createEmptyType("cinit_input")).build();
        }
        classStack.push(metaClass);
        super.visitClass(psiClass);
        classStack.pop();
        metaClass.stage = ResolutionStage.DECLARED;
    }

    private ClassType createEmptyType(String namePrefix) {
        return ClassBuilder.newBuilder(namePrefix, null).temporary().build();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        PsiMethod overridenMethod = TranspileUtil.getOverriddenMethod(method);
        Flow overridenFlow;
        if (overridenMethod != null) {
            var overridenMethodCls = NncUtils.requireNonNull(overridenMethod.getContainingClass());
            var overridenMethodType = TranspileUtil.createTemplateType(overridenMethodCls);
            overridenFlow = TranspileUtil.getFlowByMethod(
                    (ClassType) typeResolver.resolveDeclaration(overridenMethodType),
                    overridenMethod, typeResolver
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
        var flow = FlowBuilder.newBuilder(currentClass(), getFlowName(method), method.getName())
                .isConstructor(method.isConstructor())
                .isAbstract(method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT))
                .inputType(inputType)
                .outputType(getOutputType(method))
                .overriden(overridenFlow)
                .build();
        for (PsiTypeParameter typeParameter : method.getTypeParameters()) {
            typeResolver.resolveTypeVariable(typeParameter).setGenericDeclaration(flow);
        }
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
