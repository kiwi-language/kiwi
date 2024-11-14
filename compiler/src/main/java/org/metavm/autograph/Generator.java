package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.EntityIndex;
import org.metavm.entity.StdKlass;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.CompositeTypeEventRegistry;
import org.metavm.object.type.generic.CompositeTypeListener;
import org.metavm.util.CompilerConfig;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Generator extends VisitorBase {

    public static final Logger logger = LoggerFactory.getLogger(Generator.class);

    private final PsiClass psiClass;
    private final LinkedList<MethodGenerator> builders = new LinkedList<>();
    private final Map<String, Klass> classes = new HashMap<>();
    private final LinkedList<ClassInfo> classInfoStack = new LinkedList<>();
    private final TypeResolver typeResolver;

    public Generator(PsiClass psiClass, TypeResolver typeResolver) {
        this.psiClass = psiClass;
        this.typeResolver = typeResolver;
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
        klass.setQualifiedName(psiClass.getQualifiedName());
        MethodGenerator initFlowBuilder;
        Method initFlow;
        if(!psiClass.isInterface()) {
            initFlow = klass.getMethodByNameAndParamTypes("<init>", List.of());
            initFlowBuilder = new MethodGenerator(initFlow, typeResolver, this);
            initFlowBuilder.enterScope(initFlowBuilder.getMethod().getScope());
            if (klass.getSuperType() != null) {
                var superInit = klass.getSuperType().resolve().findSelfMethodByName("<init>");
                if (superInit != null) {
                    initFlowBuilder.getThis();
                    initFlowBuilder.createMethodCall(superInit);
                }
            }
        }
        else {
            initFlowBuilder = null;
            initFlow = null;
        }
        var classInit = klass.getMethodByNameAndParamTypes("<cinit>", List.of());
        var classInitFlowBuilder = new MethodGenerator(classInit, typeResolver, this);
        classInitFlowBuilder.enterScope(classInitFlowBuilder.getMethod().getScope());
        if (klass.getSuperType() != null) {
            var superCInit = klass.getSuperType().resolve().findSelfMethodByName("<cinit>");
            if (superCInit != null)
                classInitFlowBuilder.createMethodCall(superCInit);
        }
        enterClass(new ClassInfo(klass, psiClass, initFlowBuilder, classInitFlowBuilder));

        super.visitClass(psiClass);

        if(initFlowBuilder != null) {
            initFlowBuilder.createVoidReturn();
            initFlowBuilder.exitScope();
        }

        classInitFlowBuilder.createVoidReturn();
        classInitFlowBuilder.exitScope();

        if(!psiClass.isInterface()) {
            boolean hasConstructor = NncUtils.anyMatch(List.of(psiClass.getMethods()), PsiMethod::isConstructor);
            if (!hasConstructor) {
                var constructor = klass.getDefaultConstructor();
                var constructorGen = new MethodGenerator(constructor, typeResolver, this);
                constructorGen.enterScope(constructor.getScope());
                constructorGen.getThis();
                constructorGen.createMethodCall(initFlow);
                constructorGen.getThis();
                constructorGen.createReturn();
                constructorGen.exitScope();
            }
        }
        if(klass.isEnum())
            Flows.generateValuesMethodBody(klass);
        exitClass();
        klass.setStage(ResolutionStage.DEFINITION);
        klass.emitCode();
//        for (Method method : klass.getMethods()) {
//            if(method.isRootScopePresent()) {
//                logger.debug("Emitted code for method {}. Constant pool size: {}, code length: {}",
//                        method.getQualifiedSignature(), method.getConstantPool().getEntries().size(),
//                        method.getScope().getCode() == null ? 0 : method.getScope().getCode().length);
//            }
//        }
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var ecd = Objects.requireNonNull(enumConstant.getUserData(Keys.ENUM_CONSTANT_DEF));
        var initializer = ecd.getInitializer();
        initializer.clearContent();
        var builder = new MethodGenerator(initializer, typeResolver, this);
        builder.enterScope(initializer.getScope());
        builder.createLoadConstant(Instances.stringInstance(ecd.getName()));
        builder.createLoadConstant(Instances.longInstance(ecd.getOrdinal()));
        var argTypes = NncUtils.merge(
                List.of(Types.getStringType(), Types.getLongType()),
                NncUtils.map(
                    requireNonNull(enumConstant.getArgumentList()).getExpressions(),
                    expr -> builder.getExpressionResolver().resolve(expr).getType()
                )
        );
        var constructor = ecd.getKlass().resolveMethod(ecd.getKlass().getName(), argTypes, List.of(), false);
        builder.createNew(constructor, false, false);
        builder.createReturn();
        builder.exitScope();
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
                fields.add(type.getFieldByName(fieldCode));
            }
            var method = (PsiMethod) requireNonNull(initializer.getMethodExpression().resolve());
            var unique = method.getName().equals("createUnique");
            var index = (Index) NncUtils.find(type.getConstraints(), c -> c instanceof Index idx &&
                    Objects.equals(idx.getName(), psiField.getName()));
            if (index == null)
                index = new Index(type, psiField.getName(), "", unique, fields, null);
            var name2indexField = new HashMap<String, IndexField>();
            index.getFields().forEach(f -> name2indexField.put(f.getName(), f));
            var indexFields = new ArrayList<IndexField>();
            for (Field field : fields) {
                var indexField = name2indexField.get(field.getName());
                if (indexField == null)
                    indexField = IndexField.createFieldItem(index, field);
                indexFields.add(indexField);
            }
            index.setFields(indexFields);
            return;
        }
        var field = Objects.requireNonNull(psiField.getUserData(Keys.FIELD));
        if (psiField.getInitializer() != null) {
            if (field.isStatic()) {
                var builder = currentClassInfo().staticBuilder;
                builder.getExpressionResolver().resolve(psiField.getInitializer());
                builder.createSetStatic(field);
            } else {
                var builder = currentClassInfo().fieldBuilder;
                builder.getThis();
                builder.getExpressionResolver().resolve(psiField.getInitializer());
                builder.createSetField(field);
            }
        } else if (field.getType().isNullable()) {
            initFieldWithConstant(field, Instances.nullInstance());
        } else if (field.getType().isBoolean()) {
            initFieldWithConstant(field, Instances.falseInstance());
        } else if (field.getType().isLong()) {
            initFieldWithConstant(field, Instances.longZero());
        } else if (field.getType().isDouble()) {
            initFieldWithConstant(field, Instances.doubleInstance(0.0));
        }
    }

    private void initFieldWithConstant(Field field, org.metavm.object.instance.core.Value value) {
        if (field.isStatic()) {
            var builder = currentClassInfo().staticBuilder;
            builder.createLoadConstant(value);
            builder.createSetStatic(field);
        } else {
            var builder = currentClassInfo().fieldBuilder;
            builder.getThis();
            builder.createLoadConstant(value);
            builder.createSetField(field);
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
        builder().createTryEnter();
        requireNonNull(statement.getTryBlock()).accept(this);
        var tryExit = builder().createTryExit();
        var exceptionVar = tryExit.getVariableIndex();
        var caught = builder().nextVariableIndex();
        if (statement.getCatchSections().length > 0) {
            JumpNode lastIfNode = null;
            var gotoNodes = new ArrayList<GotoNode>();
            for (PsiCatchSection catchSection : statement.getCatchSections()) {
                var catchBlock = NncUtils.requireNonNull(catchSection.getCatchBlock());
                var sectionEntry = builder().createLoad(exceptionVar, StdKlass.exception.type());
                if(lastIfNode != null)
                    lastIfNode.setTarget(sectionEntry);
                Values.node(builder().createInstanceOf(resolveType(catchSection.getCatchType())));
                lastIfNode = builder().createIfNot(null);
                var param = requireNonNull(catchSection.getParameter());
                builder().createLoadConstant(Instances.trueInstance());
                builder().createStore(caught);
                builder().createLoad(exceptionVar, StdKlass.exception.type());
                builder().createTypeCast(typeResolver.resolveDeclaration(param.getType()));
                builder().createStore(builder().getVariableIndex(param));
                catchBlock.accept(this);
                gotoNodes.add(builder().createGoto(null));
            }
            requireNonNull(lastIfNode).setTarget(builder().createLoadConstant(Instances.falseInstance()));
            builder().createStore(caught);
            var joinNode = builder().createNoop();
            gotoNodes.forEach(g -> g.setTarget(joinNode));
        } else {
            builder().createLoadConstant(Instances.falseInstance());
            builder().createStore(caught);
        }
        if (statement.getFinallyBlock() != null) {
            statement.getFinallyBlock().accept(this);
        }
        builder().createLoad(exceptionVar, StdKlass.exception.type());
        builder().createLoadConstant(Instances.nullInstance());
        builder().createEq();
        var if1 = builder().createIf(null);
        builder().createLoad(caught, Types.getBooleanType());
        var if2 = builder().createIf(null);
        builder().createLoad(exceptionVar, StdKlass.exception.type());
        builder().createRaise();
        var exit = builder().createNoop();
        if1.setTarget(exit);
        if2.setTarget(exit);
    }

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        if (CompilerConfig.isMethodBlacklisted(psiMethod))
            return;
        if(TranspileUtils.isAbstract(psiMethod))
            return;
//        logger.debug("Generating code for method {}", TranspileUtils.getMethodQualifiedName(psiMethod));
        var method = NncUtils.requireNonNull(psiMethod.getUserData(Keys.Method));
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
                    builder().getThis();
                    builder().createMethodCall(superClass.getDefaultConstructor());
                }
                builder.getThis();
                builder.createMethodCall(currentClassInfo().fieldBuilder.getMethod());
                if (currentClass().isEnum()) {
                    var klass = currentClass();
                    var enumClass = requireNonNull(klass.getSuperType()).resolve();
                    builder.getThis();
                    builder.createLoad(1, Types.getStringType());
                    builder.createSetField(enumClass.getFieldByName("name"));
                    builder.getThis();
                    builder.createLoad(2, Types.getLongType());
                    builder.createSetField(enumClass.getFieldByName("ordinal"));
                }
            }
        }
        requireNonNull(psiMethod.getBody(), "body is missing from method " +
                TranspileUtils.getMethodQualifiedName(psiMethod)).accept(this);
        NodeRT lastNode;
        if (psiMethod.isConstructor()) {
            builder.getThis();
            builder.createReturn();
        } else if (method.getReturnType().isVoid() &&
                ((lastNode = method.getScope().getLastNode()) == null || !lastNode.isExit())) {
            builder.createVoidReturn();
        }
        builder.exitScope();
        builders.pop();
        CompositeTypeEventRegistry.removeListener(capturedTypeListener);
