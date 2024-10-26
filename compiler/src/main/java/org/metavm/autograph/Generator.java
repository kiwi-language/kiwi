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
import org.metavm.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Generator extends CodeGenVisitor {

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
        }
        else {
            initFlowBuilder = null;
            initFlow = null;
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
                constructorGen.enterScope(constructor.getRootScope());
                constructorGen.createMethodCall(new NodeExpression(constructorGen.createSelf()), initFlow, List.of());
                constructorGen.createReturn(new NodeExpression(constructorGen.createSelf()));
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
        var tryEnter = builder().createTryEnter();
        builder().enterTrySection(tryEnter);
        tryBlock.accept(this);
        var trySectionOutput = builder().exitTrySection(tryEnter, NncUtils.map(outputVars, Objects::toString));
        var tryExit = builder().createTryExit();
        for (QualifiedName outputVar : outputVars) {
            var field = FieldBuilder.newBuilder(outputVar.toString(), outputVar.toString(),
                            tryExit.getKlass(), resolveType(outputVar.type()))
                    .build();
            new TryExitField(
                    field,
                    NncUtils.map(
                            trySectionOutput.keySet(),
                            raiseNode -> new TryExitValue(
                                    raiseNode,
                                    Values.expressionOrNever(trySectionOutput.get(raiseNode).get(outputVar.toString()))
                            )
                    ),
                    Values.expressionOrNever(builder().getVariable(outputVar.toString())),
                    tryExit
            );
            builder().setVariable(outputVar.toString(), Expressions.nodeProperty(tryExit, field));
        }

        var exceptionExpr = new PropertyExpression(
                new NodeExpression(tryExit),
                tryExit.getKlass().getFieldByCode("exception").getRef()
        );

        if (statement.getCatchSections().length > 0) {
            Set<QualifiedName> catchModified = new HashSet<>();
            Set<QualifiedName> catchLiveOut = null;
            var entryNode = builder().createNoop();
            builder().enterCondSection(entryNode);
            IfNode lastIfNode = null;
            long branchIdx = 0;
            var exits = new HashMap<Long, NodeRT>();
            for (PsiCatchSection catchSection : statement.getCatchSections()) {
                var catchBlock = NncUtils.requireNonNull(catchSection.getCatchBlock());
                catchModified.addAll(requireNonNull(catchSection.getUserData(Keys.BODY_SCOPE)).getModified());
                if (catchLiveOut == null) {
                    catchLiveOut = getBlockLiveOut(catchBlock);
                }
                var cond = createExceptionCheck(exceptionExpr, catchSection.getCatchType());
                builder().enterBranch(entryNode, branchIdx, Values.expression(cond));
                var ifNode = builder().createIf(Expressions.not(cond), null);
                if(lastIfNode != null)
                    lastIfNode.setTarget(ifNode);
                lastIfNode = ifNode;
                var param = requireNonNull(catchSection.getParameter());
                builder().defineVariable(param.getName());
                builder().setVariable(param.getName(), exceptionExpr);
                catchBlock.accept(this);
                exits.put(branchIdx++, builder().createGoto(null));
            }
            builder().enterBranch(entryNode, branchIdx, Values.constantTrue());
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
                            exit ->
                                    Values.expression(
                                            exit == defaultExit ? exceptionExprFinal : Expressions.nullExpression()
                                    )
                    )
            );
            exceptionExpr = new PropertyExpression(new NodeExpression(joinNode), exceptionField.getRef());
        }
        if (statement.getFinallyBlock() != null) {
            statement.getFinallyBlock().accept(this);
        }
        var ifNode = builder().createIf(Expressions.eq(exceptionExpr, Expressions.nullExpression()), null);
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

    private Expression createExceptionCheck(Expression exceptionExpr, PsiType catchType) {
        var expression = new InstanceOfExpression(exceptionExpr, resolveType(catchType));
        return NncUtils.requireNonNull(expression);
    }

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        if (CompilerConfig.isMethodBlacklisted(psiMethod))
            return;
        if(TranspileUtils.isAbstract(psiMethod))
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
        requireNonNull(psiMethod.getBody(), "body is missing from method " +
                TranspileUtils.getMethodQualifiedName(psiMethod)).accept(this);
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
        var switchExpr = new NodeExpression(entryNode);
        var stmts = NncUtils.requireNonNull(statement.getBody()).getStatements();
        builder().enterCondSection(entryNode);
        var modified = NncUtils.requireNonNull(statement.getUserData(Keys.BODY_SCOPE)).getModified();
        var liveVarOut = NncUtils.requireNonNull(statement.getUserData(Keys.LIVE_VARS_OUT));
        var outputVars = NncUtils.intersect(modified, liveVarOut);
        var exits = new HashMap<Long, NodeRT>();
        IfNode lastIfNode = null;
        var branchIdx = 0L;
        for (PsiStatement stmt : stmts) {
            var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
            var caseLabelElementList = labeledRuleStmt.getCaseLabelElementList();
            if (caseLabelElementList == null || caseLabelElementList.getElementCount() == 0)
                continue;
            String castVar = null;
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
            builder().enterBranch(entryNode, branchIdx, Values.expression(cond));
            var ifNode = builder().createIf(Expressions.not(cond), null);
            if(lastIfNode != null)
                lastIfNode.setTarget(ifNode);
            lastIfNode = ifNode;
            if (castVar != null) {
                builder().setVariable(castVar, switchExpr);
            }
            processSwitchCaseBody(labeledRuleStmt.getBody());
            exits.put(branchIdx++, builder().createGoto(null));
        }
        var defaultStmt = (PsiSwitchLabeledRuleStatement)
                NncUtils.findRequired(stmts, stmt -> ((PsiSwitchLabeledRuleStatement) stmt).isDefaultCase());
        builder().enterBranch(entryNode, branchIdx, Values.constantTrue());
        var noop = builder().createNoop();
        if(lastIfNode != null)
            lastIfNode.setTarget(noop);
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
                    joinField.setValue(requireNonNull(exits.get(idx)), Values.expression(values.get(outputVar.toString())))
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
        var field = Objects.requireNonNull(inputNode.getKlass().findFieldByCode(parameter.getName()),
                () -> "Field for parameter " + parameter.getName() + " is not found in the input class");
        builder().defineVariable(parameter.getName());
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
        var outputVars = NncUtils.map(outputVariables, Objects::toString);
        var condOutputs = builder().exitCondSection(sectionId, joinNode, exits, false);
        for (var qn : outputVariables) {
            var branch2value = new HashMap<NodeRT, Value>();
            for (var entry2 : condOutputs.entrySet()) {
                var branchIdx = entry2.getKey();
                var branchOutputs = entry2.getValue();
                branch2value.put(exits.get(branchIdx), Values.expression(branchOutputs.get(qn.toString())));
            }
            var memberTypes = new HashSet<Type>();
            for (var value : branch2value.values()) {
                if (NncUtils.noneMatch(memberTypes, t -> t.isAssignableFrom(value.getType())))
                    memberTypes.add(value.getType());
            }
            var fieldType =
                    memberTypes.size() == 1 ? memberTypes.iterator().next() : new UnionType(memberTypes);
            var field = FieldBuilder.newBuilder(qn.toString(), qn.toString(), joinNode.getKlass(), fieldType).build();
            var mergeField = new JoinNodeField(field, joinNode, branch2value);
            builder().setVariable(qn.toString(), new PropertyExpression(new NodeExpression(joinNode), mergeField.getField().getRef()));
        }
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        builder().defineVariable(variable.getName());
        if (variable.getInitializer() != null) {
            var valueNode = builder().createValue(variable.getName(), resolveExpression(variable.getInitializer()));
            builder().setVariable(variable.getName(), new NodeExpression(valueNode));
        }
    }

    private void processLoop(PsiLoopStatement statement,
                             PsiExpression condition,
                             @Nullable TriConsumer<JoinNode, IfNode, Map<QualifiedName, Field>> preprocessor,
                             @Nullable TriConsumer<JoinNode, NodeRT, NodeRT> postProcessor
                             ) {
        var entryNode = Objects.requireNonNullElseGet(builder().scope().getLastNode(),
                () -> Objects.requireNonNull(builder().scope().getOwner()));
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
        Map<Field, Expression> initialValues = new HashMap<>();
        Map<QualifiedName, Field> loopVar2Field = new HashMap<>();
        for (QualifiedName loopVar : loopVars) {
            var field = builder().newTemporaryField(
                    joinNode.getKlass(),
                    loopVar.toString(),
                    Types.getNullableType(resolveType(loopVar.type()))
            );
            loopVar2Field.put(loopVar, field);
            initialValues.put(field, builder().getVariable(loopVar.toString()));
        }
        for (QualifiedName loopVar : loopVar2Field.keySet()) {
            var field = loopVar2Field.get(loopVar);
            builder().setVariable(loopVar.toString(), Expressions.nodeProperty(joinNode, field));
        }
        var cond = condition != null ? Expressions.not(resolveExpression(condition)) : Expressions.falseExpression();
        var ifNode = builder().createIf(cond, null);
        var exitValues = new HashMap<Field, Expression>();
        for (QualifiedName loopVar : loopVars) {
            var field = loopVar2Field.get(loopVar);
            exitValues.put(field, builder().getVariable(loopVar.toString()));
        }
        if (preprocessor != null) {
            preprocessor.accept(joinNode, ifNode, loopVar2Field);
        }
        if (statement.getBody() != null) {
            statement.getBody().accept(this);
        }
        var goBack = builder().createGoto(joinNode);
        ifNode.setTarget(builder().createNoop());
        for (QualifiedName loopVar : loopVars) {
            var field = loopVar2Field.get(loopVar);
            var initialValue = initialValues.get(field);
            var updatedValue = builder().getVariable(loopVar.toString());
            new JoinNodeField(field, joinNode, Map.of(entryNode, Values.expression(initialValue),
                    goBack, Values.expression(updatedValue)));
            builder().setVariable(loopVar.toString(), Objects.requireNonNull(exitValues.get(field)));
        }
        if(postProcessor != null)
            postProcessor.accept(joinNode, entryNode, goBack);
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
        Expression iteratedExpr;
        if(builder().getExpressionType(iteratedExpr0).isNullable())
            iteratedExpr = Expressions.node(builder().createNonNull("nonNull", iteratedExpr0));
        else
            iteratedExpr = iteratedExpr0;
        var type = builder().getExpressionType(iteratedExpr);
        if (type instanceof ArrayType) {
            processLoop(statement, getExtraLoopTest(statement),
                    (joinNode, ifNode, loopVar2Field) -> {
                        var indexField = FieldBuilder
                                .newBuilder("index", "index", joinNode.getKlass(), Types.getLongType())
                                .build();
                        ifNode.setCondition(
                                Values.expression(
                                        Expressions.or(
                                            ifNode.getCondition().getExpression(),
                                            Expressions.ge(
                                                    Expressions.nodeProperty(joinNode, indexField),
                                                    new FunctionExpression(Func.LEN, iteratedExpr)
                                            )
                                        )
                                )
                        );
                        builder().setVariable(statement.getIterationParameter().getName(),
                                Expressions.arrayAccess(iteratedExpr,
                                        Expressions.nodeProperty(joinNode, indexField))
                        );
                    },
                    (joinNode, entryNode, exitNode) -> {
                        var indexField = joinNode.getKlass().getFieldByCode("index");
                        new JoinNodeField(
                            indexField, joinNode, Map.of(
                                entryNode, Values.constantLong(0L),
                                exitNode,
                                Values.expression(
                                    Expressions.add(
                                            Expressions.nodeProperty(joinNode, indexField),
                                            Expressions.constantLong(1L)
                                    )
                                )
                            )
                        );
                    }
                );
        } else {
            var collType = Types.resolveKlass(type);
            typeResolver.ensureDeclared(collType);
            var itNode = builder().createNonNull("nonNull",
                    Expressions.node(builder().createMethodCall(
                        iteratedExpr, Objects.requireNonNull(collType.findMethodByCode("iterator")),
                        List.of()))
            );
            var itType = Types.resolveKlass(NncUtils.requireNonNull(itNode.getType()));
            processLoop(statement, getExtraLoopTest(statement), (joinNode, ifNode, loopVar2Field) -> {
                ifNode.setCondition(
                    Values.expression(
                        Expressions.or(
                                ifNode.getCondition().getExpression(),
                                Expressions.not(new FunctionExpression(Func.HAS_NEXT, new NodeExpression(itNode)))
                        )
                    )
                );
                var elementNode = builder().createMethodCall(
                        new NodeExpression(itNode),
                        Objects.requireNonNull(itType.findMethodByCode("next")),
                        List.of()
                );
                builder().setVariable(statement.getIterationParameter().getName(), new NodeExpression(elementNode));
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
