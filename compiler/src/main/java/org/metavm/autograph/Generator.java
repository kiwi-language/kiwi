package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.EntityIndex;
import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.*;
import org.metavm.object.type.*;
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
        enterClass(new ClassInfo(klass, psiClass));
        super.visitClass(psiClass);
        if(klass.isEnum())
            Flows.generateValuesMethodBody(klass);
        exitClass();
        klass.setStage(ResolutionStage.DEFINITION);
        klass.emitCode();
    }

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        var ecd = Objects.requireNonNull(enumConstant.getUserData(Keys.ENUM_CONSTANT_DEF));
        var initializer = ecd.getInitializer();
        initializer.clearContent();
        initializer.setReturnType(initializer.getDeclaringType().getType());
        var builder = new MethodGenerator(initializer, typeResolver, this);
        builder.enterScope(initializer.getCode());
        builder.createLoadConstant(Instances.stringInstance(ecd.getName()));
        builder.createLoadConstant(Instances.intInstance(ecd.getOrdinal()));
        var argList = enumConstant.getArgumentList();
        if(argList != null) {
            for (var arg :  argList.getExpressions()){
                builder.getExpressionResolver().resolve(arg);
            }
        }
        var paramTypes = new ArrayList<Type>();
        paramTypes.add(Types.getStringType());
        paramTypes.add(Types.getIntType());
        var generics = enumConstant.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(generics.getElement());
        for (var psiParam : method.getParameterList().getParameters()) {
            paramTypes.add(typeResolver.resolveNullable(psiParam.getType(), ResolutionStage.DECLARATION));
        }
        var constructor = ecd.getKlass().getSelfMethod(
                m -> m.isConstructor() && m.getParameterTypes().equals(paramTypes)
        );
        var typeArgs = NncUtils.map(
                method.getTypeParameters(),
                tp -> typeResolver.resolveDeclaration(generics.getSubstitutor().substitute(tp))
        );
        var methodRef = new MethodRef(ecd.getKlass().getType(), constructor, typeArgs);
        builder.createNew(methodRef, false, false);
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
                lastIfNode = builder().createIfEq(null);
                var param = requireNonNull(catchSection.getParameter());
                builder().createLoadConstant(Instances.one());
                builder().createStore(caught);
                builder().createLoad(exceptionVar, StdKlass.exception.type());
                builder().createTypeCast(typeResolver.resolveDeclaration(param.getType()));
                builder().createStore(builder().getVariableIndex(param));
                catchBlock.accept(this);
                gotoNodes.add(builder().createGoto(null));
            }
            requireNonNull(lastIfNode).setTarget(builder().createLoadConstant(Instances.zero()));
            builder().createStore(caught);
            var joinNode = builder().createNoop();
            gotoNodes.forEach(g -> g.setTarget(joinNode));
        } else {
            builder().createLoadConstant(Instances.zero());
            builder().createStore(caught);
        }
        if (statement.getFinallyBlock() != null) {
            statement.getFinallyBlock().accept(this);
        }
        builder().createLoad(exceptionVar, StdKlass.exception.type());
        builder().createLoadConstant(Instances.nullInstance());
        builder().createRefCompareEq();
        var if1 = builder().createIfNe(null);
        builder().createLoad(caught, Types.getBooleanType());
        var if2 = builder().createIfNe(null);
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
        MethodGenerator builder = new MethodGenerator(method, typeResolver, this);
        builders.push(builder);
        builder.enterScope(method.getCode());
        if (TranspileUtils.isStatic(psiMethod)) {
            processParameters(psiMethod.getParameterList(), method);
        } else {
            processParameters(psiMethod.getParameterList(), method);
            if (psiMethod.isConstructor()) {
                if (currentClass().isEnum()) {
                    var klass = currentClass();
                    var enumType = requireNonNull(klass.getSuperType());
                    builder.getThis();
                    builder.createLoad(1, Types.getStringType());
                    builder.createSetField(new FieldRef(enumType, StdField.enumName.get()));
                    builder.getThis();
                    builder.createLoad(2, Types.getIntType());
                    builder.createSetField(new FieldRef(enumType, StdField.enumOrdinal.get()));
                }
            }
        }
        requireNonNull(psiMethod.getBody(), "body is missing from method " +
                TranspileUtils.getMethodQualifiedName(psiMethod)).accept(this);
        Node lastNode;
        if (psiMethod.isConstructor()) {
            builder.getThis();
            builder.createReturn();
        } else if (method.getReturnType().isVoid() &&
                ((lastNode = method.getCode().getLastNode()) == null || !lastNode.isExit())) {
            builder.createVoidReturn();
        }
        builder.exitScope();
        builders.pop();
