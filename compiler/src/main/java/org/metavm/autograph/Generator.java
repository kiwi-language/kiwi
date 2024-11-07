package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.EntityIndex;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdKlass;
import org.metavm.expression.Expression;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.CompositeTypeEventRegistry;
import org.metavm.object.type.generic.CompositeTypeListener;
import org.metavm.util.CompilerConfig;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

public class Generator extends VisitorBase {

    public static final Logger logger = LoggerFactory.getLogger(Generator.class);

    private final PsiClass psiClass;
    private final LinkedList<MethodGenerator> builders = new LinkedList<>();
    private final Map<String, Klass> classes = new HashMap<>();
    private final LinkedList<ClassInfo> classInfoStack = new LinkedList<>();
    private final TypeResolver typeResolver;
    private final IEntityContext entityContext;

    public Generator(PsiClass psiClass, TypeResolver typeResolver, IEntityContext entityContext) {
        this.psiClass = psiClass;
        this.typeResolver = typeResolver;
        this.entityContext = entityContext;
    }

    @Override
    public void visitTypeParameter(PsiTypeParameter classParameter) {
    }

    @Override
    public void visitClass(PsiClass psiClass) {
        if(TranspileUtils.isDiscarded(psiClass))
            return;
        if (TranspileUtils.getAnnotation(psiClass, EntityIndex.class) != null)
            return;
        if(psiClass != this.psiClass)
            return;
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        if (klass.getStage().isAfterOrAt(ResolutionStage.DEFINITION))
            return;
        klass.setStage(ResolutionStage.DEFINITION);
        klass.setCode(psiClass.getQualifiedName());

        MethodGenerator initFlowBuilder;
        Method initFlow;
        if(!psiClass.isInterface()) {
            initFlow = klass.getMethodByCodeAndParamTypes("<init>", List.of());
            initFlowBuilder = new MethodGenerator(initFlow, typeResolver, this);
            initFlowBuilder.enterScope(initFlowBuilder.getMethod().getScope());
            if (klass.getSuperType() != null) {
                var superInit = klass.getSuperType().resolve().findSelfMethodByCode("<init>");
                if (superInit != null) {
                    initFlowBuilder.createMethodCall(
                            initFlowBuilder.getThis(),
                            superInit,
                            List.of()
                    );
                }
            }
        }
        else {
            initFlowBuilder = null;
            initFlow = null;
        }
        var classInit = klass.getMethodByCodeAndParamTypes("<cinit>", List.of());
        var classInitFlowBuilder = new MethodGenerator(classInit, typeResolver, this);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getMethod().getScope());
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

        if(initFlowBuilder != null) {
            initFlowBuilder.createReturn();
            initFlowBuilder.exitScope();
        }

        classInitFlowBuilder.createReturn();
        classInitFlowBuilder.exitScope();

