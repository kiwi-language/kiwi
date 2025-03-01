package org.metavm.compiler.generate;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ArrayType;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.flow.Bytecodes;
import org.metavm.util.NamingUtils;

import javax.annotation.Nullable;

@Slf4j
public class Generator extends StructuralNodeVisitor {

    private Clazz clazz;
    private Code code;
    private final StackItem stackItem = new StackItem();
    private final VoidItem voidItem = new VoidItem();

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        var prevClazz = clazz;
        clazz = classDecl.getElement();
        processAnnotations(classDecl);
        var cp = clazz.getConstantPool();
        clazz.getInterfaces().forEach(cp::put);
        clazz.getTypeParameters().forEach(tp -> cp.put(tp.getBound()));
        super.visitClassDecl(classDecl);
        clazz = prevClazz;
        cp.freeze();
        return null;
    }

    private void processAnnotations(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        var attrs = List.<Attribute>builder();
        for (Annotation annotation : classDecl.getAnnotations()) {
            if (annotation.getName() == SymName.Component()) {
                attrs.append(new Attribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT));
                attrs.append(new Attribute(AttributeNames.BEAN_NAME,
                        NamingUtils.firstCharToLowerCase(clazz.getName().toString())));
            } else if (annotation.getName() == SymName.Searchable()) {
                clazz.setSearchable(true);
            } else if (annotation.getName() == SymName.Tag()) {
                clazz.setSourceTag(Integer.parseInt(annotation.getAttributes().getFirst().getValue().getText()));
            }
        }
        clazz.setAttributes(attrs.build());
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        var field = fieldDecl.getElement();
        var cp = clazz.getConstantPool();
        cp.put(field.getType());
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
        var initial = localVarDecl.initial();
        code.newLocal(localVarDecl.getElement());
        if (initial != null) {
            genExpr(initial).load();
            code.store(localVarDecl.getElement().getIndex());
        }
        return null;
    }

    @Override
    public Void visitEnumConstantDecl(EnumConstantDecl enumConstantDecl) {
        var ec = enumConstantDecl.getElement();
        var cp = clazz.getConstantPool();
        cp.put(ec.getType());
        return super.visitEnumConstantDecl(enumConstantDecl);
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        code.newLocal(paramDecl.getElement());
        return super.visitParamDecl(paramDecl);
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        var method = methodDecl.getElement();
        if (Traces.traceGeneration)
            log.trace("Generating code for method: {}", method.getQualifiedName());
        var prevCode = code;
        var cp = method.getConstantPool();
        method.getParameterTypes().forEach(cp::put);
        cp.put(method.getReturnType());
        clazz.getTypeParameters().forEach(tp -> cp.put(tp.getBound()));
        code = method.getCode();
        if (!method.isStatic() && !method.isAbstract()) {
            code.newLocal(new LocalVariable(
                    SymName.this_(),
                    method.getDeclaringClass().getType(),
                    method
            ));
        }
        super.visitMethodDecl(methodDecl);
        if (!method.isAbstract() && code.isAlive()) {
            if (!method.getReturnType().isVoid())
                throw new RuntimeException("Code of method '" + method.getQualifiedName() + "' is not properly terminated");
            code.voidRet();
        }
        code = prevCode;
        cp.freeze();
        return null;
    }

    @Override
    public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
        var prevCode = code;
        var lambda = (Lambda) lambdaExpr.getElement();
        code = lambda.getCode();
        var cp = code.getExecutable().getConstantPool();
        lambda.getParameters().forEach(p -> cp.put(p.getType()));
        cp.put(lambda.getReturnType());
        super.visitLambdaExpr(lambdaExpr);
        code = prevCode;
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        genExpr(exprStmt.expr()).drop();
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        var r = returnStmt.result();
        if (r != null) {
            genExpr(r).load();
            code.ret();
        }
        else
            code.voidRet();
        return null;
    }

    @Override
    public Void visitThrowStmt(ThrowStmt throwStmt) {
        genExpr(throwStmt.expr()).load();
        code.raise();
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        var item = genExpr(ifStmt.cond()).makeCond();
        var falseChain = item.jumpFalse();
        ifStmt.body().accept(this);
        var els = ifStmt.else_();
        if (els != null) {
            var trueChain = code.goto_(0);
            code.connect(falseChain);
            els.accept(this);
            code.connect(trueChain);
        }
        else
            code.connect(falseChain);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        var pc = code.pc();
        var item = genExpr(whileStmt.cond()).makeCond();
        var chain = item.jumpFalse();
        code.connect(item.trueJumps);
        whileStmt.body().accept(this);
        code.goto_(pc - code.pc());
        code.connect(chain);
        return null;
    }

    @Override
    public Void visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
        var v = typeVariableDecl.getElement();
        v.getGenericDeclaration().getConstantPool().put(v.getBound());
        return super.visitTypeVariableDecl(typeVariableDecl);
    }

    @Override
    public Void visitExpr(Expr expr) {
        genExpr(expr);
        return null;
    }

    private Item genExpr(Expr expr) {
        if (Traces.traceGeneration)
            log.trace("Generating expression: {}", expr.getText());
        return expr.accept(new ExprVisitor());
    }

    private class ExprVisitor extends AbstractNodeVisitor<Item> {

        @Override
        public Item visitCondExpr(CondExpr condExpr) {
            var item = condExpr.getCond().accept(this).makeCond();
            var falseChain = item.jumpFalse();
            condExpr.getTruePart().accept(this).load();
            var trueChain = code.goto_(0);
            code.connect(falseChain);
            condExpr.getFalsePart().accept(this).load();
            code.connect(trueChain);
            return stackItem;
        }

        @Override
        public Item visitLambdaExpr(LambdaExpr lambdaExpr) {
            if (Traces.traceGeneration)
                log.trace("Generating code for lambda: {}", lambdaExpr.getText());
            var prevCode = code;
            var lambda = lambdaExpr.getElement();
            var cp = lambda.getFunction().getConstantPool();
            lambda.getParameters().forEach(p -> cp.put(p.getType()));
            cp.put(lambda.getReturnType());
            code = lambda.getCode();
            lambdaExpr.forEachChild(e -> e.accept(Generator.this));
            code = prevCode;
            code.lambda(lambda);
            return stackItem;
        }

        @Override
        public Item visitCastExpr(CastExpr castExpr) {
            castExpr.expr().accept(this).load();
            code.cast(castExpr.getType());
            return stackItem;
        }

        @Override
        public Item visitBinaryExpr(BinaryExpr binaryExpr) {
            if (binaryExpr.op() == BinOp.AND) {
                var lCond = binaryExpr.x().accept(this).makeCond();
                var falseChain = lCond.jumpFalse();
                code.connect(lCond.trueJumps);
                var rCond = binaryExpr.y().accept(this).makeCond();
                return new CondItem(
                        rCond.opcode,
                        rCond.trueJumps,
                        Code.Chain.merge(falseChain, rCond.falseJumps)
                );
            }
            else if (binaryExpr.op() == BinOp.OR) {
                var lCond = binaryExpr.x().accept(this).makeCond();
                var trueChain = lCond.jumpTrue();
                code.connect(lCond.falseJumps);
                var rCond = binaryExpr.y().accept(this).makeCond();
                return new CondItem(
                        rCond.opcode,
                        Code.Chain.merge(trueChain, rCond.trueJumps),
                        rCond.falseJumps
                );
            }
            else {
                binaryExpr.x().accept(this).load();
                binaryExpr.y().accept(this).load();
                binaryExpr.op().apply(binaryExpr.x().getType(), code);
                return stackItem;
            }
        }

        @Override
        public Item visitCallExpr(CallExpr callExpr) {
            var funcItem = callExpr.getFunc().accept(this);
            var funcType = funcItem.prepareInvoke();
            for (Expr argument : callExpr.getArguments()) {
                argument.accept(this).load();
            }
            funcItem.invoke(funcType);
            return getStackitem(funcType.getReturnType());
        }

        @Override
        public Item visitLiteral(Literal literal) {
            return new ImmediateItem(literal.value());
        }

        @Override
        public Item visitNewExpr(NewExpr newExpr) {
            if (newExpr.owner() != null) {
                newExpr.owner().accept(this).load();
                code.newChild((ClassType) newExpr.getType());
            }
            else
                code.new_((ClassType) newExpr.getType());
            code.dup();
            newExpr.arguments().forEach(e -> e.accept(this).load());
            var method = (MethodInst) newExpr.getElement();
            code.invokeSpecial(method);
            return stackItem;
        }

        @Override
        public Item visitIndexExpr(IndexExpr indexExpr) {
            indexExpr.x().accept(this).load();
            indexExpr.index().accept(this).load();
            return new IndexedItem();
        }

        @Override
        public Item visitSelectorExpr(SelectorExpr selectorExpr) {
            var field = (MemberRef) selectorExpr.getElement();
            if (field.isStatic()) {
                return new StaticItem(field);
            } else {
                selectorExpr.x().accept(this).load();
                return new MemberItem(field);
            }
        }

        @Override
        public Item visitAssignExpr(AssignExpr assignExpr) {
            var l = assignExpr.lhs().accept(this);
            var op = assignExpr.op();
            if (op == null) {
                assignExpr.rhs().accept(this).load();
                return new AssignItem(l);
            }
            else {
                l.dup();
                l.load();
                assignExpr.rhs().accept(this).load();
                /*
                 TODO operation type may differ from expression type, e.g.:
                 int a = 0
                 long l = 1
                 a += 1
                 The expression type is int while the operation type is long
                 */
                op.apply(assignExpr.getType(), code);
                return new AssignItem(l);
            }
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
                return stackItem;
            }
        }

        @Override
        public Item visitNode(Node node) {
            throw new IllegalStateException("Expression type '" + node.getClass().getName() + "' is not handled");
        }

        @Override
        public Item visitTypeApply(TypeApply typeApply) {
            return null;
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
                return stackItem;
            }
            else {
                postfixExpr.x().accept(this).load();
                op.apply(postfixExpr.getType(), code);
                return stackItem;
            }
        }

        @Override
        public Item visitNewArrayExpr(NewArrayExpr newArrayExpr) {
            code.newArray(newArrayExpr.getType());
            for (Expr element : newArrayExpr.getElements()) {
                code.dup();
                element.accept(this).load();
                code.arrayAdd();
            }
            return stackItem;
        }

        @Override
        public Item visitRefExpr(RefExpr refExpr) {
            var e = refExpr.getElement();
            return switch (e) {
                case LocalVariable local -> new LocalItem(local);
                case MemberRef member -> {
                    if (member.isStatic())
                        yield new StaticItem(member);
                    else {
                        code.loadThis();
                        yield new MemberItem(member);
                    }
                }
                case FreeFuncInst func -> new FuncItem(func);
                case BuiltinVariable b when (b.getName() == SymName.this_() || b.getName() == SymName.super_()) -> {
                    if (b.getElement() instanceof MethodInst method) {
                        code.loadThis();
                        yield new MemberItem(method);
                    }
                    else
                        yield new ThisItem();
                }
                case null, default -> throw new RuntimeException("Unrecognized element: " + e + ", ref: " + refExpr.getText());
            };
        }

        @Override
        public Item visitIsExpr(IsExpr isExpr) {
            isExpr.getExpr().accept(this).load();
            code.is(isExpr.getCheckType().getType());
            return stackItem;
        }
    }

    private Item getStackitem(Type type) {
        return type.isVoid() ? voidItem : stackItem;
    }

    //<editor-fold desc="Items">
    private abstract class Item {

        Item load() {
            throw new UnsupportedOperationException();
        }

        abstract void dup();

        abstract void stash();

        void store() {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support store");
        }

        abstract void drop();

        FunctionType prepareInvoke() {
            load();
            return (FunctionType) code.peek();
        }

        void invoke(FunctionType type) {
            code.call(type);
        }

        CondItem makeCond() {
            load();
            return new CondItem(Bytecodes.IF_NE, null, null);
        }

    }

    private class AssignItem extends Item {

        private final Item lhs;

        private AssignItem(Item lhs) {
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
            return stackItem;
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
            return stackItem;
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

        @Override
        public Item load() {
            code.arrayLoad();
            return stackItem;
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
            this.member = member;
        }

        @Override
        public Item load() {
            member.load(code);
            return stackItem;
        }

        @Override
        FunctionType prepareInvoke() {
            return (FunctionType) member.getType();
        }

        @Override
        void invoke(FunctionType type) {
            member.invoke(code);
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
           member.store(code);
        }

        @Override
        public void drop() {

        }

    }

    private class MemberItem extends Item {

        private final MemberRef member;

        protected MemberItem(MemberRef member) {
            this.member = member;
        }

        @Override
        public Item load() {
            member.load(code);
            return stackItem;
        }

        @Override
        FunctionType prepareInvoke() {
            return (FunctionType) member.getType();
        }

        @Override
        void invoke(FunctionType type) {
            member.invoke(code);
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
            member.store(code);
        }

        @Override
        public void drop() {
            code.pop();
        }

    }

    private class ThisItem extends Item {

        @Override
        void dup() {
        }

        @Override
        Item load() {
            code.loadThis();
            return stackItem;
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

        private final LocalVariable variable;

        protected LocalItem(LocalVariable variable) {
            this.variable = variable;
        }

        @Override
        public Item load() {
            variable.load(code);
            return stackItem;
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
            this.value = value;
        }

        @Override
        public Item load() {
            code.ldc(value);
            return stackItem;
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

        StackItem() {
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

        private final FreeFuncInst func;

        FuncItem(FreeFuncInst func) {
            this.func = func;
        }

        @Override
        FunctionType prepareInvoke() {
            return func.getType();
        }

        @Override
        void invoke(FunctionType type) {
            code.invokeFunction(func);
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
