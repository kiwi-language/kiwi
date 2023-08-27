package tech.metavm.autograph;

import com.intellij.psi.PsiElement;
import tech.metavm.expression.Expression;
import tech.metavm.flow.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlowBuilder {

    private final FlowRT flow;
    private final LinkedList<ScopeRT> scopes = new LinkedList<>();
    private final VariableTable variableTable = new VariableTable();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;

    public FlowBuilder(String name, String code, ClassType outputType, ClassType declaringClass, TypeResolver typeResolver) {
        ClassType inputType = new ClassType(name + "Input");
        flow = new FlowRT(name, inputType, outputType, declaringClass);
        flow.setCode(code);
        this.typeResolver = typeResolver;
        expressionResolver = new ExpressionResolver(this, variableTable, typeResolver);
    }

    FlowRT getFlow() {
        return flow;
    }

    BranchNode createBranch(boolean inclusive) {
        return new BranchNode("Branch", inclusive, scope().getLastNode(), scope());
    }

    void enterCondSection(PsiElement sectionId) {
        variableTable.enterCondSection(sectionId);
    }

    void newCondBranch(PsiElement sectionId, Branch branch) {
        variableTable.newCondBranch(sectionId, branch);
    }

    Map<Branch, Map<String, Expression>> exitCondSection(PsiElement sectionId, List<String> outputVars) {
        return variableTable.exitCondSection(sectionId, outputVars);
    }

    void setVariable(String name, Expression value) {
        variableTable.set(name, value);
    }

    Expression getVariable(String name) {
        return variableTable.get(name);
    }

    void enterScope(ScopeRT scope) {
        scopes.push(scope);
    }

    void exitScope() {
        scopes.pop();
    }

    private ScopeRT scope() {
        return Objects.requireNonNull(scopes.peek());
    }

    UpdateObjectNode createUpdateObject() {
        return new UpdateObjectNode("Update", scope().getLastNode(), scope());
    }

    AddObjectNode createAddObject(ClassType klass) {
        return new AddObjectNode("Add Object", klass, scope().getLastNode(), scope());
    }

    ReturnNode createReturn() {
        return new ReturnNode("Exit", scope().getLastNode(), scope());
    }

    SubFlowNode createSubFlow(Expression self, FlowRT flow, List<Expression> arguments) {
        ClassType inputType = flow.getInputType();
        List<FieldParam> fieldParams = NncUtils.biMap(
                inputType.getFields(),
                arguments,
                (f, arg) -> new FieldParam(f, new ExpressionValue(arg))
        );
        return new SubFlowNode("Subflow", scope().getLastNode(), scope(), self, fieldParams, flow);
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        return new InputNode("Input", getFlow().getInputType(), scope().getLastNode(), scope());
    }

    public SelfNode createSelf() {
        return new SelfNode("Self", scope().getLastNode(), scope());
    }
}