        if(!psiClass.isInterface()) {
            boolean hasConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
            if (!hasConstructor) {
                var constructor = klass.getDefaultConstructor();
                var constructorGen = new MethodGenerator(constructor, typeResolver, this);
                constructorGen.enterScope(constructor.getScope());
                constructorGen.createMethodCall(constructorGen.getThis(), initFlow, List.of());
                constructorGen.createReturn(constructorGen.getThis());
                constructorGen.exitScope();
            }
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
                expr -> builder.getExpressionResolver().resolve(expr)
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
                index = new Index(type, psiField.getName(), psiField.getName(), "", unique, fields, null);
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
                builder.createUpdate(builder.getThis(), Map.of(field, initializer));
            }
        } else if (field.getType().isNullable()) {
            initField(field, Values.nullValue());
        } else if (field.getType().isBoolean()) {
            initField(field, Values.constantBoolean(false));
        } else if (field.getType().isLong()) {
            initField(field, Values.constantLong(0L));
        } else if (field.getType().isDouble()) {
            initField(field, Values.constantDouble(0.0));
        }
    }

    private void initField(Field field, Value initializer) {
        if (field.isStatic()) {
            var builder = currentClassInfo().staticBuilder;
            builder.createUpdateStatic(currentClass(), Map.of(field, initializer));
        } else {
            var builder = currentClassInfo().fieldBuilder;
            builder.createUpdate(builder.getThis(), Map.of(field, initializer));
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
        var tryEnter = builder().createTryEnter();
        builder().enterTrySection(tryEnter);
        tryBlock.accept(this);
        var trySectionOutput = builder().exitTrySection(tryEnter, NncUtils.map(outputVars, Objects::toString));
        var tryExit = builder().createTryExit();
        var exceptionExpr = Values.node(Nodes.getProperty(
                Values.node(tryExit),
                tryExit.getKlass().getFieldByCode("exception"),
                builder().scope()
        ));

        if (statement.getCatchSections().length > 0) {
            Set<QualifiedName> catchModified = new HashSet<>();
            Set<QualifiedName> catchLiveOut = null;
            var entryNode = builder().createNoop();
            builder().enterCondSection(entryNode);
            JumpNode lastIfNode = null;
            long branchIdx = 0;
            var exits = new HashMap<Long, NodeRT>();
            for (PsiCatchSection catchSection : statement.getCatchSections()) {
                var catchBlock = NncUtils.requireNonNull(catchSection.getCatchBlock());
                catchModified.addAll(requireNonNull(catchSection.getUserData(Keys.BODY_SCOPE)).getModified());
                if (catchLiveOut == null) {
                    catchLiveOut = getBlockLiveOut(catchBlock);
                }
                builder().enterBranch(entryNode, branchIdx);
                var ifNode = createExceptionCheck(exceptionExpr, catchSection.getCatchType());
                if(lastIfNode != null)
                    lastIfNode.setTarget(ifNode);
                lastIfNode = ifNode;
                var param = requireNonNull(catchSection.getParameter());
                builder().defineVariable(param.getName());
                builder().createStore(TranspileUtils.getVariableIndex(param), exceptionExpr);
                catchBlock.accept(this);
                exits.put(branchIdx++, builder().createGoto(null));
            }
            builder().enterBranch(entryNode, branchIdx);
            var defaultExit = builder().createNoop();
            Objects.requireNonNull(lastIfNode).setTarget(defaultExit);
            exits.put(branchIdx, defaultExit);
            List<QualifiedName> catchOutputVars = catchLiveOut == null ? List.of()
                    : new ArrayList<>(NncUtils.intersect(catchModified, catchLiveOut));
            var joinNode = builder().createJoin();
            for (NodeRT exit : exits.values()) {
                if(exit instanceof GotoNode g)
                    g.setTarget(joinNode);
            }
            exitCondSection(entryNode, joinNode, exits, catchOutputVars);
            var exceptionField = FieldBuilder.newBuilder("exception", "exception",
                    joinNode.getKlass(), Types.getNullableThrowableType()).build();

            final var exceptionExprFinal = exceptionExpr;
            new JoinNodeField(
                    exceptionField,
                    joinNode,
                    NncUtils.toMap(
                            exits.values(),
                            java.util.function.Function.identity(),
                            exit -> exit == defaultExit ? exceptionExprFinal : Values.nullValue()
                    )
            );
            exceptionExpr = Values.node(builder().createNodeProperty(joinNode, exceptionField));
        }
        if (statement.getFinallyBlock() != null) {
            statement.getFinallyBlock().accept(this);
        }
        var ifNode = builder().createIf(
                Values.node(builder().createEq(exceptionExpr, Values.nullValue())),
                null);
        builder().createRaise(exceptionExpr);
        ifNode.setTarget(builder().createNoop());
    }

    @Nullable
    private Set<QualifiedName> getBlockLiveOut(PsiCodeBlock block) {
        if (block.isEmpty()) {
            return null;
        }
        var lastStmt = block.getStatements()[block.getStatementCount() - 1];
        return NncUtils.requireNonNull(lastStmt.getUserData(Keys.LIVE_VARS_OUT));
    }

    private List<JumpNode> createExceptionCheck(Value exceptionExpr, List<PsiType> catchTypes) {
        NncUtils.requireNotEmpty(catchTypes);
        var jumps = new ArrayList<JumpNode>();
        for (PsiType catchType : catchTypes) {
            var ifNode = builder().createIfNot(
                    Values.node(builder().createInstanceOf(exceptionExpr, resolveType(catchType))),
                    null
            );
            jumps.add(ifNode);
        }
        return jumps;
    }

    private JumpNode createExceptionCheck(Value exceptionExpr, PsiType catchType) {
        return builder().createIfNot(
                Values.node(builder().createInstanceOf(exceptionExpr, resolveType(catchType))),
                null
        );
    }

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        if (CompilerConfig.isMethodBlacklisted(psiMethod))
            return;
        if(TranspileUtils.isAbstract(psiMethod))
            return;
//        logger.debug("Generating code for method {}", TranspileUtils.getMethodQualifiedName(psiMethod));
        var method = NncUtils.requireNonNull(psiMethod.getUserData(Keys.Method));
        method.getScope().setMaxLocals(TranspileUtils.getMaxLocals(psiMethod));
        method.setLambdas(List.of());
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
        builder.enterScope(method.getScope());
        if (TranspileUtils.isStatic(psiMethod)) {
            processParameters(psiMethod.getParameterList(), method);
        } else {
            processParameters(psiMethod.getParameterList(), method);
            if (psiMethod.isConstructor()) {
                var superClass = NncUtils.get(currentClass().getSuperType(), ClassType::resolve);
                if (superClass != null && !isEnumType(superClass) && !isEntityType(superClass)
                        && !isRecordType(superClass) && !isSuperCallPresent(psiMethod)) {
                    builder().createMethodCall(
                            builder().getThis(),
                            superClass.getDefaultConstructor(),
                            List.of()
                    );
                }
                builder.createMethodCall(
                        builder.getThis(),
                        currentClassInfo().fieldBuilder.getMethod(),
                        List.of()
                );
                if (currentClass().isEnum()) {
                    var klass = currentClass();
                    var enumClass = requireNonNull(klass.getSuperType()).resolve();
                    builder.createUpdate(
                            builder.getThis(),
                            Map.of(
                                    enumClass.getFieldByCode("name"),
                                    Values.node(builder.createLoad(1, Types.getStringType())),
                                    enumClass.getFieldByCode("ordinal"),
                                    Values.node(builder.createLoad(2, Types.getLongType()))
                            )
                    );
                }
            }
        }
        requireNonNull(psiMethod.getBody(), "body is missing from method " +
                TranspileUtils.getMethodQualifiedName(psiMethod)).accept(this);
        NodeRT lastNode;
        if (psiMethod.isConstructor()) {
            builder.createReturn(Values.node(method.getRootNode()));
        } else if (method.getReturnType().isVoid() &&
                ((lastNode = method.getScope().getLastNode()) == null || !lastNode.isExit())) {
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
        var stmts = requireNonNull(method.getBody(),
                () -> "Failed to get body of method " + TranspileUtils.getMethodQualifiedName(method))
                .getStatements();
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
        var entryNode = builder().createValue("switchValue", resolveExpression(statement.getExpression()));
        var switchExpr = Values.node(entryNode);
        var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
        builder().enterCondSection(entryNode);
        var modified = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE)).getModified();
        var liveVarOut = NncUtils.requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        var outputVars = NncUtils.intersect(modified, liveVarOut);
        var exits = new HashMap<Long, NodeRT>();
        List<? extends JumpNode> lastIfNodes = List.of();
        GotoNode lastGoto = null;
        var branchIdx = 0L;
        for (PsiStatement stmt : stmts) {
            var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
            var caseLabelElementList = labeledRuleStmt.getCaseLabelElementList();
            if (caseLabelElementList == null || caseLabelElementList.getElementCount() == 0)
                continue;
            PsiVariable castVar = null;
            List<IfNotNode> ifNodes;
            if (isTypePatternCase(caseLabelElementList)) {
                PsiTypeTestPattern typeTestPattern = (PsiTypeTestPattern) caseLabelElementList.getElements()[0];
                var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
                builder().createStore(
                        TranspileUtils.getVariableIndex(requireNonNull(typeTestPattern.getPatternVariable())),
                        switchExpr
                );
                ifNodes = List.of(builder().createIfNot(
                        Values.node(builder().createInstanceOf(switchExpr, typeResolver.resolveDeclaration(checkType))),
                        null
                ));
                castVar = requireNonNull(typeTestPattern.getPatternVariable());
            } else {
                var expressions = NncUtils.map(caseLabelElementList.getElements(),
                        e -> resolveExpression((PsiExpression) e));
                ifNodes = new ArrayList<>();
                for (var expression : expressions) {
                    ifNodes.add(builder().createIfNot(
                            Values.node(builder().createEq(switchExpr, expression)),
                            null
                    ));
                }
            }
            builder().enterBranch(entryNode, branchIdx);
            if(lastGoto != null) {
                for (JumpNode lastIfNode : lastIfNodes) {
                    lastIfNode.setTarget(lastGoto.getSuccessor());
                }
            }
            lastIfNodes = ifNodes;
            if (castVar != null) {
                builder().createStore(TranspileUtils.getVariableIndex(castVar), switchExpr);
            }
            processSwitchCaseBody(labeledRuleStmt.getBody());
            exits.put(branchIdx++, lastGoto = builder().createGoto(null));
        }
        var defaultStmt = (PsiSwitchLabeledRuleStatement)
                NncUtils.findRequired(stmts, stmt -> ((PsiSwitchLabeledRuleStatement) stmt).isDefaultCase());
        builder().enterBranch(entryNode, branchIdx);
        var noop = builder().createNoop();
        lastIfNodes.forEach(n -> n.setTarget(noop));
        processSwitchCaseBody(defaultStmt.getBody());
        exits.put(branchIdx, requireNonNull(builder().scope().getLastNode()));
        var joinNode = builder().createJoin();
        exits.values().forEach(exit -> {
            if(exit instanceof GotoNode g)
                g.setTarget(joinNode);
        });
        var condOutputs = builder().exitCondSection(entryNode, joinNode, exits, true);
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(), joinNode.getKlass(),
                            typeResolver.resolveDeclaration(outputVar.type()))
                    .build();
            var joinField = new JoinNodeField(field, joinNode);
            condOutputs.forEach((idx, values) ->
                    joinField.setValue(requireNonNull(exits.get(idx)), values.get(outputVar.toString()))
            );
        }
    }

    private void processSwitchCaseBody(PsiElement element) {
        if (element != null) {
            element.accept(this);
            if (element instanceof PsiExpression bodyExpression) {
                builder().setYield(resolveExpression(bodyExpression));
            }
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
    }

    private Klass currentClass() {
        return requireNonNull(classInfoStack.peek()).klass;
    }

    private ClassInfo currentClassInfo() {
        return requireNonNull(classInfoStack.peek());
    }

    private Type resolveType(PsiType type) {
        return typeResolver.resolveDeclaration(type);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        Scope bodyScope = requireNonNull(statement.getUserData(Keys.BODY_SCOPE)),
                elseScope = requireNonNull(statement.getUserData(Keys.ELSE_SCOPE));
        Set<QualifiedName> modified = NncUtils.union(bodyScope.getModified(), elseScope.getModified());
        Set<QualifiedName> outputVars = getBlockOutputVariables(statement, modified);
        List<String> outputVariables = NncUtils.map(outputVars, Object::toString);
        builder().getExpressionResolver().constructIf(
                requireNonNull(statement.getCondition()),
                () -> {
                    if (statement.getThenBranch() != null) {
                        statement.getThenBranch().accept(this);
                    }
                },
                () -> {
                    if (statement.getElseBranch() != null) {
                        statement.getElseBranch().accept(this);
                    }
                }
        );
    }

    private void exitCondSection(NodeRT sectionId, JoinNode joinNode, Map<Long, NodeRT> exits, List<QualifiedName> outputVariables) {
        builder().exitCondSection(sectionId, joinNode, exits, false);
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        builder().defineVariable(variable.getName());
        if (variable.getInitializer() != null) {
            builder().createStore(TranspileUtils.getVariableIndex(variable),
                    resolveExpression(variable.getInitializer()));
        }
    }

    private void processLoop(PsiLoopStatement statement,
                             PsiExpression condition,
                             @Nullable BiFunction<JoinNode, Map<QualifiedName, Field>, JumpNode> preprocessor,
                             @Nullable java.util.function.Function<JoinNode, LoopField> postProcessor
                             ) {
        var entryNode = builder().createNoop();
        var joinNode = builder().createJoin();
        var bodyScope = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
        var modified = new HashSet<>(bodyScope.getModified());
        var condScope = statement.getUserData(Keys.COND_SCOPE);
        if(condScope != null)
            modified.addAll(condScope.getModified());
        Set<QualifiedName> liveIn = requireNonNull(statement.getUserData(Keys.LIVE_VARS_IN));
        Set<QualifiedName> liveOut = requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        List<QualifiedName> loopVars = NncUtils.filter(
                modified, qn -> liveIn.contains(qn) || liveOut.contains(qn)
        );
        Map<Field, Value> initialValues = new HashMap<>();
        Map<QualifiedName, Field> loopVar2Field = new HashMap<>();
        var cond = condition != null ? resolveExpression(condition) : Values.constantTrue();
        var ifNode = builder().createIfNot(cond, null);
        JumpNode extraIfNode = null;
        if (preprocessor != null)
            extraIfNode = preprocessor.apply(joinNode, loopVar2Field);
        if (statement.getBody() != null) {
            statement.getBody().accept(this);
        }
        LoopField extraLoopField = null;
        if(postProcessor != null)
            extraLoopField = postProcessor.apply(joinNode);
        var goBack = builder().createGoto(joinNode);
        var exit = builder().createNoop();
        ifNode.setTarget(exit);
        if(extraIfNode != null)
            extraIfNode.setTarget(exit);
        if(extraLoopField != null) {
            new JoinNodeField(extraLoopField.field(), joinNode, Map.of(
                            entryNode, extraLoopField.initialValue,
                            goBack, extraLoopField.updatedValue
            ));
        }
    }

    private record LoopField(Field field, Value initialValue, Value updatedValue) {
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        throw new InternalException("For loop should be transformed into while loop before code generation");
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        processLoop(statement, statement.getCondition(), null, null
//                loopVar2Field ->
//                        node.setCondition(Value.expression(resolveExpression(statement.getCondition())))
        );
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        var iteratedExpr0 = resolveExpression(statement.getIteratedValue());
        Value iteratedExpr;
        if(builder().getExpressionType(iteratedExpr0.getExpression()).isNullable())
            iteratedExpr = Values.node(builder().createNonNull("nonNull", iteratedExpr0));
        else
            iteratedExpr = iteratedExpr0;
        var type = builder().getExpressionType(iteratedExpr.getExpression());
        if (type instanceof ArrayType) {
            processLoop(statement, getExtraLoopTest(statement),
                    (joinNode, loopVar2Field) -> {
                        var indexField = FieldBuilder
                                .newBuilder("index", "index", joinNode.getKlass(), Types.getLongType())
                                .build();
                        var index = builder().createNodeProperty(joinNode, indexField);
                        var ifNode = builder().createIf(Values.node(builder().createGe(
                                Values.node(index),
                                Values.node(builder().createArrayLength(iteratedExpr))
                        )), null);
                        builder().createStore(statement.getIterationParameter(),
                                Values.node(builder().createGetElement(iteratedExpr, Values.node(index))));
                        return ifNode;
                    },
                    joinNode -> {
                        var indexField = joinNode.getKlass().getFieldByCode("index");
                        return new LoopField(
                            indexField,
                                Values.constantLong(0L),
                                Values.node(
                                        builder().createAdd(
                                                Values.node(
                                                        builder().createNodeProperty(joinNode, indexField)
                                                ),
                                                Values.constantLong(1L)
                                        )
                                )
                        );
                    }
                );
        } else {
            var collType = Types.resolveKlass(type);
            typeResolver.ensureDeclared(collType);
            var itNode = builder().createNonNull("nonNull",
                    Values.node(builder().createMethodCall(
                        iteratedExpr, Objects.requireNonNull(collType.findMethodByCode("iterator")),
                        List.of()))
            );
            var itType = Types.resolveKlass(NncUtils.requireNonNull(itNode.getType()));
            processLoop(statement, getExtraLoopTest(statement), (joinNode, loopVar2Field) -> {
                var hashNext = builder().createMethodCall(
                        Values.node(itNode),
                        itType.getMethod("hasNext", List.of()),
                        List.of()
                );
                var ifNode = builder().createIfNot(
                        Values.node(hashNext),
                        null
                );
                var elementNode = builder().createMethodCall(
                        Values.node(itNode),
                        itType.getMethod("next", List.of()),
                        List.of()
                );
                builder().createStore(statement.getIterationParameter(), Values.node(elementNode));
                return ifNode;
            }, null);
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
        Value returnValue;
        if (Flows.isConstructor(builder().getMethod())) {
            returnValue = Values.node(builder().getMethod().getRootNode());
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

    private Value resolveExpression(PsiExpression expression) {
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
