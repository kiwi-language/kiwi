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

    private final Env env = new Env();

    @Override
    public Void visitDeclStmt(DeclStmt declStmt) {
        env.currentScope().add(declStmt.getDecl().getElement());
        return super.visitDeclStmt(declStmt);
    }

    @Override
    public Void visitBlock(Block block) {
        try (var ignored = env.enterScope()) {
            return super.visitBlock(block);
        }
    }

    @Override
    public Void visitFile(File file) {
        try (var scope = env.enterScope()) {
            for (Import imp : file.getImports()) {
                imp.getElements().forEach(scope::add);
            }
            enterPackage(file.getPackage().getRoot(), scope);
            if (!file.getPackage().isRoot())
                enterPackage(file.getPackage(), scope);
            return super.visitFile(file);
        }
    }

    private void enterPackage(Package pkg, Scope scope) {
        pkg.getClasses().forEach(scope::add);
        pkg.getFunctions().forEach(f -> scope.add(f.getInstance()));
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var scope = env.enterScope()) {
            var clazz = classDecl.getElement();
            clazz.getTypeParameters().forEach(scope::add);
            scope.addAll(clazz.getType().getTable());
            return super.visitClassDecl(classDecl);
        }
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        try (var scope = env.enterScope()) {
            var method = methodDecl.getElement();
            method.getTypeParameters().forEach(scope::add);
            method.getParameters().forEach(scope::add);
            return super.visitMethodDecl(methodDecl);
        }
    }

    @Override
    public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
        super.visitLocalVarDecl(localVarDecl);
        if (localVarDecl.type() == null) {
            var v = localVarDecl.getElement();
            var initial = localVarDecl.initial();
            if (initial == null)
                throw new AnalysisException("Auto variable must have initial value");
            v.setType(initial.getType());
        }
        return null;
    }


    @Override
    public Void visitExpr(Expr expr) {
        attrExpr(expr).resolve();
        return null;
    }

    private Resolver attrExpr(Expr expr) {
        return expr.accept(new ExprAttr());
    }

    private class ExprAttr extends AbstractNodeVisitor<Resolver> {

        @Override
        public Resolver visitNode(Node node) {
            throw new RuntimeException("Unhandled expression class: " + node.getClass().getName());
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
            condExpr.setType(Types.getLUB(List.of(
                    condExpr.getTruePart().getType(),
                    condExpr.getFalsePart().getType()
            )));
            return new NullResolver();
        }

        @Override
        public Resolver visitIsExpr(IsExpr isExpr) {
            isExpr.getExpr().accept(this).resolve();
            isExpr.setType(PrimitiveType.BOOLEAN);
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
            binaryExpr.x().accept(this).resolve();
            binaryExpr.y().accept(this).resolve();
            var xType = binaryExpr.x().getType();
            var yType = binaryExpr.y().getType();
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
                case Boolean ignored -> PrimitiveType.BOOLEAN;
                case Character ignored -> PrimitiveType.CHAR;
                case String ignored -> PrimitiveType.STRING;
                case null -> PrimitiveType.NULL;
                default -> throw new IllegalStateException("Unrecognized literal: " + literal);
            };
            literal.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitNewExpr(NewExpr newExpr) {
            var type = (ClassType) newExpr.type().getType();
            if (newExpr.owner() != null)
                newExpr.owner().accept(this).resolve();
            var argTypes = newExpr.arguments().map(e -> {
                e.accept(this).resolve();
                return e.getType();
            });
            var table  = type.getTable();
            var resolved = (MethodInst) table.lookupFirst(SymName.init(), e ->
                    e instanceof MethodInst m && m.isConstructor() && resolveFunction(m, List.nil(), argTypes) != null
            );
            if (resolved == null)
                throw new AnalysisException("Can not resolve constructor invocation " + newExpr.getText());
            newExpr.setElement(resolved);
            newExpr.setType(type);
            return new NullResolver();
        }

        @Override
        public Resolver visitAssignExpr(AssignExpr assignExpr) {
            assignExpr.lhs().accept(this).resolve();
            assignExpr.rhs().accept(this).resolve();
            assignExpr.setType(assignExpr.lhs().getType());
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
                    e0 instanceof Clazz || e0 instanceof Variable ? e0 : null
            );
            var table = switch (e) {
                case Clazz clazz -> clazz.getType().getTable();
                case Package pkg -> pkg.getTable();
                case null, default -> selectorExpr.x().getType().getTable();
            };
            var candidates = table.lookupAll(selectorExpr.sel().value());
            return new PendingResolver(selectorExpr, candidates);
        }

        @Override
        public Resolver visitCallExpr(CallExpr callExpr) {
            var funcResolver = callExpr.getFunc().accept(this);
            var typeArgs = callExpr.getTypeArguments().map(TypeNode::getType);
            var argTypes = callExpr.getArguments().map(arg -> {
                arg.accept(this).resolve();
                return arg.getType();
            });
            var func = (ValueElement) funcResolver.resolve(
                    e -> e instanceof ValueElement v ? resolveFunction(v, typeArgs, argTypes) : null
            );
            var funcType = (FunctionType) func.getType();
            callExpr.setElement(func);
            callExpr.setType(funcType.getReturnType());
            return new NullResolver();
        }

        @Override
        public Resolver visitLambdaExpr(LambdaExpr lambdaExpr) {
            try (var scope = env.enterScope()) {
                var lambda = lambdaExpr.getElement();
                lambdaExpr.setType(env.types().getFunctionType(
                        lambda.getParameters().map(Parameter::getType),
                        lambda.getReturnType()
                ));
                lambda.getParameters().forEach(scope::add);
                lambdaExpr.forEachChild(e -> e.accept(Attr.this));
                return new ImmediateResolver(lambda);
            }
        }

        @Override
        public Resolver visitRefExpr(RefExpr refExpr) {
            var e = env.getTable().lookupAll(refExpr.getName().value());
            return new PendingResolver(refExpr, e);
        }

    }

    private abstract class Resolver {

        abstract Element resolve();

        abstract Element resolve(Function<Element, Element> mapper);

    }

    private class ImmediateResolver extends Resolver {
        private final Element resolved;

        private ImmediateResolver(Element resolved) {
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

    private class PendingResolver extends Resolver{
        private final Iterable<Element> candidates;
        private final Expr expr;

        public PendingResolver(Expr expr, Iterable<Element> candidates) {
            this.expr = expr;
            this.candidates = candidates;
        }

        public Element resolve(Function<Element, Element> mapper) {
            for (Element candidate : candidates) {
                Element resolved;
                if ((resolved = mapper.apply(candidate)) != null) {
                    onResolved(resolved);
                    return resolved;
                }
            }
            throw new AnalysisException("Fail to resolve symbol for expression: " + expr.getText());
        }

        Element resolve() {
            var resolved = Objects.requireNonNull(candidates.iterator().next());
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

    private Element resolveFunction(ValueElement element, List<Type> typeArguments, List<Type> argumentTypes) {
        if (Traces.traceAttr) {
            log.trace("Checking applicable of element {} with argument types: {}",
                    element, argumentTypes.map(Type::getText).join(", "));
        }
        if (typeArguments.isEmpty()) {
            if (element.getType() instanceof FunctionType functionType &&
                functionType.getParameterTypes().matches(argumentTypes, Type::isAssignableFrom))
                return element;
            else
                return null;
        }
        else if (element instanceof FuncInst func) {
            if (func.getFunction().getTypeParameters().size() == typeArguments.size()) {
                var m = func.getInstance(typeArguments);
                if (m.getParameterTypes().matches(argumentTypes, Type::isAssignableFrom))
                    return m;
            }
        }
        return null;
    }

}
