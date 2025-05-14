package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;

import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class Attr extends StructuralNodeVisitor {

    private final Project project;

    public Attr(Project project) {
        this.project = project;
    }

    @Override
    public Void visitExtend(Extend extend) {
        extend.getArgs().forEach(arg -> attrExpr(arg, PrimitiveType.ANY).resolve());
        return null;
    }

    @Override
    public Void visitForeachStmt(ForeachStmt foreachStmt) {
        var varDecl = foreachStmt.getVar();
        var v = varDecl.getElement();
        var expr = foreachStmt.getExpr();
        attrExpr(expr, PrimitiveType.ANY).resolve();
        if (expr.getType() instanceof ClassType type) {
            var iterableClass = project.getRootPackage().subPackage("java")
                    .subPackage("lang").getClass("Iterable");
            var iterableType = Objects.requireNonNull(type.asSuper(iterableClass),
                    () -> "Foreach expression type '" + type.getTypeText() + "' is not an iterable type");
            var varType = iterableType.getTypeArguments().head();
            v.setType(varType);
        }
        else if (expr.getType() instanceof ArrayType arrayType)
            v.setType(arrayType.getElementType());
        else
            throw new AnalysisException("Cannot iterate over type: " + expr.getType().getTypeText());
        foreachStmt.getBody().accept(this);
        return null;
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
        var v = localVarDecl.getElement();
        var initial = localVarDecl.getInitial();
        if (initial != null)
            attrExpr(initial, v.getType()).resolve();
        if (localVarDecl.getType() == null) {
            if (initial == null)
                throw new AnalysisException("Auto variable must have initial value");
            v.setType(initial.getType());
        }
        return null;
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        var field = fieldDecl.getElement();
        var initial = fieldDecl.getInitial();
        if (initial != null)
            attrExpr(initial, field.getType()).resolve();
        if (fieldDecl.getType() == null) {
            if (initial == null)
                throw new AnalysisException("Auto variable must have initial value");
            field.setType(initial.getType());
        }
        return null;
    }

    @Override
    public Void visitEnumConstDecl(EnumConstDecl enumConstDecl) {
        for (Expr argument : enumConstDecl.getArguments())
            attrExpr(argument, PrimitiveType.ANY).resolve();
        var clazz = enumConstDecl.getActualClass();
        var argTypes = enumConstDecl.getArguments().map(Expr::getType);
        var init = Objects.requireNonNull(resolveInit(clazz, argTypes));
        if (enumConstDecl.getDecl() != null) {
            enumConstDecl.getDecl().accept(this);
        }
        enumConstDecl.setInit(init);
        return null;
    }

    @Override
    public Void visitReturnStmt(RetStmt retStmt) {
        if (retStmt.result() != null)
            attrExpr(retStmt.result(), retStmt.getType()).resolve();
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        attrExpr(exprStmt.expr(), PrimitiveType.ANY).resolve();
        return null;
    }

    @Override
    public Void visitExpr(Expr expr) {
        throw new IllegalStateException("Unhandled expression: " + expr.getText());
    }

    @Override
    public Void visitTypeNode(TypeNode typeNode) {
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        attrExpr(ifStmt.cond(), PrimitiveType.BOOL).resolve();
        ifStmt.body().accept(this);
        if (ifStmt.else_() != null)
            ifStmt.else_().accept(this);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        attrExpr(whileStmt.cond(), PrimitiveType.BOOL).resolve();
        whileStmt.body().accept(this);
        return null;
    }

    @Override
    public Void visitDoWhileStmt(DoWhileStmt doWhileStmt) {
        doWhileStmt.body().accept(this);
        attrExpr(doWhileStmt.cond(), PrimitiveType.BOOL).resolve();
        return null;
    }

    @Override
    public Void visitThrowStmt(ThrowStmt throwStmt) {
        attrExpr(throwStmt.expr(), PrimitiveType.ANY).resolve();
        return null;
    }

    private Resolver attrExpr(Expr expr, Type type) {
        if (Traces.traceAttr)
            log.trace("Attributing expression {}", expr.getText());
        if (expr.getStatus() == ExprStatus.RESOLVED)
            return new ImmediateResolver(expr.getElement());
        if (expr.getStatus() == ExprStatus.RESOLVING)
            throw new AnalysisException("Circular reference");
        if (expr instanceof LambdaExpr lambdaExpr) {
            if (type instanceof FuncType funcType)
                lambdaExpr.setTargetType(funcType);
        }
        var resolver = expr.accept(new ExprAttr());
        expr.setStatus(ExprStatus.RESOLVED);
        return resolver;
    }

    private void ensureElementTyped(Element element) {
        if (element instanceof Variable variable && variable.getType() instanceof DeferredType) {
            var templateVar = variable instanceof FieldInst fieldInst ? fieldInst.field() : variable;
            var varDecl = (VariableDecl<?>) templateVar.getNode();
            attrExpr(Objects.requireNonNull(varDecl.getInitial()), Types.instance.getNullableAny());
            templateVar.setType(Objects.requireNonNull(varDecl.getInitial()).getType());
        }
    }

    private class ExprAttr extends AbstractNodeVisitor<Resolver> {

        @Override
        public Resolver visitNode(Node node) {
            throw new RuntimeException("Unhandled expression class: " + node.getClass().getName());
        }

        @Override
        public Resolver visitRangeExpr(RangeExpr rangeExpr) {
            rangeExpr.getMin().accept(this).resolve();
            rangeExpr.getMax().accept(this).resolve();
            rangeExpr.setType(Types.instance.getArrayType(PrimitiveType.INT));
            return new NullResolver();
        }

        @Override
        public Resolver visitIdent(Ident ident) {
            return new PendingResolver(ident, ident.getCandidates());
        }

        @Override
        public Resolver visitNewArrayExpr(NewArrayExpr newArrayExpr) {
            newArrayExpr.setType(Types.instance.getArrayType(newArrayExpr.elementType().getType()));
            return new NullResolver();
        }

        @Override
        public Resolver visitCondExpr(CondExpr condExpr) {
            condExpr.getCond().accept(this).resolve();
            condExpr.getFalsePart().accept(this).resolve();
            condExpr.getTruePart().accept(this).resolve();
            var type1 = condExpr.getTruePart().getType();
            var type2 = condExpr.getFalsePart().getType();
            Type type;
            if (type1 instanceof PrimitiveType pt1 && type2 instanceof PrimitiveType pt2) {
                if (pt1.widensTo(pt2))
                    type = pt2;
                else if(pt2.widensTo(pt1))
                    type = pt1;
                else
                    type = PrimitiveType.ANY;
            }
            else
                type = Types.getLUB(List.of(type1, type2));
            condExpr.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitIsExpr(IsExpr isExpr) {
            isExpr.getExpr().accept(this).resolve();
            isExpr.setType(PrimitiveType.BOOL);
            return new NullResolver();
        }

        @Override
        public Resolver visitPrefixExpr(PrefixExpr prefixExpr) {
            prefixExpr.x().accept(this).resolve();
            prefixExpr.setType(prefixExpr.op().getType(prefixExpr.x().getType()));
            return new NullResolver();
        }

        @Override
        public Resolver visitPostfixExpr(PostfixExpr postfixExpr) {
            postfixExpr.x().accept(this).resolve();
            postfixExpr.setType(postfixExpr.op().getType(postfixExpr.x().getType()));
            return new NullResolver();
        }

        @Override
        public Resolver visitCastExpr(CastExpr castExpr) {
            castExpr.expr().accept(this).resolve();
            castExpr.setType(castExpr.type().getType());
            return new NullResolver();
        }

        @Override
        public Resolver visitBinaryExpr(BinaryExpr binaryExpr) {
            binaryExpr.lhs().accept(this).resolve(e -> e instanceof Variable v ? v : null);
            binaryExpr.rhs().accept(this).resolve(e -> e instanceof Variable v ? v : null);
            var xType = binaryExpr.lhs().getType();
            var yType = binaryExpr.rhs().getType();
            var type = binaryExpr.op().getType(xType, yType);
            binaryExpr.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitLiteral(Literal literal) {
            var type = switch (literal.value()) {
                case Byte ignored -> PrimitiveType.BYTE;
                case Short ignored -> PrimitiveType.SHORT;
                case Integer ignored -> PrimitiveType.INT;
                case Long ignored -> PrimitiveType.LONG;
                case Float ignored -> PrimitiveType.FLOAT;
                case Double ignored -> PrimitiveType.DOUBLE;
                case Boolean ignored -> PrimitiveType.BOOL;
                case Character ignored -> PrimitiveType.CHAR;
                case String ignored -> Types.instance.getStringType();
                case null -> PrimitiveType.NULL;
                default -> throw new IllegalStateException("Unrecognized literal: " + literal);
            };
            literal.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitAnonClassExpr(AnonClassExpr anonClassExpr) {
            var type = anonClassExpr.getDecl().getElement();
            anonClassExpr.getDecl().accept(Attr.this);
            anonClassExpr.setType(type);
            return new NullResolver();
        }


        private Type resolveArgumentType(Expr argument) {
            if (argument instanceof LambdaExpr lambdaExpr)
               return lambdaExpr.getElement().getType();
            else {
                argument.accept(this).resolve();
                return argument.getType();
            }
        }

        private void completeArgumentResolution(List<Expr> args, List<Type> paramTypes) {
            var paramTypeIt = paramTypes.iterator();
            for (var arg : args) {
                var paramType = paramTypeIt.next();
                if (arg instanceof LambdaExpr lambdaExpr) {
                    attrExpr(lambdaExpr, paramType).resolve();
                }
            }
        }

        @Override
        public Resolver visitAssignExpr(AssignExpr assignExpr) {
            assignExpr.lhs().accept(this).resolve();
            var type = assignExpr.lhs().getType();
            attrExpr(assignExpr.rhs(), type).resolve();
            assignExpr.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitIndexExpr(IndexExpr indexExpr) {
            indexExpr.x().accept(this).resolve();
            indexExpr.index().accept(this).resolve();
            var arrayType = (ArrayType) indexExpr.x().getType();
            indexExpr.setType(arrayType.getElementType());
            return new NullResolver();
        }

        @Override
        public Resolver visitSelectorExpr(SelectorExpr selectorExpr) {
            var e = selectorExpr.x().accept(this).resolve(e0 ->
                    e0 instanceof ClassType || e0 instanceof Variable ? e0 : null
            );
            var table = switch (e) {
                case ClassType ct -> ct.getTable();
                case Package pkg -> pkg.getTable();
                case null, default -> selectorExpr.x().getType().getTable();
            };
            var candidates = table.lookupAll(selectorExpr.sel());
            return new PendingResolver(selectorExpr, candidates);
        }

        @Override
        public Resolver visitTypeApply(TypeApply typeApply) {
            var r = typeApply.getExpr().accept(this);
            return new Resolver() {
                @Override
                Element resolve() {
                    return resolve(e -> e);
                }

                @Override
                Element resolve(Function<Element, Element> mapper) {
                    return r.resolve(e -> {
                        if (e instanceof GenericDecl genDecl
                                && genDecl.getTypeParams().size() == typeApply.getArgs().size()) {
                            var inst = genDecl.getInst(typeApply.getArgs().map(TypeNode::getType));
                            var resolved = mapper.apply(inst);
                            if (resolved != null) {
                                typeApply.setElement(resolved);
                                return resolved;
                            }
                        }
                        return null;
                    });
                }
            };
        }

        @Override
        public Resolver visitCall(Call call) {
            var funcResolver = call.getFunc().accept(this);
            var argTypes = call.getArguments().map(this::resolveArgumentType);
            var func = (ValueElement) funcResolver.resolve(
                    e -> {
                        if (e instanceof ValueElement v)
                            return resolveFunc(v, argTypes);
                        else if (e instanceof ClassType ct)
                            return resolveInit(ct, argTypes);
                        return null;
                    }
            );
            var funcType = (FuncType) call.getFunc().getType();
            completeArgumentResolution(call.getArguments(), funcType.getParamTypes());
            if (func != null)
                call.setElement(func);
            return new NullResolver();
        }

        @Override
        public Resolver visitLambdaExpr(LambdaExpr lambdaExpr) {
            var lambda = lambdaExpr.getElement();
            lambdaExpr.setType(Types.instance.getFuncType(
                    lambda.getParams().map(Param::getType),
                    lambda.getRetType()
            ));
            if (lambdaExpr.body() instanceof Expr expr)
                attrExpr(expr, lambda.getRetType()).resolve();
            else
                lambdaExpr.body().accept(Attr.this);
            return new ImmediateResolver(lambda);
        }

    }

    private abstract class Resolver {

        abstract Element resolve();

        abstract Element resolve(Function<Element, Element> mapper);

    }

    private class ImmediateResolver extends Resolver {
        private final Element resolved;

        private ImmediateResolver(Element resolved) {
            ensureElementTyped(resolved);
            this.resolved = resolved;
        }

        @Override
        Element resolve() {
            return resolved;
        }

        @Override
        Element resolve(Function<Element, Element> mapper) {
            return resolved;
        }

    }

    private class PendingResolver extends Resolver {
        private final Iterable<Element> candidates;
        private final Expr expr;

        public PendingResolver(Expr expr, Iterable<Element> candidates) {
            this.expr = expr;
            this.candidates = candidates;
        }

        public Element resolve(Function<Element, Element> mapper) {
            for (Element candidate : candidates) {
                ensureElementTyped(candidate);
                Element resolved;
                if ((resolved = mapper.apply(candidate)) != null) {
                    onResolved(resolved);
                    return resolved;
                }
            }
            throw new AnalysisException("Cannot resolve expression '" + expr.getText() + "'");
        }

        Element resolve() {
            var it = candidates.iterator();
            if (!it.hasNext())
                throw new AnalysisException("Cannot resolve expression '" + expr.getText() + "'");
            var resolved = Objects.requireNonNull(it.next());
            ensureElementTyped(resolved);
            onResolved(resolved);
            return resolved;
        }

        private void onResolved(Element resolved) {
            expr.setElement(resolved);
        }

    }

    private class NullResolver extends Resolver {

        @Override
        Element resolve() {
            return null;
        }

        @Override
        Element resolve(Function<Element, Element> mapper) {
            return null;
        }

    }

    private Element resolveFunc(ValueElement element, List<Type> argumentTypes) {
        if (Traces.traceAttr) {
            log.trace("Checking applicable of element {}, type: {}, with argument types: {}",
                    element.getName(),element.getType().getTypeText(), argumentTypes.map(Type::getTypeText).join(", "));
        }
        if (element.getType() instanceof FuncType funcType &&
            funcType.getParamTypes().matches(argumentTypes, this::isApplicable))
            return element;
        else
            return null;
    }

    private MethodRef resolveInit(ClassType type, List<Type> argumentTypes) {
        var inits = type.getTable().lookupAll(
                Name.init(),
                i -> i instanceof MethodRef m && m.getDeclType().getClazz() == type.getClazz()
        );
        if (Traces.traceAttr) {
            log.trace("Resolving constructor. class: {}, arg types: [{}]",
                    type.getTypeText(),
                    argumentTypes.map(Type::getTypeText).join(", ")
            );
        }
        for (var init : inits) {
            var m = (MethodRef) init;
            if (Traces.traceAttr) {
                log.trace("Trying init: {}", init.getText());
            }
            if (m.getParamTypes().matches(argumentTypes, this::isApplicable)) {
                return m;
            }
        }
        return null;
    }

    private boolean isApplicable(Type parameterType, Type argumentType) {
        if (parameterType.isAssignableFrom(argumentType))
            return true;
        if (parameterType instanceof PrimitiveType pt1 && argumentType instanceof PrimitiveType pt2)
            return pt2.widensTo(pt1);
        return false;
    }

}