//        if(method.getName().equals("Warehouse"))
//            logger.debug("{}", method.getText());
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
        var statements = requireNonNull(method.getBody(),
                () -> "Failed to get body of method " + TranspileUtils.getMethodQualifiedName(method))
                .getStatements();
        boolean requireSuperCall = false;
        if (statements.length > 0) {
            if (statements[0] instanceof PsiExpressionStatement exprStmt &&
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
            if (fistStmt instanceof PsiSwitchLabeledRuleStatement)
                processLabeledRuleSwitch(statement);
            else
                processClassicSwitch(statement);
        }
    }

    private void processLabeledRuleSwitch(PsiSwitchStatement statement) {
        var valueVar = builder().nextVariableIndex();
        var expr = Objects.requireNonNull(statement.getExpression());
        var valueType = typeResolver.resolveDeclaration(expr.getType());
        resolveExpression(expr);
        builder().createStore(valueVar);
        var statements = requireNonNull(statement.getBody()).getStatements();
        var gotoNodes = new ArrayList<GotoNode>();
        List<IfNotNode> lastIfNodes = List.of();
        GotoNode lastGoto = null;
        for (PsiStatement stmt : statements) {
            var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
            var caseLabelElementList = labeledRuleStmt.getCaseLabelElementList();
            if (caseLabelElementList == null || caseLabelElementList.getElementCount() == 0)
                continue;
            List<IfNotNode> ifNodes;
            if (isTypePatternCase(caseLabelElementList)) {
                var typeTestPattern = (PsiTypeTestPattern) caseLabelElementList.getElements()[0];
                var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
                var patternVar = requireNonNull(typeTestPattern.getPatternVariable());
                builder().createLoad(valueVar, valueType);
                builder().createInstanceOf(typeResolver.resolveDeclaration(checkType));
                ifNodes = List.of(builder().createIfNot(null));
                builder().createLoad(valueVar, valueType);
                builder().createStore(builder().getVariableIndex(patternVar));
            } else {
                ifNodes = new ArrayList<>();
                for (var expression : caseLabelElementList.getElements()) {
                    builder().createLoad(valueVar, valueType);
                    resolveExpression((PsiExpression) expression);
                    builder().createEq();
                    ifNodes.add(builder().createIfNot(null));
                }
            }
            if(lastGoto != null) {
                for (JumpNode lastIfNode : lastIfNodes) {
                    lastIfNode.setTarget(lastGoto.getSuccessor());
                }
            }
            lastIfNodes = ifNodes;
            processSwitchCaseBody(labeledRuleStmt.getBody());
            gotoNodes.add(lastGoto = builder().createGoto(null));
        }
        var defaultStmt = (PsiSwitchLabeledRuleStatement)
                NncUtils.findRequired(statements, stmt -> ((PsiSwitchLabeledRuleStatement) stmt).isDefaultCase());
        var d = builder().createNoop();
        lastIfNodes.forEach(n -> n.setTarget(d));
        processSwitchCaseBody(defaultStmt.getBody());
        var exit = builder().createNoop();
        gotoNodes.forEach(g -> g.setTarget(exit));
    }

    private void processSwitchCaseBody(PsiElement element) {
        if (element != null) {
            element.accept(this);
            if (element instanceof PsiExpression bodyExpression) {
                resolveExpression(bodyExpression);
                builder().createYieldStore();
            }
        }
    }

    private void processClassicSwitch(PsiSwitchStatement ignored) {
        throw new UnsupportedOperationException();
    }

    private boolean isTypePatternCase(PsiCaseLabelElementList caseLabelElementList) {
        return caseLabelElementList.getElementCount() == 1 &&
                caseLabelElementList.getElements()[0] instanceof PsiTypeTestPattern;
    }


    private void processParameters(PsiParameterList parameterList, Method method) {
        var i = method.isStatic() ? 0 : (method.isConstructor() && method.getDeclaringType().isEnum() ? 3 : 1);
        for (PsiParameter parameter : parameterList.getParameters()) {
            parameter.putUserData(Keys.VARIABLE_INDEX, i++);
        }
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

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        var i = builder().getVariableIndex(variable);
        if (variable.getInitializer() != null) {
            resolveExpression(variable.getInitializer());
            builder().createStore(i);
        }
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        throw new InternalException("For loop should be transformed into while loop before code generation");
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        var entryNode = builder().createNoop();
        var condition = statement.getCondition();
        resolveExpression(condition);
        var ifNode = condition != null ? builder().createIfNot(null) : null;
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        builder().createGoto(entryNode);
        var exit = builder().createNoop();
        if(ifNode != null)
            ifNode.setTarget(exit);
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        resolveExpression(Objects.requireNonNull(statement.getIteratedValue()));
        builder().createNonNull();
        var iteratedVar = builder().nextVariableIndex();
        builder().createStore(iteratedVar);
        var iteratedType = typeResolver.resolveDeclaration(statement.getIteratedValue().getType());
        if (iteratedType instanceof ArrayType) {
            var indexVar = builder().nextVariableIndex();
            builder().createLoadConstant(Instances.longInstance(0));
            builder().createStore(indexVar);
            var entryNode = builder().createNoop();
            builder().createLoad(indexVar, Types.getLongType());
            builder().createLoad(iteratedVar, iteratedType);
            builder().createArrayLength();
            builder().createGe();
            var ifNode = builder().createIf(null);
            var condition = getExtraLoopTest(statement);
            IfNotNode ifNode1;
            if(condition != null) {
                resolveExpression(condition);
                ifNode1 = builder().createIfNot(null);
            }
            else
                ifNode1 = null;
            builder().createLoad(iteratedVar, iteratedType);
            builder().createLoad(indexVar, Types.getLongType());
            builder().createGetElement();
            builder().createStore(builder().getVariableIndex(statement.getIterationParameter()));
            if (statement.getBody() != null)
                statement.getBody().accept(this);
            builder().createLoad(indexVar, Types.getLongType());
            builder().createLoadConstant(Instances.longOne());
            builder().createAdd();
            builder().createStore(indexVar);
            builder().createGoto(entryNode);
            var exit = builder().createNoop();
            ifNode.setTarget(exit);
            if(ifNode1 != null)
                ifNode1.setTarget(exit);
        } else {
            var collType = Types.resolveKlass(iteratedType);
            typeResolver.ensureDeclared(collType);
            builder().createLoad(iteratedVar, iteratedType);
            var iteratorMethod = Objects.requireNonNull(collType.findMethodByName("iterator"));
            builder().createMethodCall(iteratorMethod);
            builder().createNonNull();
            var itVar = builder().nextVariableIndex();
            builder().createStore(itVar);
            var itType = (ClassType) iteratorMethod.getReturnType().getUnderlyingType();
            var itKlass = itType.resolve();
            var entryNode = builder().createNoop();
            builder().createLoad(itVar, itType);
            builder().createMethodCall(itKlass.getMethod("hasNext", List.of()));
            var ifNode = builder().createIfNot(null);
            var condition = getExtraLoopTest(statement);
            IfNotNode ifNode1;
            if(condition != null) {
                resolveExpression(condition);
                ifNode1 = builder().createIfNot(null);
            }
            else
                ifNode1 = null;
            builder().createLoad(itVar, itType);
            builder().createMethodCall(itKlass.getMethod("next", List.of()));
            builder().createStore(builder().getVariableIndex(statement.getIterationParameter()));
            if (statement.getBody() != null)
                statement.getBody().accept(this);
            builder().createGoto(entryNode);
            var exit = builder().createNoop();
            ifNode.setTarget(exit);
            if(ifNode1 != null)
                ifNode1.setTarget(exit);
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

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        if (Flows.isConstructor(builder().getMethod())) {
            builder().getThis();
            builder().createReturn();
        } else {
            if (statement.getReturnValue() != null) {
                resolveExpression(statement.getReturnValue());
                builder().createReturn();
            } else {
                builder().createVoidReturn();
            }
        }
    }

    @Override
    public void visitThrowStatement(PsiThrowStatement statement) {
        resolveExpression(statement.getException());
        builder().createRaise();
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        resolveExpression(statement.getExpression());
        builder().createYieldStore();
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        if (isExtraLoopTest(statement)) {
            return;
        }
        var node = resolveExpression(statement.getExpression());
        if(node.getType() != null)
            builder().createPop();
    }

    private NodeRT resolveExpression(PsiExpression expression) {
        return builder().getExpressionResolver().resolve(expression);
    }

    private MethodGenerator builder() {
        return requireNonNull(builders.peek());
    }

    @SuppressWarnings("unused")
    public Map<String, Klass> getClasses() {
        return classes;
    }

    private record ClassInfo(Klass klass, PsiClass psiClass, MethodGenerator fieldBuilder, MethodGenerator staticBuilder) {


    }

}
