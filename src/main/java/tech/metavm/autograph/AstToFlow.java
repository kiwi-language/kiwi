package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.FieldExpression;
import tech.metavm.expression.NodeExpression;
import tech.metavm.flow.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class AstToFlow extends JavaRecursiveElementVisitor {

    private final LinkedList<FlowBuilder> builders = new LinkedList<>();
    private final Map<String, ClassType> classes = new HashMap<>();
    private final LinkedList<ClassType> classStack = new LinkedList<>();
    private final TypeResolver typeResolver;

    public AstToFlow(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        ClassType klass = new ClassType(TranspileUtil.getBizClassName(psiClass));
        typeResolver.addType(psiClass.getQualifiedName(), klass);
        klass.setCode(psiClass.getName());
        enterClass(psiClass, klass);
        super.visitClass(psiClass);
        exitClass();
    }

    @Override
    public void visitField(PsiField psiField) {
        var field = new Field(
                TranspileUtil.getBizFieldName(psiField),
                currentClass(),
                resolveType(psiField.getType())
        );
        field.setCode(psiField.getName());
    }

    private void enterClass(PsiClass psiClass, ClassType klass) {
        classes.put(psiClass.getQualifiedName(), klass);
        classStack.push(klass);
    }

    private void exitClass() {
        classStack.pop();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        FlowBuilder builder = new FlowBuilder(method.getName(),
                getOutputType(method.getReturnType()), currentClass(), typeResolver);
        builders.push(builder);
        FlowRT flow = builder.getFlow();
        builder.enterScope(flow.getRootScope());
        processParameters(method.getParameterList());
        requireNonNull(method.getBody()).accept(this);
        builder.exitScope();
        builders.pop();
    }

    @SuppressWarnings("UnstableApiUsage")
    private ClassType getOutputType(PsiType type) {
        ClassType outputType = new ClassType("Output", true, true);
        if (type instanceof PsiPrimitiveType primitiveType
                && primitiveType.getKind() == JvmPrimitiveTypeKind.VOID) {
            return outputType;
        }
        Type valueType = resolveType(type);
        var valueField = new Field("value", outputType, valueType);
        valueField.setCode("value");
        return outputType;
    }

    private void processParameters(PsiParameterList parameterList) {
        var inputNode = builder().createInput();
        for (PsiParameter parameter : parameterList.getParameters()) {
            processParameter(parameter, inputNode);
        }
    }

    private ClassType currentClass() {
        return classStack.peek();
    }

    private void processParameter(PsiParameter parameter, InputNode inputNode) {
        var field = new Field(
                parameter.getName(),
                inputNode.getType(),
                resolveType(parameter.getType())
        );
        builder().setVariable(
                parameter.getName(),
                new FieldExpression(new NodeExpression(inputNode), field)
        );
    }

    private Type resolveType(PsiType type) {
        return typeResolver.resolve(type);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
//        super.visitIfStatement(statement);
        Expression cond = resolveExpression(requireNonNull(statement.getCondition()));
        BranchNode branchNode = builder().createBranch(false);
        Scope bodyScope = requireNonNull(statement.getUserData(Keys.BODY_SCOPE)),
                elseScope = requireNonNull(statement.getUserData(Keys.ELSE_SCOPE));
        Set<QualifiedName> modified = NncUtils.union(bodyScope.getModified(), elseScope.getModified());
        Set<QualifiedName> outputVars = getBlockOutputVariables(statement, modified);
        List<QualifiedName> outputVariables = new ArrayList<>();
        ClassType outputType = branchNode.getType();
        for (QualifiedName var : outputVars) {
            new Field(var.toString(), var.toString(), outputType, resolveType(var.type()));
            outputVariables.add(var);
        }
        enterCondSection(statement);
        newCondBranch(statement, branchNode.addBranch(new ExpressionValue(cond)), statement.getThenBranch());
        newCondBranch(statement, branchNode.addDefaultBranch(), statement.getElseBranch());
        Map<Field, BranchNodeOutputField> outputs = exitCondSection(statement, branchNode, outputVariables);
        outputs.forEach(branchNode::setOutput);
    }

    private void enterCondSection(PsiElement node) {
        builder().enterCondSection(node);
    }

    private void newCondBranch(PsiElement sectionId, Branch branch, @Nullable PsiStatement body) {
        builder().newCondBranch(sectionId, branch);
        builder().enterScope(branch.getScope());
        if (body != null) body.accept(this);
        builder().exitScope();
    }

    private Map<Field, BranchNodeOutputField> exitCondSection(PsiElement element, BranchNode branchNode,
                                                              List<QualifiedName> outputVariables) {
        Map<Field, BranchNodeOutputField> result = new HashMap<>();
        List<String> outputVars = NncUtils.map(outputVariables, Objects::toString);
        var condOutputs = builder().exitCondSection(element, outputVars);
        for (var qn : outputVariables) {
            var field = branchNode.getType().getFieldByCode(qn.toString());
            var outputField = new BranchNodeOutputField(field);
            result.put(field, outputField);
            for (var entry2 : condOutputs.entrySet()) {
                var branch = entry2.getKey();
                var branchOutputs = entry2.getValue();
                outputField.setValue(branch, branchOutputs.get(qn.toString()));

            }
            builder().setVariable(qn.toString(), new FieldExpression(new NodeExpression(branchNode), outputField.getField()));
        }
        return result;
    }

    private Set<QualifiedName> getBlockOutputVariables(PsiStatement statement, Set<QualifiedName> modified) {
        Set<QualifiedName> liveOut = requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        return NncUtils.filterUnique(modified, qn -> qn.isSimple() && liveOut.contains(qn));
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        var node = builder().createReturn();
        var flow = builder().getFlow();
        if (statement.getReturnValue() != null) {
            var valueField = flow.getOutputType().getFieldByCode("value");
            node.setField(valueField, resolveExpression(statement.getReturnValue()));
        }
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        resolveExpression(statement.getExpression());
    }

    private Expression resolveExpression(PsiExpression expression) {
        return builder().getExpressionResolver().resolve(expression);
    }

    private FlowBuilder builder() {
        return requireNonNull(builders.peek());
    }

    public Map<String, ClassType> getClasses() {
        return classes;
    }
}
