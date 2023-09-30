package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.getEnumConstantName;

public class Generator extends VisitorBase {

    private final LinkedList<FlowGenerator> builders = new LinkedList<>();
    private final Map<String, ClassType> classes = new HashMap<>();
    private final LinkedList<ClassInfo> classInfoStack = new LinkedList<>();
    private final TypeResolver typeResolver;
    private final IEntityContext entityContext;

    public Generator(TypeResolver typeResolver, IEntityContext entityContext) {
        this.typeResolver = typeResolver;
        this.entityContext = entityContext;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        psiClass.putUserData(Keys.RESOLVE_STAGE, 2);
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.META_CLASS));
        klass.setCode(psiClass.getName());

        var initFlow = klass.getFlow("<init>", List.of());
        var initFlowBuilder = new FlowGenerator(this, initFlow, typeResolver, entityContext);
        initFlowBuilder.enterScope(initFlowBuilder.getFlow().getRootScope(), null);
        initFlowBuilder.createSelf();
        initFlowBuilder.createInput();

        var classInit = klass.getFlow("<cinit>", List.of());
        var classInitFlowBuilder = new FlowGenerator(this, classInit, typeResolver, entityContext);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getFlow().getRootScope(), null);
        classInitFlowBuilder.createInput();

        enterClass(new ClassInfo(klass, psiClass, initFlowBuilder, classInitFlowBuilder));

        super.visitClass(psiClass);

        initFlowBuilder.createReturn();
        initFlowBuilder.exitScope();

        classInitFlowBuilder.createReturn();
        classInitFlowBuilder.exitScope();

        exitClass();

        klass.stage = ResolutionStage.GENERATED;
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var field = NncUtils.requireNonNull(enumConstant.getUserData(Keys.FIELD));
        var builder = currentClassInfo().staticBuilder;
        var constructor = enumConstant.resolveConstructor();
        List<Type> paramTypes = new ArrayList<>();
        paramTypes.add(ModelDefRegistry.getType(String.class));
        paramTypes.add(ModelDefRegistry.getType(long.class));
        paramTypes.addAll(
                NncUtils.map(
                        requireNonNull(constructor).getParameterList().getParameters(),
                        param -> typeResolver.resolveTypeOnly(param.getType())
                )
        );
        List<Expression> args = new ArrayList<>();
        args.add(ExpressionUtil.constantString(getEnumConstantName(enumConstant)));
        args.add(ExpressionUtil.constantLong(currentClassInfo().nextEnumConstantOrdinal()));
        args.addAll(NncUtils.map(
                requireNonNull(enumConstant.getArgumentList()).getExpressions(),
                expr -> builder.getExpressionResolver().resolve(expr)
        ));
        var expr = builder.getExpressionResolver().newInstance(currentClass(), args,
                List.of(TranspileUtil.createType(String.class), TranspileUtil.createType(Long.class)),
                enumConstant);
        builder.createUpdateStatic(currentClass(), Map.of(field, expr));
    }

    @Override
    public void visitField(PsiField psiField) {
        var field = NncUtils.requireNonNull(psiField.getUserData(Keys.FIELD));
        if (psiField.getInitializer() != null) {
            var builder = field.isStatic() ? currentClassInfo().staticBuilder : currentClassInfo().fieldBuilder;
            var initializer = builder.getExpressionResolver().resolve(psiField.getInitializer());
            builder.createUpdate(selfExpression(), Map.of(field, initializer));
        }
    }

    private void enterClass(ClassInfo classInfo) {
        classes.put(classInfo.psiClass.getQualifiedName(), classInfo.klass);
        classInfoStack.push(classInfo);
    }

    private void exitClass() {
        classInfoStack.pop();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        var flow = NncUtils.requireNonNull(method.getUserData(Keys.FLOW));
        FlowGenerator builder = new FlowGenerator(this, flow, typeResolver, entityContext);
        builders.push(builder);
        builder.enterScope(flow.getRootScope(), null);
        var selfNode = builder().createSelf();
        builder.setVariable("this", new NodeExpression(selfNode));
        processParameters(method.getParameterList());
        if (method.isConstructor()) {
            builder.createSubFlow(
                    new NodeExpression(selfNode),
                    currentClassInfo().fieldBuilder.getFlow(),
                    List.of()
            );
            if (currentClass().isEnum()) {
                var klass = currentClass();
                var inputNode = flow.getInputNode();
                builder.createUpdate(
                        new NodeExpression(selfNode),
                        Map.of(
                                klass.getFieldByCode("name"),
                                new FieldExpression(
                                        new NodeExpression(inputNode),
                                        inputNode.getType().getFieldByCode("name")
                                ),
                                klass.getFieldByCode("ordinal"),
                                new FieldExpression(
                                        new NodeExpression(inputNode),
                                        inputNode.getType().getFieldByCode("ordinal")
                                )
                        )
                );
            }
        }
        requireNonNull(method.getBody()).accept(this);
        if (method.isConstructor()) {
            builder.createReturn(new NodeExpression(flow.getRootNode()));
        } else if (!requireNonNull(flow.getRootScope().getLastNode()).isExit()) {
            builder.createReturn();
        }
        builder.exitScope();
        builders.pop();
    }


    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        if (statement.getBody() != null && statement.getBody().getStatementCount() > 0) {
            var fistStmt = statement.getBody().getStatements()[0];
            if (fistStmt instanceof PsiSwitchLabeledRuleStatement) {
                processLabeledRuleSwitch(statement);
            } else {
                processClassicSwitch(statement);
            }
        }
    }

    private void processLabeledRuleSwitch(PsiSwitchStatement statement) {
        var switchExpr = new NodeExpression(
                builder().createValue("switchValue", resolveExpression(statement.getExpression()))
        );
        var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
        var branchNode = builder().createBranch(false);
        builder().enterCondSection(statement);
//        boolean hasResult = statement.getType() != null;
//        Map<Branch, Value> yieldMap = new HashMap<>();
        var modified = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE)).getModified();
        var liveVarOut = NncUtils.requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        var outputVars = NncUtils.intersect(modified, liveVarOut);

        for (PsiStatement stmt : stmts) {
            var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
            var caseLabelElementList = labeledRuleStmt.getCaseLabelElementList();
            Branch branch;
            if (caseLabelElementList != null && caseLabelElementList.getElementCount() > 0) {
                Expression cond;
                if (isTypePatternCase(caseLabelElementList)) {
                    PsiTypeTestPattern typeTestPattern = (PsiTypeTestPattern) caseLabelElementList.getElements()[0];
                    var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
                    builder().setVariable(
                            requireNonNull(typeTestPattern.getPatternVariable()).getName(),
                            switchExpr
                    );
                    cond = new InstanceOfExpression(
                            switchExpr,
                            typeResolver.resolveDeclaration(checkType)
                    );
                } else {
                    var expressions = NncUtils.map(caseLabelElementList.getElements(),
                            e -> resolveExpression((PsiExpression) e));
                    cond = new BinaryExpression(
                            Operator.IN,
                            resolveExpression(statement.getExpression()),
                            new ArrayExpression(expressions)
                    );
                }
                branch = branchNode.addBranch(new ExpressionValue(cond));
            }
            else {
                branch = branchNode.addDefaultBranch();
            }
            builder().newCondBranch(stmt, branch);
            builder().enterScope(branch.getScope(), branch.getCondition().getExpression());
            if(labeledRuleStmt.getBody() != null) {
                labeledRuleStmt.getBody().accept(this);
                if(labeledRuleStmt.getBody() instanceof PsiExpression bodyExpression) {
                    builder().setYieldValue(resolveExpression(bodyExpression));
                }
            }
//            if(hasResult) {
//                yieldMap.put(branch, new ExpressionValue(NncUtils.requireNonNull(builder().getYield())));
//            }
            builder().exitScope();
        }
        var condOutputs = builder().exitCondSection(statement, NncUtils.map(outputVars, Objects::toString));
        var mergeNode = builder().createMerge();
