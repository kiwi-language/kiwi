package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.expression.Func;
import tech.metavm.object.type.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.util.CompilerConfig;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import tech.metavm.expression.Expression;
import tech.metavm.flow.Parameter;
import java.util.*;
import java.util.function.BiConsumer;

import tech.metavm.flow.Flow;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.getEnumConstantName;
import static tech.metavm.expression.Expressions.trueExpression;

public class Generator extends VisitorBase {

    private final LinkedList<MethodGenerator> builders = new LinkedList<>();
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
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        if(klass.getStage().isAfterOrAt(ResolutionStage.DEFINITION))
            return;
        klass.setStage(ResolutionStage.DEFINITION);
        klass.setCode(psiClass.getQualifiedName());

        var initFlow = klass.getMethodByCodeAndParamTypes("<init>", List.of());
        var initFlowBuilder = new MethodGenerator(initFlow, typeResolver, entityContext, this);
        initFlowBuilder.enterScope(initFlowBuilder.getMethod().getRootScope());
        initFlowBuilder.setVariable("this", new NodeExpression(initFlowBuilder.createSelf()));
        initFlowBuilder.createInput();

        var classInit = klass.getMethodByCodeAndParamTypes("<cinit>", List.of());
        var classInitFlowBuilder = new MethodGenerator(classInit, typeResolver, entityContext, this);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getMethod().getRootScope());
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
            var constructorGen = new MethodGenerator(constructor, typeResolver, entityContext, this);
            constructorGen.enterScope(constructor.getRootScope());
            constructorGen.createMethodCall(new NodeExpression(constructorGen.createSelf()), initFlow, List.of());
            constructorGen.createReturn(new NodeExpression(constructorGen.createSelf()));
            constructorGen.exitScope();
        }
        exitClass();
        klass.setStage(ResolutionStage.DEFINITION);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var field = NncUtils.requireNonNull(enumConstant.getUserData(Keys.FIELD));
        var builder = currentClassInfo().staticBuilder;
        var constructor = enumConstant.resolveConstructor();
        List<Type> paramTypes = new ArrayList<>();
        paramTypes.add(entityContext.getType(String.class));
        paramTypes.add(entityContext.getType(long.class));
        paramTypes.addAll(
                NncUtils.map(
                        requireNonNull(constructor).getParameterList().getParameters(),
                        param -> typeResolver.resolveTypeOnly(param.getType())
                )
        );
        List<Expression> args = new ArrayList<>();
        args.add(Expressions.constantString(getEnumConstantName(enumConstant)));
        args.add(Expressions.constantLong(currentClassInfo().nextEnumConstantOrdinal()));
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
            builder.createUpdate(builder.getVariable("this"), Map.of(field, Expressions.nullExpression()));
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
                                    Values.expression(trySectionOutput.get(raiseNode).get(outputVar.toString()))
                            )
                    ),
                    Values.expression(builder().getVariable(outputVar.toString())),
                    tryEndNode
            );
        }

        var exceptionExpr = new PropertyExpression(
                new NodeExpression(tryEndNode),
                tryEndNode.getType().getFieldByCode("exception")
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
                        Values.expression(
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
                                    Values.expression(
                                            branch.isPreselected() ? exceptionExprFinal : Expressions.nullExpression()
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
                Values.expression(new UnaryExpression(UnaryOperator.IS_NOT_NULL, exceptionExpr))
        );
        exceptBranchNode.addDefaultBranch();
        builder().enterCondSection(exceptBranchNode);
        builder().enterBranch(exceptionBranch);
        builder().createRaise(exceptionExpr);
        builder().exitBranch();
        builder().createMerge();
    }

    @Nullable
    private Set<QualifiedName> getBlockLiveOut(PsiCodeBlock block) {
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
                expression = new BinaryExpression(BinaryOperator.OR, expression, checkExpr);
            }
        }
        return NncUtils.requireNonNull(expression);
    }

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        if(CompilerConfig.isMethodBlacklisted(psiMethod))
            return;
        var method = NncUtils.requireNonNull(psiMethod.getUserData(Keys.Method));
        MethodGenerator builder = new MethodGenerator(method, typeResolver, entityContext, this);
        builders.push(builder);
        builder.enterScope(method.getRootScope());
        var selfNode = builder().createSelf();
        builder.setVariable("this", new NodeExpression(selfNode));
        processParameters(psiMethod.getParameterList(), method);
        if (psiMethod.isConstructor()) {
            var superClass = currentClass().getSuperClass();
            if (superClass != null && !isEnumType(superClass) && !isEntityType(superClass)
                    && !isSuperCallPresent(psiMethod)) {
                builder().createMethodCall(
                        builder().getVariable("this"),
                        superClass.getMethodByCodeAndParamTypes(superClass.getCodeRequired(), List.of()),
                        List.of()
                );
            }
            builder.createMethodCall(
                    new NodeExpression(selfNode),
                    currentClassInfo().fieldBuilder.getMethod(),
                    List.of()
            );
            if (currentClass().isEnum()) {
                var klass = currentClass();
                var inputNode = method.getInputNode();
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
        requireNonNull(psiMethod.getBody()).accept(this);
        if (psiMethod.isConstructor()) {
            builder.createReturn(new NodeExpression(method.getRootNode()));
        } else if (!requireNonNull(method.getRootScope().getLastNode()).isExit()) {
            builder.createReturn();
        }
        builder.exitScope();
        builders.pop();
    }

    private boolean isEnumType(ClassType classType) {
        return classType.getTemplate() != null && classType.getTemplate() == StandardTypes.getEnumType();
    }

    private boolean isEntityType(ClassType classType) {
        return classType == StandardTypes.getEntityType();
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
                            BinaryOperator.IN,
                            resolveExpression(statement.getExpression()),
                            ArrayExpression.create(expressions, new ContextArrayTypeProvider(entityContext))
                    );
                }
                branch = branchNode.addBranch(Values.expression(cond));
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
                    mergeField.setValue(branch, Values.expression(values.get(outputVar.toString())))
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
        var field = Objects.requireNonNull(inputNode.getType().findFieldByCode(parameter.getName()));
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

        var thenBranch = branchNode.addBranch(Values.expression(trueExpression()));
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
            var field = mergeNode.getType().getFieldByCode(qn.toString());
            var mergeField = new MergeNodeField(field, mergeNode);
            for (var entry2 : condOutputs.entrySet()) {
                var branch = entry2.getKey();
                var branchOutputs = entry2.getValue();
                mergeField.setValue(branch, Values.expression(branchOutputs.get(qn.toString())));
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
            condField = builder().newTemproryField(node.getType(), "循环条件", StandardTypes.getBooleanType());
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
            node.setField(field, Values.expression(initialValue), Values.expression(updatedValue));
            builder().setVariable(loopVar.toString(), new PropertyExpression(new NodeExpression(node), field));
        }
        if (condition != null) {
            node.setField(requireNonNull(condField),
                    Values.expression(requireNonNull(condInitialValue)),
                    Values.expression(requireNonNull(condUpdatedValue))
            );
            var extraCond = new PropertyExpression(new NodeExpression(node), condField);
            var finalCond = node.getCondition() == null ? extraCond :
                    Expressions.and(node.getCondition().getExpression(), extraCond);
            node.setCondition(Values.expression(finalCond));
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
//                        node.setCondition(Value.expression(resolveExpression(statement.getCondition())))
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
                                Values.expression(
                                        Expressions.lt(
                                                Expressions.nodeProperty(whileNode, indexField),
                                                new FunctionExpression(Func.LEN, iteratedExpr)
                                        )
                                )
                        );
                        whileNode.setField(indexField,
                                Values.expression(Expressions.constantLong(0L)),
                                Values.expression(
                                        Expressions.add(
                                                Expressions.nodeProperty(whileNode, indexField),
                                                Expressions.constantLong(1L)
                                        )
                                )
                        );
                        builder().setVariable(statement.getIterationParameter().getName(),
                                Expressions.arrayAccess(iteratedExpr,
                                        Expressions.nodeProperty(whileNode, indexField))
                        );
                    });
        } else {
            var collType = (ClassType) iteratedExpr.getType();
            typeResolver.ensureDeclared(collType);
            var itNode = builder().createMethodCall(
                    iteratedExpr, Objects.requireNonNull(collType.findMethodByCode("iterator")),
                    List.of()
            );
            var itType = (ClassType) NncUtils.requireNonNull(itNode.getType());
            processLoop(statement, getExtraLoopTest(statement), (node, loopVar2Field) -> {
                node.setCondition(
                        Values.expression(new FunctionExpression(Func.HAS_NEXT, new NodeExpression(itNode)))
                );
                var elementNode = builder().createMethodCall(
                        new NodeExpression(itNode),
                        Objects.requireNonNull(itType.findMethodByCode("next")),
                        List.of()
                );
                builder().setVariable(statement.getIterationParameter().getName(), new NodeExpression(elementNode));
            });
        }
    }


    @Nullable
    private PsiExpression getExtraLoopTest(PsiForeachStatement statement) {
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
        if (Flows.isConstructor(builder().getMethod())) {
            returnValue = new NodeExpression(builder().getMethod().getRootNode());
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

    private Expression resolveExpression(PsiExpression expression) {
        return builder().getExpressionResolver().resolve(expression);
    }

    private MethodGenerator builder() {
        return requireNonNull(builders.peek());
    }

    @SuppressWarnings("unused")
    public Map<String, ClassType> getClasses() {
        return classes;
    }

    private static final class ClassInfo {
        private final ClassType klass;
        private final PsiClass psiClass;
        private final MethodGenerator fieldBuilder;
        private final MethodGenerator staticBuilder;
        private int enumConstantCount;

        private ClassInfo(
                ClassType klass,
                PsiClass psiClass,
                MethodGenerator fieldBuilder,
                MethodGenerator staticBuilder
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
