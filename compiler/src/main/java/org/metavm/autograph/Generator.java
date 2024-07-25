package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.EntityIndex;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdKlass;
import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.CompositeTypeEventRegistry;
import org.metavm.object.type.generic.CompositeTypeListener;
import org.metavm.util.CompilerConfig;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static org.metavm.expression.Expressions.trueExpression;

public class Generator extends CodeGenVisitor {

    private final LinkedList<MethodGenerator> builders = new LinkedList<>();
    private final Map<String, Klass> classes = new HashMap<>();
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
        if (TranspileUtils.getAnnotation(psiClass, EntityIndex.class) != null)
            return;
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        if (klass.getStage().isAfterOrAt(ResolutionStage.DEFINITION))
            return;
        klass.setStage(ResolutionStage.DEFINITION);
        klass.setCode(psiClass.getQualifiedName());

        var initFlow = klass.getMethodByCodeAndParamTypes("<init>", List.of());
        var initFlowBuilder = new MethodGenerator(initFlow, typeResolver, this);
        initFlowBuilder.enterScope(initFlowBuilder.getMethod().getRootScope());
        initFlowBuilder.setVariable("this", new NodeExpression(initFlowBuilder.createSelf()));
        initFlowBuilder.createInput();
        if (klass.getSuperType() != null) {
            var superInit = klass.getSuperType().resolve().findSelfMethodByCode("<init>");
            if (superInit != null) {
                initFlowBuilder.createMethodCall(
                        initFlowBuilder.getVariable("this"),
                        superInit,
                        List.of()
                );
            }
        }
        var classInit = klass.getMethodByCodeAndParamTypes("<cinit>", List.of());
        var classInitFlowBuilder = new MethodGenerator(classInit, typeResolver, this);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getMethod().getRootScope());
        classInitFlowBuilder.createInput();
        if (klass.getSuperType() != null) {
            var superCInit = klass.getSuperType().resolve().findSelfMethodByCode("<cinit>");
            if (superCInit != null) {
                classInitFlowBuilder.createMethodCall(
                        null,
                        superCInit,
                        List.of()
                );
            }
        }
        enterClass(new ClassInfo(klass, psiClass, initFlowBuilder, classInitFlowBuilder));

        super.visitClass(psiClass);

        initFlowBuilder.createReturn();
        initFlowBuilder.exitScope();

        classInitFlowBuilder.createReturn();
        classInitFlowBuilder.exitScope();

        boolean hasConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
        if (!hasConstructor) {
            var constructor = klass.getDefaultConstructor();
            var constructorGen = new MethodGenerator(constructor, typeResolver, this);
            constructorGen.enterScope(constructor.getRootScope());
            constructorGen.createMethodCall(new NodeExpression(constructorGen.createSelf()), initFlow, List.of());
            constructorGen.createReturn(new NodeExpression(constructorGen.createSelf()));
            constructorGen.exitScope();
        }
        if(klass.isEnum())
            Flows.generateValuesMethodBody(klass);
        exitClass();
        klass.setStage(ResolutionStage.DEFINITION);
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var ecd = Objects.requireNonNull(enumConstant.getUserData(Keys.ENUM_CONSTANT_DEF));
        var builder = currentClassInfo().staticBuilder;
        ecd.setArguments(NncUtils.map(
                requireNonNull(enumConstant.getArgumentList()).getExpressions(),
                expr -> Values.expression(builder.getExpressionResolver().resolve(expr))
        ));
    }

    @Override
    public void visitField(PsiField psiField) {
        if (TranspileUtils.isIndexDefField(psiField)) {
            var initializer = (PsiMethodCallExpression) requireNonNull(psiField.getInitializer());
            var arguments = initializer.getArgumentList().getExpressions();
            var fields = new ArrayList<Field>();
            var type = currentClass();
            var fieldNames = requireNonNull(requireNonNull((PsiNewExpression) arguments[1]).getArrayInitializer())
                    .getInitializers();
            for (PsiExpression fieldName : fieldNames) {
                var fieldCode = (String) ((PsiLiteralExpression) fieldName).getValue();
                fields.add(type.getFieldByCode(fieldCode));
            }
            var method = (PsiMethod) requireNonNull(initializer.getMethodExpression().resolve());
            var unique = method.getName().equals("createUnique");
            var index = (Index) NncUtils.find(type.getConstraints(), c -> c instanceof Index idx &&
                    Objects.equals(idx.getCode(), psiField.getName()));
            if (index == null)
                index = new Index(type, psiField.getName(), psiField.getName(), "", unique, fields);
            var code2IndexField = new HashMap<String, IndexField>();
            index.getFields().forEach(f -> code2IndexField.put(f.getCode(), f));
            var indexFields = new ArrayList<IndexField>();
            for (Field field : fields) {
                var indexField = code2IndexField.get(field.getCode());
                if (indexField == null)
                    indexField = IndexField.createFieldItem(index, field);
                indexFields.add(indexField);
            }
            index.setFields(indexFields);
            return;
        }
//        An NPE may occur here for record fields if modifications are made between declaration stage and generation stage.
//        To track these modifications, set a breakpoint at com.intellij.openapi.util.SimpleModificationTracker.incModificationCount.
        var field = Objects.requireNonNull(psiField.getUserData(Keys.FIELD));
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
        } else if (field.getType().isNullable()) {
            initField(field, Expressions.nullExpression());
        } else if (field.getType().isBoolean()) {
            initField(field, Expressions.constantBoolean(false));
        } else if (field.getType().isLong()) {
            initField(field, Expressions.constantLong(0L));
        } else if (field.getType().isDouble()) {
            initField(field, Expressions.constantDouble(0.0));
        }
    }

    private void initField(Field field, Expression initializer) {
        if (field.isStatic()) {
            var builder = currentClassInfo().staticBuilder;
            builder.createUpdateStatic(currentClass(), Map.of(field, initializer));
        } else {
            var builder = currentClassInfo().fieldBuilder;
            builder.createUpdate(builder.getVariable("this"), Map.of(field, initializer));
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
                            tryEndNode.getKlass(), resolveType(outputVar.type()))
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
                tryEndNode.getKlass().getFieldByCode("exception").getRef()
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
            exitCondSection(mergeNode, catchOutputVars);

            var exceptionField = FieldBuilder.newBuilder("exception", "exception",
                    mergeNode.getKlass(), Types.getNullableThrowableType()).build();

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
            exceptionExpr = new PropertyExpression(new NodeExpression(mergeNode), exceptionField.getRef());
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
        if (CompilerConfig.isMethodBlacklisted(psiMethod))
            return;
        var method = NncUtils.requireNonNull(psiMethod.getUserData(Keys.Method));
        var capturedTypeListener = new CompositeTypeListener() {
            @Override
            public void onTypeCreated(Type type) {
                if (type.isCaptured())
                    method.addCapturedCompositeType(type);
            }

            @Override
            public void onFlowCreated(Flow flow) {
                if (NncUtils.anyMatch(flow.getTypeArguments(), Type::isCaptured))
                    method.addCapturedFlow(flow);
            }
        };
        CompositeTypeEventRegistry.addListener(capturedTypeListener);
        method.clearContent();
        MethodGenerator builder = new MethodGenerator(method, typeResolver, this);
        builders.push(builder);
        builder.enterScope(method.getRootScope());
        if (TranspileUtils.isStatic(psiMethod)) {
            processParameters(psiMethod.getParameterList(), method);
        } else {
            var selfNode = builder().createSelf();
            builder.setVariable("this", new NodeExpression(selfNode));
            processParameters(psiMethod.getParameterList(), method);
            if (psiMethod.isConstructor()) {
                var superClass = NncUtils.get(currentClass().getSuperType(), ClassType::resolve);
                if (superClass != null && !isEnumType(superClass) && !isEntityType(superClass)
                        && !isRecordType(superClass) && !isSuperCallPresent(psiMethod)) {
                    builder().createMethodCall(
                            builder().getVariable("this"),
                            superClass.getDefaultConstructor(),
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
                    var enumClass = requireNonNull(klass.getSuperType()).resolve();
                    var inputNode = method.getInputNode();
                    builder.createUpdate(
                            new NodeExpression(selfNode),
                            Map.of(
                                    enumClass.getFieldByCode("name"),
                                    new PropertyExpression(
                                            new NodeExpression(inputNode),
                                            inputNode.getKlass().getFieldByCode("__name__").getRef()
                                    ),
                                    enumClass.getFieldByCode("ordinal"),
                                    new PropertyExpression(
                                            new NodeExpression(inputNode),
                                            inputNode.getKlass().getFieldByCode("__ordinal__").getRef()
                                    )
                            )
                    );
                }
            }
        }
        requireNonNull(psiMethod.getBody()).accept(this);
        if (psiMethod.isConstructor()) {
            builder.createReturn(new NodeExpression(method.getRootNode()));
        } else if (method.getReturnType().isVoid() && !requireNonNull(method.getRootScope().getLastNode()).isExit()) {
            builder.createReturn();
        }
        builder.exitScope();
        builders.pop();
        CompositeTypeEventRegistry.removeListener(capturedTypeListener);
    }

    private boolean isEnumType(Klass classType) {
        return classType.getTemplate() != null && classType.getTemplate() == StdKlass.enum_.get();
    }

    private boolean isEntityType(Klass classType) {
        return classType == StdKlass.entity.get();
    }

    private boolean isRecordType(Klass classType) {
        return classType == StdKlass.record.get();
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
            String castVar = null;
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
                    castVar = requireNonNull(typeTestPattern.getPatternVariable()).getName();
                } else {
                    var expressions = NncUtils.map(caseLabelElementList.getElements(),
                            e -> resolveExpression((PsiExpression) e));
                    cond = null;
                    for (Expression expression : expressions) {
                        var expr = new BinaryExpression(BinaryOperator.EQ, switchExpr, expression);
                        if (cond == null)
                            cond = expr;
                        else
                            cond = new BinaryExpression(BinaryOperator.OR, cond, expr);
                    }
                    requireNonNull(cond);
                }
                branch = branchNode.addBranch(Values.expression(cond));
            } else {
                branch = branchNode.addDefaultBranch();
            }
            builder().enterBranch(branch);
            if (castVar != null) {
                builder().setVariable(castVar, switchExpr);
            }
            if (labeledRuleStmt.getBody() != null) {
                labeledRuleStmt.getBody().accept(this);
                if (labeledRuleStmt.getBody() instanceof PsiExpression bodyExpression) {
                    builder().setYield(resolveExpression(bodyExpression));
                }
            }
            builder().exitBranch();
        }
        var mergeNode = builder().createMerge();
        var condOutputs = builder().exitCondSection(mergeNode, NncUtils.map(outputVars, Objects::toString));
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(), mergeNode.getKlass(),
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
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputNode.getKlass(),
                            Types.tryCapture(parameter.getType(), flow, parameter))
                    .build();
        }
        for (PsiParameter parameter : parameterList.getParameters()) {
            processParameter(parameter, inputNode);
        }
    }

    private Klass currentClass() {
        return requireNonNull(classInfoStack.peek()).klass;
    }

    private ClassInfo currentClassInfo() {
        return requireNonNull(classInfoStack.peek());
    }

    private void processParameter(PsiParameter parameter, InputNode inputNode) {
        var field = Objects.requireNonNull(inputNode.getKlass().findFieldByCode(parameter.getName()));
        builder().setVariable(
                parameter.getName(),
                new PropertyExpression(new NodeExpression(inputNode), field.getRef())
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
        List<QualifiedName> outputVariables = new ArrayList<>(outputVars);
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
        var outputVars = NncUtils.map(outputVariables, Objects::toString);
        var condOutputs = builder().exitCondSection(mergeNode, outputVars);
        for (var qn : outputVariables) {
            var branch2value = new HashMap<Branch, Value>();
            for (var entry2 : condOutputs.entrySet()) {
                var branch = entry2.getKey();
                var branchOutputs = entry2.getValue();
                branch2value.put(branch, Values.expression(branchOutputs.get(qn.toString())));
            }
            var memberTypes = new HashSet<Type>();
            for (var value : branch2value.values()) {
                if (NncUtils.noneMatch(memberTypes, t -> t.isAssignableFrom(value.getType())))
                    memberTypes.add(value.getType());
            }
            var fieldType =
                    memberTypes.size() == 1 ? memberTypes.iterator().next() : new UnionType(memberTypes);
            var field = FieldBuilder.newBuilder(qn.toString(), qn.toString(), mergeNode.getKlass(), fieldType).build();
            var mergeField = new MergeNodeField(field, mergeNode, branch2value);
            builder().setVariable(qn.toString(), new PropertyExpression(new NodeExpression(mergeNode), mergeField.getField().getRef()));
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
        if (condition != null) {
            condField = builder().newTemproryField(node.getKlass(), "condition", Types.getBooleanType());
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
                    node.getKlass(),
                    loopVar.toString(),
                    resolveType(loopVar.type())
            );
            loopVar2Field.put(loopVar, field);
            initialValues.put(field, builder().getVariable(loopVar.toString()));
        }
        for (QualifiedName loopVar : loopVar2Field.keySet()) {
            var field = loopVar2Field.get(loopVar);
            builder().setVariable(loopVar.toString(), new PropertyExpression(new NodeExpression(node), field.getRef()));
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
            builder().setVariable(loopVar.toString(), new PropertyExpression(new NodeExpression(node), field.getRef()));
        }
        if (condition != null) {
            node.setField(requireNonNull(condField),
                    Values.expression(requireNonNull(condInitialValue)),
                    Values.expression(requireNonNull(condUpdatedValue))
            );
            var extraCond = new PropertyExpression(new NodeExpression(node), condField.getRef());
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
                                .newBuilder("index", "index", whileNode.getKlass(), Types.getLongType())
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
            var collType = Types.resolveKlass(iteratedExpr.getType());
            typeResolver.ensureDeclared(collType);
            var itNode = builder().createMethodCall(
                    iteratedExpr, Objects.requireNonNull(collType.findMethodByCode("iterator")),
                    List.of()
            );
            var itType = Types.resolveKlass(NncUtils.requireNonNull(itNode.getType()));
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
        builder().setYield(resolveExpression(statement.getExpression()));
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
    public Map<String, Klass> getClasses() {
        return classes;
    }

    private static final class ClassInfo {
        private final Klass klass;
        private final PsiClass psiClass;
        private final MethodGenerator fieldBuilder;
        private final MethodGenerator staticBuilder;
        private int enumConstantCount;

        private ClassInfo(
                Klass klass,
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
