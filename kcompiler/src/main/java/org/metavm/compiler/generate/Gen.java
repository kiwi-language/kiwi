package org.metavm.compiler.generate;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.flow.Bytecodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;

import static java.util.Objects.requireNonNull;

public class Gen extends StructuralNodeVisitor {

    private static final Logger logger = LoggerFactory.getLogger(Gen.class);

    private final Project project;
    private Clazz clazz;
    private Code code;
    private final Env env;
    private final Item[] stackItems = new Item[TypeTags.TAG_ANY + 1];
    private @Nullable Scope currentScope;

    public Gen(Project project, Log logger) {
        this.project = project;
        env = new Env(project, logger);
        for(var tag = TypeTags.TAG_NEVER; tag <= TypeTags.TAG_ANY; tag++) {
            stackItems[tag] = tag == TypeTags.TAG_VOID ? new VoidItem() : new StackItem(tag);
        }
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var ignored = env.enterScope(classDecl)) {
            var prevClazz = clazz;
            clazz = classDecl.getElement();
            var cp = clazz.getConstPool();
            clazz.getInterfaces().forEach(cp::put);
            clazz.getTypeParams().forEach(tp -> cp.put(tp.getBound()));
            if (!clazz.isInterface())
                generatePrimInit(classDecl);
            for (EnumConst enumConstant : clazz.getEnumConstants()) {
                cp.put(enumConstant.getType());
            }
            for (Field field : clazz.getFields()) {
                cp.put(field.getType());
            }
            for (Node member : classDecl.getMembers()) {
                if (member instanceof ClassDecl || member instanceof MethodDecl)
                    member.accept(this);
            }
            for (EnumConstDecl ec : classDecl.enumConstants()) {
                if (ec.getDecl() != null)
                    ec.getDecl().accept(this);
            }
            clazz = prevClazz;
            cp.freeze();
            return null;
        }
    }

    private void generatePrimInit(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        var method = requireNonNull(clazz.getPrimaryInit());
        try(var ignored = env.enterScope(classDecl, method)) {
            if (Traces.traceGeneration)
                logger.trace("Generating code for primary constructor: {}", method.getQualName());
            var prevCode = code;
            var cp = method.getConstPool();
            code = requireNonNull(method.getCode());
            code.setMaxLocals(1);
            for (var param : method.getParams()) {
                cp.put(param.getType());
                code.newLocal(param);
            }
            cp.put(method.getRetType());
            clazz.getTypeParams().forEach(tp -> cp.put(tp.getBound()));
            for (ClassParamDecl param : classDecl.getParams()) {
                if (param.getField() != null) {
                    code.loadThis(env);
                    code.load(param.getElement().getIndex(), param.getElement().getType());
                    param.getField().store(code, env);
                }
            }
            var superType = clazz.getSuper();
            if (superType != null) {
                var call = classDecl.getImplements().head().makeCallExpr();
                genExpr(call, null).drop();
            }
            for (Node member : classDecl.getMembers()) {
                if (member instanceof FieldDecl fieldDecl) {
                    var field = fieldDecl.getElement();
                    if (!field.isStatic() && fieldDecl.getInitial() != null) {
                        code.loadThis(env);
                        genExpr(fieldDecl.getInitial(), field.getType()).load();
                        field.store(code, env);
                    }
                }
                else if (member instanceof Init init)
                    init.accept(this);
            }
            if (code.isAlive()) {
                if (!method.getRetType().isVoid())
                    throw new RuntimeException("Code of method '" + method.getQualName() + "' is not properly terminated");
                code.voidRet();
            }
            code = prevCode;
            cp.freeze();
        }
    }

    @Override
    public Void visitPackageDecl(PackageDecl packageDecl) {
        return null;
    }

    @Override
    public Void visitImport(Import imp) {
        return null;
    }

    @Override
    public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
        var initial = localVarDecl.getInitial();
        var v = localVarDecl.getElement();
        code.newLocal(v);
        if (initial != null) {
            genExpr(initial, v.getType()).load();
            code.store(localVarDecl.getElement().getIndex());
        }
        return null;
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        try(var ignored = env.enterScope(methodDecl)) {
            if (Traces.traceGeneration)
                logger.trace("Generating code for method: {}", method.getQualName());
            var prevCode = code;
            var cp = method.getConstPool();
            code = method.getCode();
            if (!method.isStatic() && !method.isAbstract())
                code.setMaxLocals(1);
            for (Param param : method.getParams()) {
                cp.put(param.getType());
                if (!method.isAbstract())
                    code.newLocal(param);
            }
            cp.put(method.getRetType());
            clazz.getTypeParams().forEach(tp -> cp.put(tp.getBound()));
            super.visitMethodDecl(methodDecl);
            if (!method.isAbstract() && code.isAlive()) {
                if (!method.getRetType().isVoid())
                    throw new RuntimeException("Code of method '" + method.getQualName() + "' is not properly terminated");
                code.voidRet();
            }
            code = prevCode;
            cp.freeze();
            return null;
        }
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        genExpr(exprStmt.expr(), null).drop();
        return null;
    }

    @Override
    public Void visitReturnStmt(RetStmt retStmt) {
        var r = retStmt.result();
        if (r != null) {
            genExpr(r, retStmt.getType()).load();
            doFinally();
            code.ret();
        }
        else {
            doFinally();
            code.voidRet();
        }
        return null;
    }

    @Override
    public Void visitDelStmt(DelStmt delStmt) {
        genExpr(delStmt.getExpr(), PrimitiveType.ANY).load();
        code.del();
        return null;
    }

    @Override
    public Void visitTryStmt(TryStmt tryStmt) {
        var pc = code.pc();
        code.tryEnter();
        try (var ignored = env.enterScope(tryStmt)) {
            tryStmt.getBody().accept(this);
        }
        Code.Chain exit = null;
        if (code.isAlive()) {
            code.tryExit();
            exit = code.goto_(0);
        }
        code.index(pc + 1, code.pc() - pc);
        var thr = project.getRootPackage().subPackage("java").subPackage("lang")
                .getClass("Throwable");
        code.reset(thr);
        var v = code.nextLocal();
        code.store(v);
        for (Catcher catcher : tryStmt.getCatchers()) {
            code.load(v, thr);
            code.is(catcher.getParam().getElement().getType());
            var chain = code.branch(Bytecodes.IF_EQ, 0);
            var param = catcher.getParam().getElement();
            code.newLocal(param);
            code.load(v, thr);
            code.store(param.getIndex());
            catcher.getBlock().accept(this);
            if (code.isAlive())
                exit = Code.Chain.merge(exit, code.goto_(0));
            code.connect(chain);
        }
        code.load(v, thr);
        code.raise();
        code.connect(exit);
        return null;
    }

    private void doFinally() {
        var s = env.currentScope();
        while (s != null && !(s.getElement() instanceof Executable)) {
            if (s.getNode() instanceof TryStmt)
                code.tryExit();
            s = s.getParent();
        }
    }

    @Override
    public Void visitThrowStmt(ThrowStmt throwStmt) {
        genExpr(throwStmt.expr(), null).load();
        doFinally();
        code.raise();
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        var item = genExpr(ifStmt.cond(), null).makeCond();
        var falseChain = item.jumpFalse();
        ifStmt.body().accept(this);
        var els = ifStmt.else_();
        if (els != null) {
            if (code.isAlive()) {
                var trueChain = code.goto_(0);
                code.connect(falseChain);
                els.accept(this);
                code.connect(trueChain);
            }
            else {
                code.connect(falseChain);
                els.accept(this);
            }
        }
        else
            code.connect(falseChain);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        try (var scope = enterScope(whileStmt)) {
            var pc = code.pc();
            var item = genExpr(whileStmt.cond(), null).makeCond();
            scope.addExit(item.jumpFalse());
            code.connect(item.trueJumps);
            whileStmt.body().accept(this);
            code.connect(scope.cont);
            if (code.isAlive())
                code.goto_(pc - code.pc());
            code.connect(scope.exit);
            return null;
        }
    }

    @Override
    public Void visitDoWhileStmt(DoWhileStmt doWhileStmt) {
        try (var scope = enterScope(doWhileStmt)) {
            var pc = code.pc();
            doWhileStmt.body().accept(this);
            code.connect(scope.cont);
            if (code.isAlive()) {
                var item = genExpr(doWhileStmt.cond(), null).makeCond();
                scope.addExit(item.jumpFalse());
                code.connect(item.trueJumps);
                code.goto_(pc - code.pc());
            }
            code.connect(scope.exit);
            return null;
        }
    }

    @Override
    public Void visitBreakStmt(BreakStmt breakStmt) {
        var scope = findScope(breakStmt.getTarget());
        scope.addExit(code.goto_(0));
        return null;
    }

    @Override
    public Void visitLabeledStmt(LabeledStmt labeledStmt) {
        try (var scope = enterScope(labeledStmt)) {
            super.visitLabeledStmt(labeledStmt);
            code.connect(scope.exit);
            return null;
        }
    }

    @Override
    public Void visitContinueStmt(ContinueStmt continueStmt) {
        var scope = findScope(continueStmt.getTarget());
        scope.addContinue(code.goto_(0));
        return null;
    }

    @Override
    public Void visitForeachStmt(ForeachStmt foreachStmt) {
        try (var scope = enterScope(foreachStmt)) {
            if (foreachStmt.getExpr().getType() instanceof ClassType)
                generateIterableForeach(foreachStmt, scope);
            else
                generateArrayForeach(foreachStmt, scope);
            return null;
        }
    }

    private void generateIterableForeach(ForeachStmt foreachStmt, Scope scope) {
        var v = foreachStmt.getVar().getElement();
        var iterableClass = project.getRootPackage().subPackage("java").subPackage("lang")
                .getClass("Iterable");
        var iterableType = iterableClass.getInst(List.of(v.getType()));
        var expr = foreachStmt.getExpr();
        code.newLocal(v);
        genExpr(expr, null).load();
        var iteratorMethod = (MethodRef) requireNonNull(iterableType.getTable().lookupFirst(NameTable.instance.iterator));
        iteratorMethod.invoke(code, env);
        var iteratorType = (ClassType) iteratorMethod.getRetType();
        var iteratorVar = code.nextLocal();
        code.store(iteratorVar);
        var entry = code.pc();
        code.load(iteratorVar, iterableType);
        var hasNextMethod = (MethodRef) requireNonNull(iteratorType.getTable().lookupFirst(NameTable.instance.hasNext));
        hasNextMethod.invoke(code, env);
        var item = stackItems[TypeTags.TAG_INT].makeCond();
        scope.addExit(item.jumpFalse());
        code.connect(item.trueJumps);
        code.load(iteratorVar, expr.getType());
        var nextMethod = (MethodRef) requireNonNull(iteratorType.getTable().lookupFirst(NameTable.instance.next));
        nextMethod.invoke(code, env);
        code.store(v.getIndex());
        foreachStmt.getBody().accept(this);
        code.connect(scope.cont);
        if (code.isAlive())
            code.goto_(entry - code.pc());
        code.connect(scope.exit);
    }

    private void generateArrayForeach(ForeachStmt foreachStmt, Scope scope) {
        var v = foreachStmt.getVar().getElement();
        code.newLocal(v);
        var idx = code.nextLocal();
        code.ldc(0);
        code.store(idx);
        var expr = foreachStmt.getExpr();
        var arrayVar = code.nextLocal();
        genExpr(expr, null).load();
        code.store(arrayVar);
        var entry = code.pc();
        code.load(arrayVar, expr.getType());
        code.arrayLength();
        code.load(idx, PrimitiveType.INT);
        code.compare(PrimitiveType.INT);
        code.gt();
        var item = intStackItem().makeCond();
        scope.addExit(item.jumpFalse());
        code.connect(item.trueJumps);
        code.load(arrayVar, expr.getType());
        code.load(idx, PrimitiveType.INT);
        code.arrayLoad();
        code.store(v.getIndex());
        foreachStmt.getBody().accept(this);
        code.connect(scope.cont);
        if (code.isAlive()) {
            code.load(idx, PrimitiveType.INT);
            code.inc(PrimitiveType.INT);
            code.store(idx);
            code.goto_(entry - code.pc());
        }
        code.connect(scope.exit);
    }

    private Item intStackItem() {
        return stackItems[TypeTags.TAG_INT];
    }

    @Override
    public Void visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
        var v = typeVariableDecl.getElement();
        ((ConstPoolOwner) v.getGenericDeclaration()).getConstPool().put(v.getBound());
        return super.visitTypeVariableDecl(typeVariableDecl);
    }

    @Override
    public Void visitExpr(Expr expr) {
        genExpr(expr, null);
        return null;
    }

    @Override
    public Void visitTypeNode(TypeNode typeNode) {
        return null;
    }

    private Item genExpr(Expr expr, @Nullable Type targetType) {
        if (Traces.traceGeneration)
            logger.trace("Generating expression: {}", expr.getText());
        var item = expr.accept(new ExprVisitor());
        return targetType != null ? item.cast(targetType) : item;
    }

    private class ExprVisitor extends AbstractNodeVisitor<Item> {

        @Override
        public Item visitCondExpr(CondExpr condExpr) {
            var item = condExpr.getCond().accept(this).makeCond();
            var falseChain = item.jumpFalse();
            genExpr(condExpr.getTruePart(), condExpr.getType()).load();
            var trueChain = code.goto_(0);
            code.connect(falseChain);
            genExpr(condExpr.getFalsePart(), condExpr.getType()).load();
            code.connect(trueChain);
            return stackItems[condExpr.getType().getTag()];
        }

        @Override
        public Item visitLambdaExpr(LambdaExpr lambdaExpr) {
            if (Traces.traceGeneration)
                logger.trace("Generating code for lambda: {}", lambdaExpr.getText());
            var prevCode = code;
            var lambda = lambdaExpr.getElement();
            var func = env.currentFunc();
            lambda.attachTo(func);
            var cp = func.getConstPool();
            lambda.getParams().forEach(p -> cp.put(p.getType()));
            cp.put(lambda.getRetType());
            try (var ignored = env.enterScope(lambdaExpr)) {
                code = lambda.getCode();
                for (Param param : lambdaExpr.getElement().getParams()) {
                    cp.put(param.getType());
                    code.newLocal(param);
                }
                if (lambdaExpr.body() instanceof Expr expr) {
                    var item = genExpr(expr, lambda.getRetType().isVoid() ? null : lambda.getRetType());
                    if (lambda.getRetType().isVoid()) {
                        item.drop();
                        code.voidRet();
                    } else {
                        item.load();
                        code.ret();
                    }
                } else {
                    lambdaExpr.body().accept(Gen.this);
                    if (code.isAlive()) {
                        if (!lambda.getRetType().isVoid())
                            throw new IllegalStateException("Code of lambda " + lambda.getQualName() + " is not properly terminated");
                        code.voidRet();
                    }
                }
                code = prevCode;
                code.lambda(lambda);
                return topStackItem();
            }
        }

        private Item topStackItem() {
            return stackItems[code.peek().getTag()];
        }

        @Override
        public Item visitCastExpr(CastExpr castExpr) {
            castExpr.expr().accept(this).load();
            code.cast(castExpr.getType());
            return topStackItem();
        }

        @Override
        public Item visitRangeExpr(RangeExpr rangeExpr) {
            // TODO use a sequence instead of an array to hold the range
            var reg = code.nextLocal();
            var maxReg = code.nextLocal();
            var arrayReg = code.nextLocal();
            rangeExpr.getMin().accept(this).load();
            code.store(reg);
            rangeExpr.getMax().accept(this).load();
            code.store(maxReg);
            code.newArray(rangeExpr.getType());
            code.store(arrayReg);
            var entry = code.pc();
            code.load(reg, PrimitiveType.INT);
            code.load(maxReg, PrimitiveType.INT);
            code.compare(PrimitiveType.INT);
            code.lt();
            var chain = code.branch(Bytecodes.IF_EQ, 0);
            code.load(arrayReg, rangeExpr.getType());
            code.load(reg, PrimitiveType.INT);
            code.arrayAdd();
            code.load(reg, PrimitiveType.INT);
            code.inc(PrimitiveType.INT);
            code.store(reg);
            code.goto_(entry - code.pc());
            code.connect(chain);
            code.load(arrayReg, rangeExpr.getType());
            return getStackitem(rangeExpr.getType());
        }

        @Override
        public Item visitBinaryExpr(BinaryExpr binaryExpr) {
            if (binaryExpr.op() == BinOp.AND) {
                var lCond = binaryExpr.lhs().accept(this).makeCond();
                var falseChain = lCond.jumpFalse();
                code.connect(lCond.trueJumps);
                var rCond = binaryExpr.rhs().accept(this).makeCond();
                return new CondItem(
                        rCond.opcode,
                        rCond.trueJumps,
                        Code.Chain.merge(falseChain, rCond.falseJumps)
                );
            }
            else if (binaryExpr.op() == BinOp.OR) {
                var lCond = binaryExpr.lhs().accept(this).makeCond();
                var trueChain = lCond.jumpTrue();
                code.connect(lCond.falseJumps);
                var rCond = binaryExpr.rhs().accept(this).makeCond();
                return new CondItem(
                        rCond.opcode,
                        Code.Chain.merge(trueChain, rCond.trueJumps),
                        rCond.falseJumps
                );
            }
            else if (binaryExpr.op() == BinOp.ADD && binaryExpr.getType().getUnderlyingType() == Types.instance.getStringType()) {
                binaryExpr.lhs().accept(this).load();
                binaryExpr.rhs().accept(this).load();
                var concat = project.getRootPackage().getFunction(NameTable.instance.concat);
                code.invokeFunction(concat);
                return topStackItem();
            }
            else {
                var xType = binaryExpr.lhs().getType();
                var yType = binaryExpr.rhs().getType();
                var targetType = xType.getTag() >= yType.getTag() ? xType : yType;
                genExpr(binaryExpr.lhs(), targetType).load();
                genExpr(binaryExpr.rhs(), targetType).load();
                binaryExpr.op().apply(targetType, code);
                return topStackItem();
            }
        }

        @Override
        public Item visitCall(Call call) {
            var funcItem = call.getFunc().accept(this);
            return funcItem.invoke(call.getArguments());
        }

        @Override
        public Item visitLiteral(Literal literal) {
            return new ImmediateItem(literal.value());
        }

        @Override
        public Item visitAnonClassExpr(AnonClassExpr expr) {
            expr.getDecl().accept(Gen.this);
            code.new_((ClassType) expr.getType());
            code.dup();
            var method = expr.getDecl().getElement().getPrimaryInit();
            code.invokeSpecial(method);
            return topStackItem();
        }

        @Override
        public Item visitIndexExpr(IndexExpr indexExpr) {
            indexExpr.x().accept(this).load();
            indexExpr.index().accept(this).load();
            return new IndexedItem(indexExpr.getType().getTag());
        }

        @Override
        public Item visitSelectorExpr(SelectorExpr selectorExpr) {
            if (selectorExpr.getElement() instanceof BuiltinVariable builtinVar) {
                if (builtinVar.getName() != Name.this_())
                    throw new AnalysisException("Unexpected built-in var: " + builtinVar.getName().toString());
                loadImplicitSelf((ClassType) builtinVar.getType());
                return getStackitem(builtinVar.getType());
            }
            var member = (MemberRef) selectorExpr.getElement();
            if (member instanceof MethodRef m && m.isInit()
                    && selectorExpr.sel() == m.getDeclType().getClazz().getName()) {
                selectorExpr.x().accept(this).load();
                code.newChild(m.getDeclType());
                return new InitItem(m);
            } else if (member.isStatic()) {
                return new StaticItem(member);
            } else {
                selectorExpr.x().accept(this).load();
                return new MemberItem(member);
            }
        }

        @Override
        public Item visitAssignExpr(AssignExpr assignExpr) {
            var l = assignExpr.lhs().accept(this);
            var op = assignExpr.op();
            if (op == null) {
                genExpr(assignExpr.rhs(), assignExpr.getType()).load();
            }
            else {
                l.dup();
                l.load().cast(assignExpr.getType());
                genExpr(assignExpr.rhs(), assignExpr.getType()).load();
                /*
                 TODO operation type may differ from expression type, e.g.:
                 int a = 0
                 long l = 1
                 a += 1
                 The expression type is int while the operation type is long
                 */
                op.apply(assignExpr.getType(), code);
            }
            return new AssignItem(l);
        }

        @Override
        public Item visitPrefixExpr(PrefixExpr prefixExpr) {
            var op = prefixExpr.op();
            if (op == PrefixOp.INC || op == PrefixOp.DEC) {
                var item = prefixExpr.x().accept(this);
                item.dup();
                item.load();
                op.apply(prefixExpr.getType(), code);
                return new AssignItem(item);
            } else if (op == PrefixOp.NOT) {
                var item = prefixExpr.x().accept(this).makeCond();
                return item.negate();
            } else {
                prefixExpr.x().accept(this).load();
                op.apply(prefixExpr.getType(), code);
                return topStackItem();
            }
        }

        @Override
        public Item visitNode(Node node) {
            throw new IllegalStateException("Expression type '" + node.getClass().getName() + "' is not handled");
        }

        @Override
        public Item visitPostfixExpr(PostfixExpr postfixExpr) {
            var op = postfixExpr.op();
            if (op == PostfixOp.INC || op == PostfixOp.DEC) {
                var item = postfixExpr.x().accept(this);
                item.dup();
                item.load();
                item.stash();
                op.apply(postfixExpr.getType(), code);
                item.store();
            }
            else {
                postfixExpr.x().accept(this).load();
                op.apply(postfixExpr.getType(), code);
            }
            return topStackItem();
        }

        @Override
        public Item visitNewArrayExpr(NewArrayExpr newArrayExpr) {
            var type = (ArrayType) newArrayExpr.getType();
            code.newArray(newArrayExpr.getType());
            for (Expr element : newArrayExpr.getElements()) {
                code.dup();
                genExpr(element, type.getElementType()).load();
                code.arrayAdd();
            }
            return topStackItem();
        }

        @Override
        public Item visitTypeApply(TypeApply typeApply) {
            return typeApply.getExpr().accept(this);
        }

        @Override
        public Item visitIdent(Ident ident) {
            var e = ident.getElement();
            return switch (e) {
                case LocalVar local -> new LocalItem(local);
                case MemberRef member -> {
                    if (member instanceof MethodRef m && m.isInit()
                            && ident.getName() == m.getDeclType().getClazz().getName()) {
                        var ownerType = m.getDeclType().getOwner();
                        if (ownerType != null && !m.getDeclType().isStatic()) {
                            loadImplicitSelf(ownerType);
                            code.newChild(m.getDeclType());
                        }
                        else
                            code.new_(m.getDeclType());
                        yield new InitItem(m);
                    }
                    if (member.isStatic())
                        yield new StaticItem(member);
                    else {
                        loadImplicitSelf(member.getDeclType());
                        yield new MemberItem(member);
                    }
                }
                case FreeFuncRef func -> new FuncItem(func);
                case BuiltinVariable b when (b.getName() == Name.this_() || b.getName() == Name.super_()) -> {
                    if (b.getElement() instanceof MethodRef method) {
                        code.loadThis(env);
                        yield new MemberItem(method);
                    }
                    else
                        yield new ThisItem();
                }
                case null, default -> throw new RuntimeException("Unrecognized element: " + e + ", ref: " + ident.getText());
            };
        }

        private void loadImplicitSelf(ClassType classType) {
            /*
            Important node. DON'T REMOVE!!!
            Structure of the scope stack (left corresponding to top):
            stack: block+
            block: class* (class method) (!method executable)*
             */
            var parentIdx = -1;
            var contextIdx = -1;
            Element last = null;
            for (var s = env.currentScope(); s != null; s = s.getParent()) {
                switch (s.getElement()) {
                    case Executable exe -> {
                        if (parentIdx >= 0) {
                            code.loadParent(parentIdx, (ClassType) requireNonNull(last));
                            parentIdx = -1;
                        }
                        if (exe instanceof MethodRef m) {
                            if (contextIdx >= 0) {
                                assert parentIdx == -1;
                                code.loadContextSlot(contextIdx, 0, m.getDeclType());
                            } else
                                code.load(0, m.getDeclType());
                            if (classType.isAssignableFrom(m.getDeclType()))
                                return;
                            contextIdx = 0;
                        }
                        else {
                            contextIdx++;
                        }
                    }
                    case ClassType ct -> {
                        if (last instanceof MethodRef m) {
                            assert m.getDeclType() == ct;
                        } else {
                            parentIdx++;
                            if (classType.isAssignableFrom(ct)) {
                                code.loadParent(parentIdx, classType);
                                return;
                            }
                        }
                    }
                    default -> {}
                }
                last = s.getElement();
            }
            throw new RuntimeException("Not inside class " + classType.getTypeText());
        }

        @Override
        public Item visitIsExpr(IsExpr isExpr) {
            isExpr.getExpr().accept(this).load();
            var v = isExpr.getVar();
            if (v != null) {
                v.accept(Gen.this);
                code.dup();
                code.store(v.getElement().getIndex());
            }
            code.is(isExpr.getCheckType().getType());
            return intStackItem();
        }
    }


    private Scope enterScope(Node node) {
        return currentScope = new Scope(node, currentScope);
    }

    private Scope findScope(Node node) {
        var s = currentScope;
        while (s != null) {
            if (s.node == node)
                return s;
            s = s.parent;
        }
        throw new IllegalStateException("Cannot find scope for node: " + node);
    }

    private Item getStackitem(Type type) {
        return stackItems[type.getTag()];
    }

    private class Scope implements Closeable {

        private final Node node;
        private final @Nullable Scope parent;
        private @Nullable Code.Chain exit;
        private @Nullable Code.Chain cont;

        private Scope(Node node, @Nullable Scope parent) {
            this.node = node;
            this.parent = parent;
        }

        @Override
        public void close() {
            currentScope = parent;
        }

        void addExit(Code.Chain chain) {
            exit = Code.Chain.merge(chain, exit);
        }

        void addContinue(Code.Chain chain) {
            cont = Code.Chain.merge(chain, cont);
        }

    }


    //<editor-fold desc="Items">
    private abstract class Item {

        final int typeTag;

        private Item(int typeTag) {
            this.typeTag = typeTag;
        }

        Item load() {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support load");
        }

        abstract void dup();

        abstract void stash();

        void store() {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support store");
        }

        abstract void drop();

        Item invoke(List<Expr> args) {
            load();
            var funcType = (FuncType) code.peek();
            loadArgs(args, funcType.getParamTypes());
            code.call(funcType);
            return getStackitem(funcType.getRetType());
        }

        CondItem makeCond() {
            load();
            return new CondItem(Bytecodes.IF_NE, null, null);
        }

        Item cast(Type type) {
            if (typeTag >= TypeTags.TAG_INT && typeTag <= TypeTags.TAG_DOUBLE) {
                var item = cast0(type);
                if (item == null)
                    throw new IllegalStateException("Cannot cast item of type tag " + typeTag + " to type " + type.getTypeText());
                return item;
            }
            else
                return this;
        }

        private Item cast0(Type type) {
            assert typeTag >= TypeTags.TAG_INT && typeTag <= TypeTags.TAG_DOUBLE;
            var sourceTag = type.toStackType().getTag();
            if (sourceTag == typeTag || sourceTag == TypeTags.TAG_ANY)
                return this;
            if (type instanceof UnionType ut) {
                for (Type alt : ut.alternatives()) {
                    var item = cast0(alt);
                    if (item != null)
                        return item;
                }
                return null;
            } else if (sourceTag > typeTag && sourceTag <= TypeTags.TAG_DOUBLE) {
                load();
                code.castPrim(typeTag, type);
                return stackItems[type.getTag()];
            }
            else
                return null;
        }

    }

    private class AssignItem extends Item {

        private final Item lhs;

        private AssignItem(Item lhs) {
            super(lhs.typeTag);
            this.lhs = lhs;
        }

        @Override
        public void dup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stash() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Item load() {
            lhs.stash();
            lhs.store();
            return stackItems[typeTag];
        }

        @Override
        public void drop() {
            lhs.store();
        }
    }

    private class CondItem extends Item {

        private final int opcode;
        private final @Nullable Code.Chain trueJumps;
        private final @Nullable Code.Chain falseJumps;

        public CondItem(int opcode, @Nullable Code.Chain trueJumps, @Nullable Code.Chain falseJumps) {
            super(TypeTags.TAG_INT);
            this.opcode = opcode;
            this.trueJumps = trueJumps;
            this.falseJumps = falseJumps;
        }

        Code.Chain jumpTrue() {
            return Code.Chain.merge(trueJumps, code.branch(opcode, 0));
        }

        Code.Chain jumpFalse() {
            return Code.Chain.merge(falseJumps, code.branch(negateOp(opcode), 0));
        }

        public CondItem negate() {
            return new CondItem(negateOp(opcode), falseJumps, trueJumps);
        }

        private static int negateOp(int opcode) {
            return switch (opcode) {
                case Bytecodes.IF_EQ ->  Bytecodes.IF_NE;
                case Bytecodes.IF_NE ->  Bytecodes.IF_EQ;
                default -> throw new IllegalStateException("Unrecognized opcode: " + opcode);
            };
        }

        @Override
        public Item load() {
            var falseChain = jumpFalse();
            code.connect(trueJumps);
            code.ldc(1);
            var trueChain = code.goto_(0);
            code.connect(falseChain);
            code.ldc(0);
            code.connect(trueChain);
            return intStackItem();
        }

        @Override
        public void dup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stash() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void drop() {
            load().drop();
        }
    }

    private class IndexedItem extends Item {

        private IndexedItem(int typeTag) {
            super(typeTag);
        }

        @Override
        public Item load() {
            code.arrayLoad();
            return stackItems[typeTag];
        }

        @Override
        public void dup() {
            code.dup2();
        }

        @Override
        public void stash() {
            code.dupX2();
        }

        @Override
        public void store() {
            code.arrayStore();
        }

        @Override
        public void drop() {
            code.pop();
            code.pop();
        }
    }

    private class VoidItem extends Item {

        private VoidItem() {
            super(TypeTags.TAG_VOID);
        }

        @Override
        public void dup() {
        }

        @Override
        public void stash() {
            code.dup();
        }

        @Override
        public void drop() {

        }
    }

    private class StaticItem extends Item {

        private final MemberRef member;

        protected StaticItem(MemberRef member) {
            super(member.getType().getTag());
            this.member = member;
        }

        @Override
        public Item load() {
            member.load(code, env);
            return stackItems[typeTag];
        }

        @Override
        Item invoke(List<Expr> args) {
            var funcType = (FuncType) member.getType();
            loadArgs(args, funcType.getParamTypes());
            member.invoke(code, env);
            return getStackitem(funcType.getRetType());
        }

        @Override
        public void dup() {
        }

        @Override
        public void stash() {
            code.dup();
        }

        @Override
        public void store() {
           member.store(code, env);
        }

        @Override
        public void drop() {

        }

    }

    private class MemberItem extends Item {

        private final MemberRef member;

        protected MemberItem(MemberRef member) {
            super(member.getType().getTag());
            this.member = member;
        }

        @Override
        public Item load() {
            member.load(code, env);
            return stackItems[typeTag];
        }

        @Override
        Item invoke(List<Expr> args) {
            var funcType = (FuncType) member.getType();
            loadArgs(args, funcType.getParamTypes());
            member.invoke(code, env);
            return getStackitem(funcType.getRetType());
        }

        @Override
        public void dup() {
            code.dup();
        }

        @Override
        public void stash() {
            code.dupX1();
        }

        @Override
        public void store() {
            member.store(code, env);
        }

        @Override
        public void drop() {
            code.pop();
        }

    }

    private class InitItem extends Item {

        private final MethodRef init;

        private InitItem(MethodRef init) {
            super(TypeTags.TAG_CLASS);
            this.init = init;
        }

        @Override
        void dup() {
            code.dup();
        }

        @Override
        void stash() {
            code.dupX1();
        }

        @Override
        void drop() {
            code.pop();
        }

        @Override
        Item invoke(List<Expr> args) {
            return new InitResultItem(this, args);
        }
    }

    private void loadArgs(List<Expr> args, List<Type> paramTypes) {
        var it = paramTypes.iterator();
        args.forEach(arg -> genExpr(arg, it.next()).load());
    }

    private class InitResultItem extends Item {

        private final InitItem initItem;
        private final List<Expr> args;

        private InitResultItem(InitItem initItem, List<Expr> args) {
            super(initItem.init.getDeclType().getTag());
            this.initItem = initItem;
            this.args = args;
        }

        @Override
        Item load() {
            code.dup();
            actualInvoke();
            return getStackitem(initItem.init.getDeclType());
        }

        private void actualInvoke() {
            loadArgs(args, initItem.init.getParamTypes());
            initItem.init.invoke(code, env);
        }

        @Override
        void dup() {
            load();
            code.dup();
        }

        @Override
        void stash() {
            load();
            code.dupX1();
        }

        @Override
        void drop() {
            actualInvoke();
        }
    }

    private class ThisItem extends Item {

        private ThisItem() {
            super(TypeTags.TAG_CLASS);
        }

        @Override
        void dup() {
        }

        @Override
        Item load() {
            code.loadThis(env);
            return stackItems[TypeTags.TAG_CLASS];
        }

        @Override
        void stash() {
            code.dup();
        }

        @Override
        void drop() {
        }
    }

    private class LocalItem extends Item {

        private final LocalVar variable;

        protected LocalItem(LocalVar variable) {
            super(variable.getType().getTag());
            this.variable = variable;
        }

        @Override
        public Item load() {
            variable.load(code, env);
            return stackItems[variable.getType().getTag()];
        }

        @Override
        public void dup() {
        }

        @Override
        public void store() {
            code.store(variable.getIndex());
        }

        @Override
        public void drop() {
        }

        @Override
        public void stash() {
            code.dup();
        }

    }

    private class ImmediateItem extends Item {

        private final Object value;

        public ImmediateItem(Object value) {
            super(new LiteralValue(value).getType().getTag());
            this.value = value;
        }

        @Override
        public Item load() {
            code.ldc(value);
            return stackItems[typeTag];
        }

        @Override
        public void dup() {
        }

        @Override
        public void stash() {
           code.dup();
        }

        @Override
        public void drop() {

        }
    }

    private class StackItem extends Item {

        StackItem(int tag) {
            super(tag);
        }

        @Override
        Item load() {
            return this;
        }

        @Override
        public void dup() {
            code.dup();
        }

        @Override
        public void stash() {
            code.dupX1();
        }

        @Override
        public void drop() {
            code.pop();
        }
    }

    private class FuncItem extends Item {

        private final FreeFuncRef func;

        FuncItem(FreeFuncRef func) {
            super(TypeTags.TAG_FUNCTION);
            this.func = func;
        }

        @Override
        Item invoke(List<Expr> args) {
            loadArgs(args, func.getParamTypes());
            code.invokeFunction(func);
            return getStackitem(func.getRetType());
        }

        @Override
        public void dup() {
            code.dup();
        }

        @Override
        public void stash() {
            code.dupX1();
        }

        @Override
        public void drop() {
        }

    }

    //</editor-fold>


}
