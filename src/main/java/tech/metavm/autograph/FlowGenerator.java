package tech.metavm.autograph;

import com.intellij.psi.PsiMethod;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.flow.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class FlowGenerator {

    private final Flow flow;
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private final VariableTable variableTable = new VariableTable();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Map<String, Integer> name2count = new HashMap<>();
    private final TypeNarrower typeNarrower = new TypeNarrower(this::getExpressionType);
    private Expression yieldValue;
    private final Map<BranchNode, LinkedList<ScopeInfo>> condScopes = new IdentityHashMap<>();
    private final Map<String, Integer> varNames = new HashMap<>();
    private final IEntityContext entityContext;

    public FlowGenerator(Flow flow, TypeResolver typeResolver, IEntityContext entityContext, Generator visitor) {
        this.flow = flow;
        this.typeResolver = typeResolver;
        this.entityContext = entityContext;
        expressionResolver = new ExpressionResolver(this, variableTable, typeResolver, entityContext, visitor);
    }

    Flow getFlow() {
        return flow;
    }

    BranchNode createBranchNode(boolean inclusive) {
        return setNodeExprTypes(new BranchNode(null, nextName("Branch"), inclusive, scope().getLastNode(), scope()));
    }

    MergeNode createMerge() {
        scope().getLastNode();
        if ((scope().getLastNode() instanceof BranchNode branchNode)) {
            var mergeNode = setNodeExprTypes(new MergeNode(
                    null,
                    nextName("Merge"),
                    branchNode,
                    ClassBuilder.newBuilder("合并节点输出", "MergeNodeOutput").temporary().build(),
                    scope()
            ));
            mergeNode.mergeExpressionTypes(MergeNode.getExpressionTypeMap(branchNode));
            return mergeNode;
        } else {
            throw new InternalException("MergeNode must directly follow a BranchNode");
        }
    }

    TryNode createTry() {
        return new TryNode(null, nextName("Try"), scope().getLastNode(), scope());
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k,v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryEndNode createTryEnd() {
        var node = new TryEndNode(
                null, nextName("TryEnd"),
                ClassBuilder.newBuilder("TryEndOutput", "TryEndOutput")
                        .temporary().build(),
                (TryNode) scope().getLastNode(),
                scope()
        );
        FieldBuilder.newBuilder("异常", "exception", node.getType(),
                StandardTypes.getNullableThrowableType())
                .build();
        return node;
    }


    ValueNode createValue(String name, Expression expression) {
        return setNodeExprTypes(new ValueNode(
                        null,
                        nextName(name),
                        getExpressionType(expression),
                        scope().getLastNode(),
                        scope(),
                        new ExpressionValue(expression)
                )
        );
    }

    NewArrayNode createNewArray(ArrayType type, List<Expression> elements) {
        return setNodeExprTypes(new NewArrayNode(
                null, nextName("NewArray"),
                type, NncUtils.map(elements, ExpressionValue::new),
                scope().getLastNode(), scope()
        ));
    }

    void enterTrySection(TryNode tryNode) {
        variableTable.enterTrySection(tryNode);
        enterScope(tryNode.getBodyScope());
    }

    Map<NodeRT<?>, Map<String, Expression>> exitTrySection(TryNode tryNode, List<String> outputVars) {
        exitScope();
        return variableTable.exitTrySection(tryNode, outputVars);
    }

    void enterCondSection(BranchNode sectionId) {
        variableTable.enterCondSection(sectionId);
        condScopes.put(sectionId, new LinkedList<>());
    }

    void enterBranch(Branch branch) {
        var exprTypeMap = variableTable.nextBranch(branch.getOwner(), branch);
        var scopeInfo = enterScope(branch.getScope(), exprTypeMap);
        condScopes.get(branch.getOwner()).add(scopeInfo);
    }

    void exitBranch() {
        exitScope();
    }

    Map<Branch, Map<String, Expression>> exitCondSection(MergeNode mergeNode, List<String> outputVars) {
        var branchNode = mergeNode.getBranchNode();
        yieldValue = null;
        ExpressionTypeMap exprTypes = null;
        for (ScopeInfo scope : condScopes.remove(branchNode)) {
            var lastNode = scope.scope.getLastNode();
            if (lastNode == null || !lastNode.isExit()) {
                var newExprTypes = lastNode == null ? scope.scope.getExpressionTypes() : lastNode.getExpressionTypes();
                if (exprTypes == null) {
                    exprTypes = newExprTypes;
                } else {
                    exprTypes = exprTypes.union(newExprTypes);
                }
            }
        }
        if (exprTypes != null) {
            mergeNode.mergeExpressionTypes(exprTypes);
        }
        return variableTable.exitCondSection(branchNode, outputVars);
    }

    private ScopeInfo currentScope() {
        return NncUtils.requireNonNull(scopes.peek());
    }

    void setVariable(String name, Expression value) {
        variableTable.set(name, value);
    }

    Expression getVariable(String name) {
        return variableTable.get(name);
    }

    ScopeInfo enterScope(ScopeRT scope) {
        return enterScope(scope, NncUtils.get(scope.getOwner(), NodeRT::getExpressionTypes));
    }

    ScopeInfo enterScope(ScopeRT scope, ExpressionTypeMap expressionTypeMap) {
        if(expressionTypeMap != null) {
            scope.setExpressionTypes(expressionTypeMap);
        }
        var scopeInfo = new ScopeInfo(scope);
        scopes.push(scopeInfo);
        return scopeInfo;
    }

    ScopeInfo exitScope() {
        return scopes.pop();
    }

    Expression getYield() {
        return yieldValue;
    }

    Type getExpressionType(Expression expression) {
        if (scopes.isEmpty()) {
            return expression.getType();
        } else {
            var lastNode = scope().getLastNode();
            if (lastNode == null) {
                return scope().getExpressionTypes().getType(expression);
            } else {
                return lastNode.getExpressionTypes().getType(expression);
            }
        }
    }

    CheckNode createCheck(Expression condition, BranchNode exit) {
        var checkNode = setNodeExprTypes(
                new CheckNode(null, nextName("Check"), scope().getLastNode(), scope(),
                        new ExpressionValue(condition), exit)
                );
        var narrower = new TypeNarrower(checkNode.getExpressionTypes()::getType);
        var branchEntry = checkNode.getExpressionTypes().merge(narrower.narrowType(ExpressionUtil.not(condition)));
        checkNode.mergeExpressionTypes(narrower.narrowType(condition));
        variableTable.addBranchEntry(branchEntry, exit);
        return checkNode;
    }

    private ScopeRT scope() {
        return currentScope().scope;
    }

    UpdateObjectNode createUpdateObject() {
        return setNodeExprTypes(new UpdateObjectNode(null, nextName("Update"), scope().getLastNode(), scope()));
    }

    UpdateObjectNode createUpdate(Expression self, Map<Field, Expression> fields) {
        var node = createUpdateObject();
        node.setObjectId(new ExpressionValue(self));
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(value)));
        return setNodeExprTypes(node);
    }

    UpdateStaticNode createUpdateStatic(ClassType klass, Map<Field, Expression> fields) {
        var node = new UpdateStaticNode(
                null, nextName("UpdateStatic"),
                scope().getLastNode(), scope(), klass
        );
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(expr)));
        return setNodeExprTypes(node);
    }

    AddObjectNode createAddObject(ClassType klass) {
        return setNodeExprTypes(new AddObjectNode(null, nextName("Add"), klass, scope().getLastNode(), scope()));
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
        return setNodeExprTypes(node);
    }

    ForeachNode createForEach(Expression array) {
        var loopType = newTemporaryType("ForeachOutput");
        newTemproryField(loopType, "array", array.getType());
        newTemproryField(loopType, "index", ModelDefRegistry.getType(Long.class));
        return setNodeExprTypes(new ForeachNode(
                null, nextName("Foreach"), loopType,
                scope().getLastNode(), scope(), new ExpressionValue(array),
                new ExpressionValue(ExpressionUtil.trueExpression())
        ));
    }

    public Field newTemproryField(ClassType klass, String name, Type type) {
        return FieldBuilder.newBuilder(name, name, klass, type).build();
    }

    public ClassType newTemporaryType(String namePrefix) {
        String name = namePrefix + "_" + NncUtils.random();
        return ClassBuilder.newBuilder(name, name)
                .anonymous(true)
                .ephemeral(true)
                .build();
    }

    SubFlowNode createSubFlow(Expression self, Flow flow, List<Expression> arguments) {
        List<Argument> args = NncUtils.biMap(
                flow.getParameters(), arguments,
                (param, arg) -> new Argument(null, param, new ExpressionValue(arg))
        );
        return setNodeExprTypes(new SubFlowNode(null, nextName(flow.getName()), scope().getLastNode(), scope(),
                new ExpressionValue(self), args, flow));
    }

    LambdaNode createLambda(List<Parameter> parameters, Type returnType, ClassType functionalInterface) {
        var funcType = entityContext.getFunctionTypeContext().get(
                NncUtils.map(parameters, Parameter::getType), returnType
        );
        return new LambdaNode(
                null, nextName("Lambda"), scope().getLastNode(), scope(),
                parameters, returnType, funcType, functionalInterface
        );
    }

    NewNode createNew(Flow flow, List<Expression> arguments) {
        List<Argument> args = NncUtils.biMap(
                flow.getParameters(), arguments,
                (param, arg) -> new Argument(null, param, new ExpressionValue(arg))
        );
        return setNodeExprTypes(new NewNode(null, nextName(flow.getName()), flow, args,
                scope().getLastNode(), scope()));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        var type = ClassBuilder.newBuilder("输入类型", "InputType").temporary().build();
        return setNodeExprTypes(new InputNode(null, nextName("Input"), type, scope().getLastNode(), scope()));
    }

    public SelfNode createSelf() {
        return setNodeExprTypes(new SelfNode(null, nextName("Self"),
                SelfNode.getSelfType(scope().getFlow(), entityContext),
                scope().getLastNode(), scope()));
    }

    private <T extends NodeRT<?>> T setNodeExprTypes(T node) {
        var scope = scope();
        var lastNode = scope.getLastNode();
        if (lastNode == null) {
            node.mergeExpressionTypes(scope.getExpressionTypes());
        } else {
            node.mergeExpressionTypes(lastNode.getExpressionTypes());
        }
        return node;
    }

    private String nextName(String prefix) {
        int cnt = name2count.compute(prefix, (k, c) -> c == null ? 1 : c + 1);
        return cnt > 1 ? prefix + "_" + (cnt - 1) : prefix;
    }

    @SuppressWarnings("UnusedReturnValue")
    public RaiseNode createRaise(Expression exception) {
        var node = setNodeExprTypes(new RaiseNode(
                null,
                nextName("Error"),
                RaiseParameterKind.THROWABLE,
                new ExpressionValue(exception),
                null,
                scope().getLastNode(),
                scope()
        ));
        variableTable.processRaiseNode(node);
        return node;
    }

    public WhileNode createWhile() {
        return createWhile(ExpressionUtil.trueExpression());
    }

    public WhileNode createWhile(Expression condition) {
        return setNodeExprTypes(new WhileNode(
                null, nextName("While"),
                newTemporaryType("WhileOutput"),
                scope().getLastNode(),
                scope(),
                new ExpressionValue(condition)
        ));
    }

    public void setYieldValue(Expression expression) {
        yieldValue = NncUtils.requireNonNull(expression);
    }

    public PsiMethod getMethod() {
        return null;
    }

    private static final class ScopeInfo {
        private final ScopeRT scope;
//        private Map<Expression, Type> expressionTypes;

        private ScopeInfo(ScopeRT scope/*, Map<Expression, Type> expressionTypes*/) {
            this.scope = scope;
//            this.expressionTypes = expressionTypes;
        }

    }


}
