package org.metavm.autograph;

import com.intellij.psi.*;
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
import static org.metavm.util.NncUtils.ifNotNull;

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
        if(psiClass != this.psiClass)
            return;
        var klass = NncUtils.requireNonNull(psiClass.getUserData(Keys.MV_CLASS));
        if (klass.getStage().isAfterOrAt(ResolutionStage.DEFINITION))
            return;
        klass.setStage(ResolutionStage.DEFINITION);
        klass.setQualifiedName(psiClass.getQualifiedName());
        enterClass(new ClassInfo(klass, psiClass));
        super.visitClass(psiClass);
        exitClass();
        klass.setStage(ResolutionStage.DEFINITION);
        klass.emitCode();
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
        var exits = new ArrayList<GotoNode>();
        var tryEnter = builder().createTryEnter();
        builder().enterTrySection(statement.getFinallyBlock());
        requireNonNull(statement.getTryBlock()).accept(this);
        builder().exitTrySection();
        if (builder().isSequential()) {
            builder().createTryExit();
            ifNotNull(statement.getFinallyBlock(), b -> b.accept(this));
            exits.add(builder().createGoto(null));
        }
        tryEnter.setHandler(builder().createLabel());
        var catchTries = new ArrayList<TryEnterNode>();
        for (PsiCatchSection catchSection : statement.getCatchSections()) {
            builder().createDup();
            builder().createInstanceOf(resolveType(catchSection.getCatchType()));
            var ifNode = builder().createIfEq(null);
            builder().createStore(builder().getVariableIndex(requireNonNull(catchSection.getParameter())));
            var catchBlock = requireNonNull(catchSection.getCatchBlock());
            if (statement.getFinallyBlock() != null) {
                catchTries.add(builder().createTryEnter());
                builder().enterTrySection(statement.getFinallyBlock());
                catchBlock.accept(this);
                builder().exitTrySection();
                if (builder().isSequential()) {
                    builder().createTryExit();
                    statement.getFinallyBlock().accept(this);
                    exits.add(builder().createGoto(null));
                }
            } else {
                catchBlock.accept(this);
                if (builder().isSequential())
                    exits.add(builder().createGoto(null));
            }
            ifNode.setTarget(builder().createLabel());
        }
        if (!catchTries.isEmpty()) {
            var l = builder().createLabel();
            catchTries.forEach(t -> t.setHandler(l));
        }
        var exceptVar = builder().nextVariableIndex();
        builder().createStore(exceptVar);
        ifNotNull(statement.getFinallyBlock(), b -> b.accept(this));
        builder().createLoad(exceptVar, StdKlass.throwable.type());
        builder().createRaise();
        builder().connectBranches(exits);
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
//        if(method.getName().equals("findByName")) {
//            logger.debug("{}", method.getText());
//        }
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        builder().createSwitch(statement);
    }

    private void processParameters(PsiParameterList parameterList, Method method) {
        var i = method.isStatic() ? 0 : 1;
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
        var block = builder().exitSection();
        if(block.hasBreaks())
            block.connectBreaks(builder().createLabel());
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
        var entry = builder().createLabel();
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
        var block = builder().exitSection();
        var continuePoint = builder().createLabel();
        var update = statement.getUpdate();
        if(update != null)
            processExpressions(update);
        builder().createGoto(entry);
        var exit = builder().createLabel();
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
        var entry = builder().createLabel();
        var condition = Objects.requireNonNull(statement.getCondition());
        resolveExpression(condition);
        var ifNode = builder().createIfEq(null);
        builder().enterBlock(statement);
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        var block = builder().exitSection();
        builder().createGoto(entry);
        var exit = builder().createLabel();
        ifNode.setTarget(exit);
        block.connect(entry, exit);
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        var entry = builder().createLabel();
        builder().enterBlock(statement);
        if (statement.getBody() != null)
            statement.getBody().accept(this);
        var block = builder().exitSection();
        var condition = statement.getCondition();
        resolveExpression(condition);
        builder().createIfNe(entry);
        var exit = builder().createLabel();
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
            var entry = builder().createLabel();
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
            var block = builder().exitSection();
            var continuePoint = builder().createLabel();
            builder().createLoad(indexVar, Types.getIntType());
            builder().createLoadConstant(Instances.intOne());
            builder().createIntAdd();
            builder().createStore(indexVar);
            builder().createGoto(entry);
            var exit = builder().createLabel();
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
            var itType = new KlassType(null, StdKlass.iterator.get(), List.of(elementType));
            var entry = builder().createLabel();
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
            var block = builder().exitSection();
            builder().createGoto(entry);
            var exit = builder().createLabel();
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
        var trySection = builder().currentTrySection();
        if (Flows.isConstructor(builder().getMethod())) {
            NncUtils.ifNotNull(trySection, tb -> tb.handleReturn(builder()));
            builder().getThis();
            builder().createReturn();
        } else {
            if (statement.getReturnValue() != null) {
                resolveExpression(statement.getReturnValue());
                NncUtils.ifNotNull(trySection, tb -> tb.handleReturn(builder()));
                builder().createReturn();
            } else {
                NncUtils.ifNotNull(trySection, tb -> tb.handleExit(builder()));
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
        var section = builder().currentSection();
        while (section != null) {
            if (section instanceof TrySection trySection)
                trySection.handleExit(builder());
            else if (section instanceof MethodGenerator.SwitchExpressionSection switchExpressionSection) {
                switchExpressionSection.addYield(builder().createGoto(null));
                return;
            }
            section = section.getParent();
        }
        throw new IllegalStateException("Not inside a switch section");
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
        var block = builder().exitSection();
        if(block.hasBreaks())
            block.connectBreaks(builder().createLabel());
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        var label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var section = builder().currentSection();
        while (section != null) {
            if (section instanceof TrySection trySection)
                trySection.handleExit(builder());
            else if (section.isBreakTarget(label)) {
                section.addBreak(builder().createGoto(null), label);
                return;
            }
            section = section.getParent();
        }
        throw new IllegalStateException("Cannot find the target block for break statement: " + statement.getText());
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        var label = NncUtils.get(statement.getLabelIdentifier(), PsiElement::getText);
        var section = builder().currentSection();
        while (section != null) {
            if (section instanceof TrySection trySection)
                trySection.handleExit(builder());
            else if (section.isContinueTarget(label)) {
                section.addContinue(builder().createGoto(null), label);
                return;
            }
            section = section.getParent();
        }
        throw new IllegalStateException("Cannot find the target block for continue statement: " + statement.getText());
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