//        Expression result;
//        if(hasResult) {
//            var yieldField = FieldBuilder.newBuilder("yield", "yield", mergeNode.getType(),
//                            typeResolver.resolveDeclaration(statement.getType()))
//                    .build();
//            new MergeNodeField(yieldField, mergeNode, yieldMap);
//            result = new FieldExpression(new NodeExpression(mergeNode), yieldField);
//        }
//        else {
//            result = null;
//        }
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(), mergeNode.getType(),
                            typeResolver.resolveDeclaration(outputVar.type()))
                    .build();
            var mergeField = new MergeNodeField(field, mergeNode);
            condOutputs.forEach((branch, values) ->
                    mergeField.setValue(branch, new ExpressionValue(values.get(outputVar.toString())))
            );
        }
//        return result;
    }

    private Expression processClassicSwitch(PsiSwitchStatement statement) {
        throw new UnsupportedOperationException();
    }

    private boolean isTypePatternCase(PsiCaseLabelElementList caseLabelElementList) {
        return caseLabelElementList.getElementCount() == 1 &&
                caseLabelElementList.getElements()[0] instanceof PsiTypeTestPattern;
    }


    private void processParameters(PsiParameterList parameterList) {
        var inputNode = builder().createInput();
        for (PsiParameter parameter : parameterList.getParameters()) {
            processParameter(parameter, inputNode);
        }
    }

    private ClassType currentClass() {
        return requireNonNull(classInfoStack.peek()).klass;
    }

    private ClassInfo currentClassInfo() {
        return requireNonNull(classInfoStack.peek());
    }

    private void processParameter(PsiParameter parameter, InputNode inputNode) {
        var field = inputNode.getType().getFieldByCode(parameter.getName());
        builder().setVariable(
                parameter.getName(),
                new FieldExpression(new NodeExpression(inputNode), field)
        );
    }

    private Type resolveType(PsiType type) {
        return typeResolver.resolveDeclaration(type);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
//        super.visitIfStatement(statement);
        Expression cond = resolveExpression(requireNonNull(statement.getCondition()));
        BranchNode branchNode = builder().createBranch(false);
        var mergeNode = builder().createMerge();
        Scope bodyScope = requireNonNull(statement.getUserData(Keys.BODY_SCOPE)),
                elseScope = requireNonNull(statement.getUserData(Keys.ELSE_SCOPE));
        Set<QualifiedName> modified = NncUtils.union(bodyScope.getModified(), elseScope.getModified());
        Set<QualifiedName> outputVars = getBlockOutputVariables(statement, modified);
        List<QualifiedName> outputVariables = new ArrayList<>();
        for (QualifiedName var : outputVars) {
            FieldBuilder.newBuilder(var.toString(), var.toString(), mergeNode.getType(), resolveType(var.type())).build();
            outputVariables.add(var);
        }
        enterCondSection(statement);
        newCondBranch(statement, branchNode.addBranch(new ExpressionValue(cond)), statement.getThenBranch());
        newCondBranch(statement, branchNode.addDefaultBranch(), statement.getElseBranch());

        exitCondSection(statement, mergeNode, outputVariables);
    }

    private void enterCondSection(PsiElement node) {
        builder().enterCondSection(node);
    }

    private void newCondBranch(PsiElement sectionId, Branch branch, @Nullable PsiStatement body) {
        builder().newCondBranch(sectionId, branch);
        builder().enterScope(branch.getScope(), branch.getCondition().getExpression());
        if (body != null) body.accept(this);
        builder().exitScope();
    }

    private void exitCondSection(PsiElement element, MergeNode mergeNode, List<QualifiedName> outputVariables) {
        List<String> outputVars = NncUtils.map(outputVariables, Objects::toString);
        var condOutputs = builder().exitCondSection(element, outputVars);
        for (var qn : outputVariables) {
            var field = mergeNode.getType().getFieldByCode(qn.toString());
            var mergeField = new MergeNodeField(field, mergeNode);
            for (var entry2 : condOutputs.entrySet()) {
                var branch = entry2.getKey();
                var branchOutputs = entry2.getValue();
                mergeField.setValue(branch, new ExpressionValue(branchOutputs.get(qn.toString())));
            }
            builder().setVariable(qn.toString(), new FieldExpression(new NodeExpression(mergeNode), mergeField.getField()));
        }
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        if (variable.getInitializer() != null) {
            var valueNode = builder().createValue(variable.getName(), resolveExpression(variable.getInitializer()));
            builder().setVariable(variable.getName(), new NodeExpression(valueNode));
        }
    }

    private void processLoop(PsiLoopStatement statement, LoopNode<?> node,
                             @Nullable Consumer<Map<QualifiedName, Field>> preprocessor) {
        var bodyScope = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
        Set<QualifiedName> liveIn = requireNonNull(statement.getUserData(Keys.LIVE_VARS_IN));
        Set<QualifiedName> liveOut = requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        List<QualifiedName> loopVars = NncUtils.filter(
                bodyScope.getModified(), qn -> liveIn.contains(qn) || liveOut.contains(qn)
        );
        Map<Field, Expression> initialValues = new HashMap<>();
        Map<QualifiedName, Field> loopVar2Field = new HashMap<>();
        for (QualifiedName loopVar : loopVars) {
            var field = builder().newTemproryField(
                    node.getType(),
                    loopVar.toString(),
                    resolveType(loopVar.type())
            );
            loopVar2Field.put(loopVar, field);
            initialValues.put(field, builder().getVariable(loopVar.toString()));
        }
        for (QualifiedName loopVar : loopVar2Field.keySet()) {
            var field = loopVar2Field.get(loopVar);
            builder().setVariable(loopVar.toString(), new FieldExpression(new NodeExpression(node), field));
        }
        builder().enterScope(node.getLoopScope(), node.getCondition().getExpression());
        if (preprocessor != null) {
            preprocessor.accept(loopVar2Field);
        }
//        for (QualifiedName loopVar : loopVars) {
//            var field = loopVar2Field.get(loopVar);
//            var valueNode = builder().createValue(
//                    loopVar.toString(),
//                    new FieldExpression(new NodeExpression(node), field)
//            );
//            builder().setVariable(loopVar.toString(), new NodeExpression(valueNode));
//        }
        if (statement.getBody() != null) {
            statement.getBody().accept(this);
        }
        builder().exitScope();
        for (QualifiedName loopVar : loopVars) {
            var field = loopVar2Field.get(loopVar);
            var initialValue = initialValues.get(field);
            var updatedValue = builder().getVariable(loopVar.toString());
            node.setField(field, new ExpressionValue(initialValue), new ExpressionValue(updatedValue));
            builder().setVariable(loopVar.toString(), new FieldExpression(new NodeExpression(node), field));
        }
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        throw new InternalException("For loop should be transformed into while loop before code generation");
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        var node = builder().createWhile();
        processLoop(statement, node, loopVar2Field ->
                node.setCondition(new ExpressionValue(resolveExpression(statement.getCondition())))
        );
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var iteratedExpr = resolveExpression(statement.getIteratedValue());
        if (iteratedExpr.getType() instanceof ArrayType) {
            var whileNode = builder().createWhile();
            var indexField = FieldBuilder
                    .newBuilder("索引", "index", whileNode.getType(), StandardTypes.getLongType())
                    .build();
            whileNode.setCondition(
                    new ExpressionValue(
                            new BinaryExpression(
                                    Operator.LT,
                                    new FieldExpression(new NodeExpression(whileNode), indexField),
                                    new FunctionExpression(Function.LEN, iteratedExpr)
                            )
                    )
            );
            whileNode.setField(indexField,
                    new ExpressionValue(ExpressionUtil.constantLong(0L)),
                    new ExpressionValue(
                            new BinaryExpression(
                                    Operator.ADD,
                                    new FieldExpression(new NodeExpression(whileNode), indexField),
                                    ExpressionUtil.constantLong(1L)
                            )
                    )
            );
            processLoop(statement, whileNode,
                    loopVar2Field -> builder().setVariable(statement.getIterationParameter().getName(),
                    new ArrayAccessExpression(iteratedExpr,
                            new FieldExpression(new NodeExpression(whileNode), indexField))));
        } else {
            var collType = (ClassType) iteratedExpr.getType();
            var itNode = builder().createSubFlow(
                    iteratedExpr, collType.getFlowByCode("iterator"),
                    List.of()
            );
            var whileNode = builder().createWhile(
                    new FunctionExpression(Function.HAS_NEXT, new NodeExpression(itNode))
            );
            var itType = (ClassType) NncUtils.requireNonNull(itNode.getType());
            processLoop(statement, whileNode, loopVar2Field -> {
                var elementNode = builder().createSubFlow(
                        new NodeExpression(itNode),
                        itType.getFlowByCode("next"),
                        List.of()
                );
                builder().setVariable(statement.getIterationParameter().getName(), new NodeExpression(elementNode));
            });
        }
    }

    private Expression getExtraLoopTest(PsiForeachStatement statement) {
        var firstStmt = getFirstStatement(statement.getBody());
        if (isExtraLoopTest(firstStmt)) {
            var cond = ((PsiMethodCallExpression) ((PsiExpressionStatement) firstStmt).getExpression())
                    .getArgumentList().getExpressions()[0];
            return resolveExpression(cond);
        }
        return ExpressionUtil.trueExpression();
    }

    private Set<QualifiedName> getBlockOutputVariables(PsiStatement statement, Set<QualifiedName> modified) {
        Set<QualifiedName> liveOut = requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        return NncUtils.filterUnique(modified, qn -> qn.isSimple() && liveOut.contains(qn));
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        Expression returnValue;
        if (builder().getFlow().isConstructor()) {
            returnValue = new NodeExpression(builder().getFlow().getRootNode());
        } else {
            if (statement.getReturnValue() != null) {
                returnValue = resolveExpression(statement.getReturnValue());
            } else {
                returnValue = null;
            }
        }
        builder().createReturn(returnValue);
    }

    @Override
    public void visitThrowStatement(PsiThrowStatement statement) {
        builder().createException(new ConstantExpression(InstanceUtils.stringInstance("Error")));
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        builder().setYieldValue(resolveExpression(statement.getExpression()));
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        if (isExtraLoopTest(statement)) {
            return;
        }
        resolveExpression(statement.getExpression());
    }

    private Expression selfExpression() {
        return builder().getVariable("this");
    }

    private Expression resolveExpression(PsiExpression expression) {
        return builder().getExpressionResolver().resolve(expression);
    }

    private FlowGenerator builder() {
        return requireNonNull(builders.peek());
    }

    @SuppressWarnings("unused")
    public Map<String, ClassType> getClasses() {
        return classes;
    }

    private static final class ClassInfo {
        private final ClassType klass;
        private final PsiClass psiClass;
        private final FlowGenerator fieldBuilder;
        private final FlowGenerator staticBuilder;
        private int enumConstantCount;

        private ClassInfo(
                ClassType klass,
                PsiClass psiClass,
                FlowGenerator fieldBuilder,
                FlowGenerator staticBuilder
        ) {
            this.klass = klass;
            this.psiClass = psiClass;
            this.fieldBuilder = fieldBuilder;
            this.staticBuilder = staticBuilder;
        }

        private int nextEnumConstantOrdinal() {
            return enumConstantCount++;
        }


    }

}