//        if(method.getName().equals("test")) {
//            logger.debug("{}", method.getText());
//        }
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
                processEnhancedSwitch(statement);
            else
                processLegacySwitch(statement);
        }
    }

    private void processEnhancedSwitch(PsiSwitchStatement statement) {
        var valueVar = builder().nextVariableIndex();
        var expr = Objects.requireNonNull(statement.getExpression());
        var psiType = expr.getType();
        var valueType = typeResolver.resolveDeclaration(psiType);
        resolveExpression(expr);
        builder().createStore(valueVar);
        var statements = requireNonNull(statement.getBody()).getStatements();
        var gotoNodes = new ArrayList<GotoNode>();
        int i = 0;
        for (PsiStatement stmt : statements) {
            var labeledRuleStmt = (PsiSwitchLabeledRuleStatement) stmt;
            if (labeledRuleStmt.isDefaultCase())
                continue;
            var elements = requireNonNull(labeledRuleStmt.getCaseLabelElementList()).getElements();
            for (var element : elements) {
                var ifNode = builder().processCaseElement(element, valueVar, valueType, psiType);
                builder().createLoadConstant(Instances.intInstance(i++));
                gotoNodes.add(builder().createGoto(null));
                ifNode.setTarget(builder().createLabel());
            }
        }
        builder().createLoadConstant(Instances.intInstance(-1));
        var tableSwitch = builder().createTableSwitch(0, statements.length - 2);
        gotoNodes.forEach(g -> g.setTarget(tableSwitch));
        builder().enterBlock(statement);
        var gotoNodes2 = new ArrayList<GotoNode>();
        for (PsiStatement stmt : statements) {
            var label = (PsiSwitchLabeledRuleStatement) stmt;
            var target = builder().createLabel();
            if (label.isDefaultCase())
                tableSwitch.setDefaultTarget(target);
            else {
                var elementCount = requireNonNull(label.getCaseLabelElementList()).getElementCount();
                for (int j = 0; j < elementCount; j++) {
                    tableSwitch.addTarget(target);
                }
            }
            processSwitchCaseBody(label.getBody());
            gotoNodes2.add(builder().createGoto(null));
        }
        var exit = builder().createLabel();
        if (tableSwitch.getDefaultTarget() == tableSwitch)
            tableSwitch.setDefaultTarget(exit);
        gotoNodes2.forEach(g -> g.setTarget(exit));
        builder().exitBlock().connectBreaks(exit);
    }

    private void processSwitchCaseBody(PsiElement element) {
        if (element != null) {
            element.accept(this);
//            if (element instanceof PsiExpression bodyExpression) {
//                resolveExpression(bodyExpression);
//                builder().createYield();
//            }
        }
    }

    private void processLegacySwitch(PsiSwitchStatement statement) {
        var valueVar = builder().nextVariableIndex();
        var expr = Objects.requireNonNull(statement.getExpression());
        var psiType = expr.getType();
        var valueType = typeResolver.resolveDeclaration(psiType);
        resolveExpression(expr);
        builder().createStore(valueVar);
        var statements = requireNonNull(statement.getBody()).getStatements();
        var gotoNodes = new ArrayList<GotoNode>();
        var labels = NncUtils.filterByType(List.of(statements), PsiSwitchLabelStatement.class);
        int i = 0;
        for (var label : labels) {
            if (label.isDefaultCase())
                continue;
            var elements = Objects.requireNonNull(label.getCaseLabelElementList()).getElements();
            for (var element : elements) {
                var ifNode = builder().processCaseElement(element, valueVar, valueType, psiType);
                builder().createLoadConstant(Instances.intInstance(i++));
                gotoNodes.add(builder().createGoto(null));
                ifNode.setTarget(builder().createLabel());
            }
        }
        builder().createLoadConstant(Instances.intInstance(-1));
        var tableSwitch = builder().createTableSwitch(0, labels.size() - 2);
        gotoNodes.forEach(g -> g.setTarget(tableSwitch));
        builder().enterBlock(statement);
        for (PsiStatement stmt : statements) {
            if (stmt instanceof PsiSwitchLabelStatement label) {
                var target = builder().createLabel();
                if (label.isDefaultCase())
                    tableSwitch.setDefaultTarget(target);
                else {
                    var elementCount = requireNonNull(label.getCaseLabelElementList()).getElementCount();
                    for (int j = 0; j < elementCount; j++) {
                        tableSwitch.addTarget(target);
                    }
                }
            } else
                stmt.accept(this);
        }
        var exit = builder().createLabel();
        if (tableSwitch.getDefaultTarget() == tableSwitch)
            tableSwitch.setDefaultTarget(exit);
        builder().exitBlock().connectBreaks(exit);
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
        builder().enterBlock(statement);
        builder().getExpressionResolver().constructIf(
                requireNonNull(statement.getCondition()),
                () -> {
                    if (statement.getThenBranch() != null)
                        statement.getThenBranch().accept(this);
                },
                () -> {
                    if (statement.getElseBranch() != null)
                        statement.getElseBranch().accept(this);
                }
        );
        var block = builder().exitBlock();
        if(block.hasBreaks())
            block.connectBreaks(builder().createNoop());
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
        var init = Objects.requireNonNull(statement.getInitialization());
        if (init instanceof PsiDeclarationStatement declStmt) {
            for (PsiElement element : declStmt.getDeclaredElements()) {
                PsiVariable variable = (PsiVariable) element;
                var varIdx = builder().getVariableIndex(variable);
                if(variable.getInitializer() != null) {
                    resolveExpression(variable.getInitializer());
                    builder().createStore(varIdx);
                }
            }
        } else
            processExpressions(init);
        var entry = builder().createNoop();
        var cond = statement.getCondition();
        IfEqNode ifNode;
        if(cond != null) {
            resolveExpression(cond);
            ifNode = builder().createIfEq(null);
        }
        else
            ifNode = null;
        builder().enterBlock(statement);
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        var block = builder().exitBlock();
        var continuePoint = builder().createNoop();
        var update = statement.getUpdate();
        if(update != null)
            processExpressions(update);
        builder().createGoto(entry);
        var exit = builder().createNoop();
        if(ifNode != null)
            ifNode.setTarget(exit);
        block.connect(continuePoint, exit);
    }

    private void processExpressions(PsiStatement statement) {
        switch (statement) {
            case PsiExpressionListStatement exprListStmt -> {
                for (var e : exprListStmt.getExpressionList().getExpressions())
                    resolveAndPopExpression(e);
            }
            case PsiExpressionStatement exprStmt -> resolveAndPopExpression(exprStmt.getExpression());
            case PsiEmptyStatement ignored -> {}
            default ->
                    throw new InternalException("Not an expression or expression list statement " + statement.getText());
        }
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        var entry = builder().createNoop();
        var condition = Objects.requireNonNull(statement.getCondition());
        resolveExpression(condition);
        var ifNode = builder().createIfEq(null);
        builder().enterBlock(statement);
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        var block = builder().exitBlock();
        builder().createGoto(entry);
        var exit = builder().createNoop();
        ifNode.setTarget(exit);
        block.connect(entry, exit);
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        var entry = builder().createNoop();
        builder().enterBlock(statement);
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        var block = builder().exitBlock();
        var condition = statement.getCondition();
        resolveExpression(condition);
        builder().createIfNe(entry);
        var exit = builder().createNoop();
        block.connect(entry, exit);
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
            builder().createLoadConstant(Instances.intInstance(0));
            builder().createStore(indexVar);
            var entry = builder().createNoop();
            builder().createLoad(indexVar, Types.getIntType());
            builder().createLoad(iteratedVar, iteratedType);
            builder().createArrayLength();
            builder().createCompareGe(TranspileUtils.intType);
            var ifNode = builder().createIfNe(null);
            var condition = getExtraLoopTest(statement);
            IfEqNode ifNode1;
            if(condition != null) {
                resolveExpression(condition);
                ifNode1 = builder().createIfEq(null);
            }
            else
                ifNode1 = null;
            builder().createLoad(iteratedVar, iteratedType);
            builder().createLoad(indexVar, Types.getIntType());
            builder().createGetElement();
            builder().createStore(builder().getVariableIndex(statement.getIterationParameter()));
            builder().enterBlock(statement);
            if (statement.getBody() != null)
                statement.getBody().accept(this);
            var block = builder().exitBlock();
            var continuePoint = builder().createNoop();
            builder().createLoad(indexVar, Types.getIntType());
            builder().createLoadConstant(Instances.intOne());
            builder().createIntAdd();
            builder().createStore(indexVar);
            builder().createGoto(entry);
            var exit = builder().createNoop();
            ifNode.setTarget(exit);
            if(ifNode1 != null)
                ifNode1.setTarget(exit);
            block.connect(continuePoint, exit);
        } else {
            var iterableType = Types.resolveAncestorType((ClassType) iteratedType, StdKlass.iterable.get());
            builder().createLoad(iteratedVar, iteratedType);
            builder().createMethodCall(new MethodRef(iterableType, StdMethod.iterableIterator.get(), List.of()));
            builder().createNonNull();
            var itVar = builder().nextVariableIndex();
            builder().createStore(itVar);
            var elementType = iterableType.getFirstTypeArgument();
            var itType = new ClassType(null, StdKlass.iterator.get(), List.of(elementType));
            var entry = builder().createNoop();
            builder().createLoad(itVar, itType);
            builder().createMethodCall(new MethodRef(itType, StdMethod.iteratorHasNext.get(), List.of()));
            var ifNode = builder().createIfEq(null);
            var condition = getExtraLoopTest(statement);
            IfEqNode ifNode1;
            if(condition != null) {
                resolveExpression(condition);
                ifNode1 = builder().createIfEq(null);
            }
            else
                ifNode1 = null;
            builder().createLoad(itVar, itType);
            builder().createMethodCall(new MethodRef(itType, StdMethod.iteratorNext.get(), List.of()));
            builder().createStore(builder().getVariableIndex(statement.getIterationParameter()));
            builder().enterBlock(statement);
            if (statement.getBody() != null)
                statement.getBody().accept(this);
            var block = builder().exitBlock();
            builder().createGoto(entry);
            var exit = builder().createNoop();
            ifNode.setTarget(exit);
            if(ifNode1 != null)
                ifNode1.setTarget(exit);
            block.connect(entry, exit);
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
        builder().createYield();
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        if (isExtraLoopTest(statement))
            return;
        resolveAndPopExpression(statement.getExpression());
    }

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        builder().enterBlock(statement);
        super.visitBlockStatement(statement);
        var block = builder().exitBlock();
        if(block.hasBreaks())
            block.connectBreaks(builder().createNoop());
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        var label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var block = builder().currentBlock();
        try {
            block.addBreak(builder().createGoto(null), label);
        }
        catch (Exception e) {
            block.printBlocks();
            throw e;
        }
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        var label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        builder().currentBlock().addContinue(builder().createGoto(null), label);
    }

    private Node resolveExpression(PsiExpression expression) {
        return builder().getExpressionResolver().resolve(expression);
    }

    private void resolveAndPopExpression(PsiExpression expression) {
        var node = resolveExpression(expression);
        if(node.hasOutput())
            builder().createPop();
    }

    private MethodGenerator builder() {
        return requireNonNull(builders.peek());
    }

    @SuppressWarnings("unused")
    public Map<String, Klass> getClasses() {
        return classes;
    }

    private record ClassInfo(Klass klass, PsiClass psiClass) {


    }

}
