package tech.metavm.autograph;

import com.intellij.psi.PsiElement;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.flow.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class FlowGenerator {

    private final Flow flow;
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private final VariableTable variableTable = new VariableTable();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Map<String, Integer> name2count = new HashMap<>();
    private final TypeReducer typeReducer = new TypeReducer(this::getExpressionType);

    public FlowGenerator(Flow flow, TypeResolver typeResolver, IEntityContext entityContext) {
        this.flow = flow;
        this.typeResolver = typeResolver;
        expressionResolver = new ExpressionResolver(this, variableTable, typeResolver, entityContext);
    }

    Flow getFlow() {
        return flow;
    }

    BranchNode createBranch(boolean inclusive) {
        return new BranchNode(null, nextName("Branch"), inclusive, scope().getLastNode(), scope());
    }

    MergeNode createMerge() {
        scope().getLastNode();
        if ((scope().getLastNode() instanceof BranchNode branchNode)) {
            return new MergeNode(
                    null,
                    nextName("Merge"),
                    branchNode,
                    ClassBuilder.newBuilder("合并节点输出", "MergeNodeOutput").temporary().build(),
                    scope()
            );
        } else {
            throw new InternalException("MergeNode must directly follow a BranchNode");
        }
    }

    ValueNode createValue(String name, Expression expression) {
        return new ValueNode(
                null,
                nextName(name),
                scope().getLastNode(),
                scope(),
                new ExpressionValue(expression));
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

    void enterScope(ScopeRT scope, @Nullable Expression condition) {
        Map<Expression, Type> exprTypes = condition != null ? typeReducer.reduceType(condition) : Map.of();
        if(!scopes.isEmpty()) {
            exprTypes = typeReducer.mergeResults(exprTypes, scopes.peek().expressionTypes());
        }
        scopes.push(new ScopeInfo(scope, exprTypes));
    }

    void exitScope() {
        scopes.pop();
    }

    Type getExpressionType(Expression expression) {
        if(scopes.isEmpty()) {
            return expression.getType();
        }
        else {
            return scopes.peek().expressionTypes.getOrDefault(expression, expression.getType());
        }
    }

    private ScopeRT scope() {
        return Objects.requireNonNull(scopes.peek()).scope();
    }

    UpdateObjectNode createUpdateObject() {
        return new UpdateObjectNode(null, nextName("Update"), scope().getLastNode(), scope());
    }

    UpdateObjectNode createUpdate(Expression self, Map<Field, Expression> fields) {
        var node = createUpdateObject();
        node.setObjectId(self);
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(value)));
        return node;
    }

    UpdateStaticNode createUpdateStatic(ClassType klass, Map<Field, Expression> fields) {
        var node = new UpdateStaticNode(
                null, nextName("UpdateStatic"),
                scope().getLastNode(), scope(), klass
        );
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(expr)));
        return node;
    }

    AddObjectNode createAddObject(ClassType klass) {
        return new AddObjectNode(null, nextName("Add"), klass, scope().getLastNode(), scope());
    }

    @SuppressWarnings("UnusedReturnValue")
    ReturnNode createReturn() {
        return createReturn(null);
    }

    ReturnNode createReturn(Expression value) {
        var node = new ReturnNode(null, nextName("Exit"), scope().getLastNode(), scope());
        if (value != null) {
            node.setValue(new ExpressionValue(value));
        }
        return node;
    }

    ForEachNode createForEach(Expression array) {
        var loopType = newTemporaryType("ForeachOutput");
        newTemproryField(loopType, "array", array.getType());
        newTemproryField(loopType, "index", ModelDefRegistry.getType(Long.class));
        return new ForEachNode(
                null, nextName("Foreach"), loopType,
                scope().getLastNode(), scope(), new ExpressionValue(array),
                new ExpressionValue(ExpressionUtil.trueExpression())
        );
    }

    public Field newTemproryField(ClassType klass, String name, Type type) {
        return new Field(
                name,
                name,
                klass,
                type, Access.GLOBAL,
                false,
                false,
                InstanceUtils.nullInstance(),
                false,
                false,
                InstanceUtils.nullInstance()
        );
    }

    public ClassType newTemporaryType(String namePrefix) {
        String name = namePrefix + "_" + NncUtils.random();
        return ClassBuilder.newBuilder(name, name)
                .anonymous(true)
                .ephemeral(true)
                .build();
    }

    @SuppressWarnings("UnusedReturnValue")
    SubFlowNode createSubFlow(Expression self, Flow flow, Expression...arguments) {
        return createSubFlow(self, flow, List.of(arguments));
    }

    SubFlowNode createSubFlow(Expression self, Flow flow, List<Expression> arguments) {
        ClassType inputType = flow.getInputType();
        List<FieldParam> fieldParams = NncUtils.biMap(
                inputType.getFields(),
                arguments,
                (f, arg) -> new FieldParam(f, new ExpressionValue(arg))
        );
        return new SubFlowNode(null, nextName(flow.getName()), scope().getLastNode(), scope(),
                new ExpressionValue(self), fieldParams, flow);
    }

    NewNode createNew(Flow flow, List<Expression> arguments) {
        ClassType inputType = flow.getInputType();
        List<FieldParam> fieldParams = NncUtils.biMap(
                inputType.getFields(),
                arguments,
                (f, arg) -> new FieldParam(f, new ExpressionValue(arg))
        );
        return new NewNode(null, nextName(flow.getName()), flow, fieldParams, scope().getLastNode(), scope());
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        return new InputNode(null, "Input", getFlow().getInputType(), scope().getLastNode(), scope());
    }

    public SelfNode createSelf() {
        return new SelfNode(null, "Self", scope().getLastNode(), scope());
    }

    private String nextName(String prefix) {
        int cnt = name2count.compute(prefix, (k, c) -> c == null ? 1 : c + 1);
        return cnt > 1 ? prefix + "_" + (cnt - 1) : prefix;
    }

    private String randomName(String prefix) {
        return prefix + "_" + NncUtils.random();
    }

    @SuppressWarnings("UnusedReturnValue")
    public ExceptionNode createException(Expression message) {
        return new ExceptionNode(
                null,
                nextName("Error"),
                new ExpressionValue(message),
                scope().getLastNode(),
                scope()
        );
    }

    public WhileNode createWhile() {
        return createWhile(ExpressionUtil.trueExpression());
    }

    public WhileNode createWhile(Expression condition) {
        return new WhileNode(
                null, nextName("While"),
                newTemporaryType("WhileOutput"),
                scope().getLastNode(),
                scope(),
                new ExpressionValue(condition)
        );
    }

    private record ScopeInfo(
            ScopeRT scope,
            Map<Expression, Type> expressionTypes
    ) {

    }

}
