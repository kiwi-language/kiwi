package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.getEnumConstantName;
import static tech.metavm.expression.ExpressionUtil.trueExpression;

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
        var stage = NncUtils.requireNonNull(psiClass.getUserData(Keys.RESOLVE_STAGE));
        if(stage == 2) {
            return;
        }
        psiClass.putUserData(Keys.RESOLVE_STAGE, 2);
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.META_CLASS));
        klass.setCode(TranspileUtil.getClassCode(psiClass));

        var initFlow = klass.getFlowByCodeAndParamTypes("<init>", List.of());
        var initFlowBuilder = new FlowGenerator(initFlow, typeResolver, entityContext, this);
        initFlowBuilder.enterScope(initFlowBuilder.getFlow().getRootScope());
        initFlowBuilder.setVariable("this", new NodeExpression(initFlowBuilder.createSelf()));
        initFlowBuilder.createInput();

        var classInit = klass.getFlowByCodeAndParamTypes("<cinit>", List.of());
        var classInitFlowBuilder = new FlowGenerator(classInit, typeResolver, entityContext, this);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getFlow().getRootScope());
        classInitFlowBuilder.createInput();

        enterClass(new ClassInfo(klass, psiClass, initFlowBuilder, classInitFlowBuilder));

        super.visitClass(psiClass);

        initFlowBuilder.createReturn();
        initFlowBuilder.exitScope();

        classInitFlowBuilder.createReturn();
        classInitFlowBuilder.exitScope();

        boolean hasConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
        if (!hasConstructor) {
            var constructor = klass.getDefaultConstructor();
            var constructorGen = new FlowGenerator(constructor, typeResolver, entityContext, this);
            constructorGen.enterScope(constructor.getRootScope());
            var self = new NodeExpression(constructorGen.createSelf());
            constructorGen.createSubFlow(self, initFlow, List.of());
            constructorGen.createReturn(self);
            constructorGen.exitScope();
        }
        exitClass();
        klass.setStage(ResolutionStage.GENERATED);
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
            if (field.isStatic()) {
                var builder = currentClassInfo().staticBuilder;
                var initializer = builder.getExpressionResolver().resolve(psiField.getInitializer());
                builder.createUpdateStatic(currentClass(), Map.of(field, initializer));
            } else {
                var builder = currentClassInfo().fieldBuilder;
                var initializer = builder.getExpressionResolver().resolve(psiField.getInitializer());
                builder.createUpdate(builder.getVariable("this"), Map.of(field, initializer));
            }
        }
        else if(field.getType().isNullable()) {
            var builder = currentClassInfo().fieldBuilder;
            builder.createUpdate(builder.getVariable("this"), Map.of(field, ExpressionUtil.nullExpression()));
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
    public void visitTryStatement(PsiTryStatement statement) {
        var tryBlock = NncUtils.requireNonNull(statement.getTryBlock());
        var tryScope = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
        Set<QualifiedName> liveOut = getBlockLiveOut(tryBlock);
        Set<QualifiedName> outputVars = liveOut == null ? Set.of() : NncUtils.intersect(tryScope.getModified(), liveOut);
        var tryNode = builder().createTry();
        builder().enterTrySection(tryNode);
        tryBlock.accept(this);
        var trySectionOutput = builder().exitTrySection(tryNode, NncUtils.map(outputVars, Objects::toString));
        var tryEndNode = builder().createTryEnd();
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(),
                            tryEndNode.getType(), resolveType(outputVar.type()))
                    .build();
            new TryEndField(
                    field,
                    NncUtils.map(
                            trySectionOutput.keySet(),
                            raiseNode -> new TryEndValue(
                                    raiseNode,
                                    new ExpressionValue(trySectionOutput.get(raiseNode).get(outputVar.toString()))
                            )
                    ),
                    new ExpressionValue(builder().getVariable(outputVar.toString())),
                    tryEndNode
            );
        }

        var exceptionExpr = new PropertyExpression(
                new NodeExpression(tryEndNode),
                tryEndNode.getType().getFieldByCodeRequired("exception")
        );

        if (statement.getCatchSections().length > 0) {
            Set<QualifiedName> catchModified = new HashSet<>();
            Set<QualifiedName> catchLiveOut = null;
            var branchNode = builder().createBranchNode(false);
            builder().enterCondSection(branchNode);
            for (PsiCatchSection catchSection : statement.getCatchSections()) {
                var catchBlock = NncUtils.requireNonNull(catchSection.getCatchBlock());
                catchModified.addAll(requireNonNull(catchSection.getUserData(Keys.BODY_SCOPE)).getModified());
                if (catchLiveOut == null) {
                    catchLiveOut = getBlockLiveOut(catchBlock);
                }
                var branch = branchNode.addBranch(
                        new ExpressionValue(
                                createExceptionCheck(exceptionExpr, catchSection.getPreciseCatchTypes())
                        )
                );
                builder().setVariable(requireNonNull(catchSection.getParameter()).getName(), exceptionExpr);
                builder().enterBranch(branch);
                catchBlock.accept(this);
                builder().exitBranch();
            }
            var defaultBranch = branchNode.addDefaultBranch();
            builder().enterBranch(defaultBranch);
            builder().exitBranch();
            List<QualifiedName> catchOutputVars = catchLiveOut == null ? List.of()
                    : new ArrayList<>(NncUtils.intersect(catchModified, catchLiveOut));
            var mergeNode = builder().createMerge();
            for (QualifiedName catchOutputVar : catchOutputVars) {
                FieldBuilder.newBuilder(catchOutputVar.toString(), catchOutputVar.toString(),
                        mergeNode.getType(), resolveType(catchOutputVar.type())).build();
            }
            exitCondSection(mergeNode, catchOutputVars);

            var exceptionField = FieldBuilder.newBuilder("异常", "exception",
                    mergeNode.getType(), StandardTypes.getNullableThrowableType()).build();

            final var exceptionExprFinal = exceptionExpr;
            new MergeNodeField(
                    exceptionField,
                    mergeNode,
                    NncUtils.toMap(
                            branchNode.getBranches(),
                            java.util.function.Function.identity(),
                            branch ->
                                    new ExpressionValue(
                                            branch.isPreselected() ? exceptionExprFinal : ExpressionUtil.nullExpression()
                                    )
                    )
            );
            exceptionExpr = new PropertyExpression(new NodeExpression(mergeNode), exceptionField);
        }
        if (statement.getFinallyBlock() != null) {
            statement.getFinallyBlock().accept(this);
        }
        var exceptBranchNode = builder().createBranchNode(false);
        var exceptionBranch = exceptBranchNode.addBranch(
                new ExpressionValue(new UnaryExpression(Operator.IS_NOT_NULL, exceptionExpr))
        );
        exceptBranchNode.addDefaultBranch();
        builder().enterCondSection(exceptBranchNode);
        builder().enterBranch(exceptionBranch);
        builder().createRaise(exceptionExpr);
        builder().exitBranch();
        builder().createMerge();
    }

    private @Nullable Set<QualifiedName> getBlockLiveOut(PsiCodeBlock block) {
        if (block.isEmpty()) {
            return null;
        }
        var lastStmt = block.getStatements()[block.getStatementCount() - 1];
        return NncUtils.requireNonNull(lastStmt.getUserData(Keys.LIVE_VARS_OUT));
    }

    private Expression createExceptionCheck(Expression exceptionExpr, List<PsiType> catchTypes) {
        NncUtils.requireNotEmpty(catchTypes);
        Expression expression = null;
        for (PsiType catchType : catchTypes) {
            var checkExpr = new InstanceOfExpression(exceptionExpr, resolveType(catchType));
            if (expression == null) {
                expression = checkExpr;
            } else {
                expression = new BinaryExpression(Operator.OR, expression, checkExpr);
            }
        }
        return NncUtils.requireNonNull(expression);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        var flow = NncUtils.requireNonNull(method.getUserData(Keys.FLOW));
        FlowGenerator builder = new FlowGenerator(flow, typeResolver, entityContext, this);
        builders.push(builder);
        builder.enterScope(flow.getRootScope());
        var selfNode = builder().createSelf();
        builder.setVariable("this", new NodeExpression(selfNode));
        processParameters(method.getParameterList(), flow);
        if (method.isConstructor()) {
            var superType = currentClass().getSuperType();
            if (superType != null && !isEnumType(superType) && !isSuperCallPresent(method)) {
                builder().createSubFlow(
                        builder().getVariable("this"),
                        superType.getFlowByCodeAndParamTypes(superType.getCodeRequired(), List.of()),
                        List.of()
                );
            }
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
                                new PropertyExpression(
                                        new NodeExpression(inputNode),
                                        inputNode.getType().getFieldByCode("name")
                                ),
                                klass.getFieldByCode("ordinal"),
                                new PropertyExpression(
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

    private boolean isEnumType(ClassType classType) {
        return classType.getTemplate() != null && classType.getTemplate() == StandardTypes.getEnumType();
    }

    private static boolean isSuperCallPresent(PsiMethod method) {
        var stmts = requireNonNull(method.getBody()).getStatements();
        boolean requireSuperCall = false;
        if (stmts.length > 0) {
            if (stmts[0] instanceof PsiExpressionStatement exprStmt &&
                    exprStmt.getExpression() instanceof PsiMethodCallExpression methodCallExpr) {
                if (Objects.equals(methodCallExpr.getMethodExpression().getReferenceName(), "super")) {
                    requireSuperCall = true;
                }
            }
        }
        return requireSuperCall;
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
        var branchNode = builder().createBranchNode(false);
        builder().enterCondSection(branchNode);
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
                            ArrayExpression.create(expressions, entityContext)
                    );
                }
                branch = branchNode.addBranch(new ExpressionValue(cond));
            } else {
                branch = branchNode.addDefaultBranch();
            }
            builder().enterBranch(branch);
            if (labeledRuleStmt.getBody() != null) {
                labeledRuleStmt.getBody().accept(this);
                if (labeledRuleStmt.getBody() instanceof PsiExpression bodyExpression) {
                    builder().setYieldValue(resolveExpression(bodyExpression));
                }
            }
            builder().exitBranch();
        }
        var mergeNode = builder().createMerge();
        var condOutputs = builder().exitCondSection(mergeNode, NncUtils.map(outputVars, Objects::toString));
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(), mergeNode.getType(),
                            typeResolver.resolveDeclaration(outputVar.type()))
                    .build();
            var mergeField = new MergeNodeField(field, mergeNode);
            condOutputs.forEach((branch, values) ->
                    mergeField.setValue(branch, new ExpressionValue(values.get(outputVar.toString())))
            );
        }
    }

    private Expression processClassicSwitch(PsiSwitchStatement statement) {
        throw new UnsupportedOperationException();
    }

    private boolean isTypePatternCase(PsiCaseLabelElementList caseLabelElementList) {
        return caseLabelElementList.getElementCount() == 1 &&
                caseLabelElementList.getElements()[0] instanceof PsiTypeTestPattern;
    }


    private void processParameters(PsiParameterList parameterList, Flow flow) {
        var inputNode = builder().createInput();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputNode.getType(), parameter.getType())
                    .build();
        }
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
                new PropertyExpression(new NodeExpression(inputNode), field)
        );
    }

    private Type resolveType(PsiType type) {
        return typeResolver.resolveDeclaration(type);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        BranchNode branchNode = builder().createBranchNode(false);

        var thenBranch = branchNode.addBranch(new ExpressionValue(trueExpression()));
        var elseBranch = branchNode.addDefaultBranch(true);

        enterCondSection(branchNode);

        builder().enterBranch(thenBranch);
        builder().getExpressionResolver().constructBool(requireNonNull(statement.getCondition()), branchNode);
        if (statement.getThenBranch() != null) {
            statement.getThenBranch().accept(this);
        }
        builder().exitBranch();

        builder().enterBranch(elseBranch);
        if (statement.getElseBranch() != null) {
            statement.getElseBranch().accept(this);
        }
        builder().exitBranch();

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

        exitCondSection(mergeNode, outputVariables);
    }

    private void enterCondSection(BranchNode branchNode) {
        builder().enterCondSection(branchNode);
    }

    private void newCondBranch(PsiElement sectionId, Branch branch, @Nullable PsiStatement body) {
        builder().enterBranch(branch);
        if (body != null) body.accept(this);
        builder().exitBranch();
    }

    private void exitCondSection(MergeNode mergeNode, List<QualifiedName> outputVariables) {
        List<String> outputVars = NncUtils.map(outputVariables, Objects::toString);
        var condOutputs = builder().exitCondSection(mergeNode, outputVars);
        for (var qn : outputVariables) {
            var field = mergeNode.getType().getFieldByCodeRequired(qn.toString());
            var mergeField = new MergeNodeField(field, mergeNode);
            for (var entry2 : condOutputs.entrySet()) {
                var branch = entry2.getKey();
                var branchOutputs = entry2.getValue();
                mergeField.setValue(branch, new ExpressionValue(branchOutputs.get(qn.toString())));
            }
            builder().setVariable(qn.toString(), new PropertyExpression(new NodeExpression(mergeNode), mergeField.getField()));
        }
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        if (variable.getInitializer() != null) {
            var valueNode = builder().createValue(variable.getName(), resolveExpression(variable.getInitializer()));
            builder().setVariable(variable.getName(), new NodeExpression(valueNode));
        }
    }

    private void processLoop(PsiLoopStatement statement,
                             PsiExpression condition,
                             @Nullable BiConsumer<WhileNode, Map<QualifiedName, Field>> preprocessor) {
        Expression condInitialValue = null;
        if (condition != null) {
            condInitialValue = resolveExpression(condition);
        }
        var node = builder().createWhile();
        Field condField = null;
        if(condition != null) {
            condField = builder().newTemproryField(node.getType(), "循环条件", StandardTypes.getBoolType());
        }
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
            builder().setVariable(loopVar.toString(), new PropertyExpression(new NodeExpression(node), field));
        }
        var narrower = new TypeNarrower(node.getExpressionTypes()::getType);
        node.getBodyScope().setExpressionTypes(narrower.narrowType(node.getCondition().getExpression()));
        builder().enterScope(node.getBodyScope());
        if (preprocessor != null) {
            preprocessor.accept(node, loopVar2Field);
        }
        if (statement.getBody() != null) {
            statement.getBody().accept(this);
        }
        Expression condUpdatedValue = null;
        if (condition != null) {
            condUpdatedValue = resolveExpression(condition);
        }
        builder().exitScope();
        for (QualifiedName loopVar : loopVars) {
            var field = loopVar2Field.get(loopVar);
            var initialValue = initialValues.get(field);
            var updatedValue = builder().getVariable(loopVar.toString());
            node.setField(field, new ExpressionValue(initialValue), new ExpressionValue(updatedValue));
            builder().setVariable(loopVar.toString(), new PropertyExpression(new NodeExpression(node), field));
        }
        if (condition != null) {
            node.setField(requireNonNull(condField),
                    new ExpressionValue(requireNonNull(condInitialValue)),
                    new ExpressionValue(requireNonNull(condUpdatedValue))
            );
            var extraCond = new PropertyExpression(new NodeExpression(node), condField);
            var finalCond = node.getCondition() == null ? extraCond :
                    ExpressionUtil.and(node.getCondition().getExpression(), extraCond);
            node.setCondition(new ExpressionValue(finalCond));
        }
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        throw new InternalException("For loop should be transformed into while loop before code generation");
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        processLoop(statement, statement.getCondition(), null
//                loopVar2Field ->
//                        node.setCondition(new ExpressionValue(resolveExpression(statement.getCondition())))
        );
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var iteratedExpr = resolveExpression(statement.getIteratedValue());
        if (iteratedExpr.getType() instanceof ArrayType) {
            processLoop(statement, getExtraLoopTest(statement),
                    (whileNode, loopVar2Field) -> {
                        var indexField = FieldBuilder
                                .newBuilder("索引", "index", whileNode.getType(), StandardTypes.getLongType())
                                .build();
                        whileNode.setCondition(
                                new ExpressionValue(
                                        ExpressionUtil.lt(
                                                ExpressionUtil.nodeProp(whileNode, indexField),
                                                new FunctionExpression(Function.LEN, iteratedExpr)
                                        )
                                )
                        );
                        whileNode.setField(indexField,
                                new ExpressionValue(ExpressionUtil.constantLong(0L)),
                                new ExpressionValue(
                                        ExpressionUtil.add(
                                                ExpressionUtil.nodeProp(whileNode, indexField),
                                                ExpressionUtil.constantLong(1L)
                                        )
                                )
                        );
                        builder().setVariable(statement.getIterationParameter().getName(),
                                ExpressionUtil.arrayAccess(iteratedExpr,
                                        ExpressionUtil.nodeProp(whileNode, indexField))
                        );
                    });
        } else {
            var collType = (ClassType) iteratedExpr.getType();
            var itNode = builder().createSubFlow(
                    iteratedExpr, collType.getFlowByCode("iterator"),
                    List.of()
            );
            var itType = (ClassType) NncUtils.requireNonNull(itNode.getType());
            processLoop(statement, getExtraLoopTest(statement), (node, loopVar2Field) -> {
                node.setCondition(
                        new ExpressionValue(new FunctionExpression(Function.HAS_NEXT, new NodeExpression(itNode)))
                );
                var elementNode = builder().createSubFlow(
                        new NodeExpression(itNode),
                        itType.getFlowByCode("next"),
                        List.of()
                );
                builder().setVariable(statement.getIterationParameter().getName(), new NodeExpression(elementNode));
            });
        }
    }


    private @Nullable PsiExpression getExtraLoopTest(PsiForeachStatement statement) {
        var firstStmt = getFirstStatement(statement.getBody());
        if (isExtraLoopTest(firstStmt)) {
            return ((PsiMethodCallExpression) ((PsiExpressionStatement) firstStmt).getExpression())
                    .getArgumentList().getExpressions()[0];
        }
        return null;
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
        builder().createRaise(resolveExpression(statement.getException()));
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
