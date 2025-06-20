package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Error;
import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class Attr extends StructuralNodeVisitor {

    public static final Logger logger = LoggerFactory.getLogger(Attr.class);

    private final Project project;
    private final Log log;

    public Attr(Project project, Log log) {
        this.project = project;
        this.log = log;
    }

    @Override
    public Void visitExtend(Extend extend) {
        if (extend.getExpr() instanceof Call call)
            attrExpr(call, PrimitiveType.ANY).resolve();
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
            log.error(expr, Errors.forEachNotApplicableToType(expr.getType()));
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
            if (initial == null) {
                log.error(localVarDecl, Errors.variableMustTypedOrInitialized);
                v.setType(ErrorType.instance);
            } else
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
            if (initial == null) {
                log.error(fieldDecl, Errors.variableMustTypedOrInitialized);
                field.setType(ErrorType.instance);
            }
            else
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
        var init = resolveInit(clazz, argTypes);
        if (init != null)
            enumConstDecl.setInit(init);
        else
            log.error(enumConstDecl, Errors.cantFindConstructor(clazz, argTypes));
        if (enumConstDecl.getDecl() != null) {
            enumConstDecl.getDecl().accept(this);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(RetStmt retStmt) {
        if (retStmt.result() != null)
            attrExpr(retStmt.result(), retStmt.getType()).resolve();
        return null;
    }

    @Override
    public Void visitDelStmt(DelStmt delStmt) {
        attrExpr(delStmt.getExpr(), PrimitiveType.ANY).resolve();
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
            logger.trace("Attributing expression {}", expr.getText());
        if (expr.getStatus() == ExprStatus.RESOLVED)
            return new ImmediateResolver(expr.getElement());
        if (expr.getStatus() == ExprStatus.RESOLVING) {
            log.error(expr, Errors.typeCheckingCircularRef);
            expr.setElement(ErrorElement.instance);
            return new ImmediateResolver(ErrorElement.instance);
        }
        if (expr instanceof LambdaExpr lambdaExpr) {
            if (type instanceof FuncType funcType)
                setLambdaType(lambdaExpr, funcType);
        }
        var resolver = expr.accept(new ExprAttr());
        expr.setStatus(ExprStatus.RESOLVED);
        return resolver;
    }

    private void setLambdaType(LambdaExpr lambdaExpr, FuncType type) {
        lambdaExpr.setTargetType(type);
        var retType = type.getRetType();
        if (retType.isVoid())
            return;
        lambdaExpr.accept(new StructuralNodeVisitor() {
            @Override
            public Void visitDecl(Decl<?> decl) {
                return null;
            }

            @Override
            public Void visitLambdaExpr(LambdaExpr l) {
                if (l == lambdaExpr)
                    return super.visitLambdaExpr(l);
                else
                    return null;
            }

            @Override
            public Void visitReturnStmt(RetStmt retStmt) {
                retStmt.setType(retType);
                return null;
            }
        });

    }

    private void ensureElementTyped(Element element) {
        if (element instanceof Variable variable && variable.getType() instanceof DeferredType) {
            var templateVar = variable instanceof FieldInst fieldInst ? fieldInst.field() : variable;
            var varDecl = (VariableDecl<?>) templateVar.getNode();
            attrExpr(Objects.requireNonNull(varDecl.getInitial()), Types.instance.getNullableAny()).resolve();
            templateVar.setType(Objects.requireNonNull(varDecl.getInitial()).getType());
        }
    }

    private class ExprAttr extends AbstractNodeVisitor<Resolver> {

        @Override
        public Resolver visitNode(Node node) {
            throw new RuntimeException("Unhandled expression class: " + node.getClass().getName());
        }

        @Override
        public Resolver visitErrorExpr(ErrorExpr errorExpr) {
            return new NullResolver();
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
                Element resolve(Function<Element, Element> mapper, @Nullable Comparator<Element> comparator, Function<Expr, Error> errorFunc) {
                    var resolved = r.resolve(e -> {
                        if (e instanceof GenericDecl genDecl
                                && genDecl.getTypeParams().size() == typeApply.getArgs().size()) {
                            var inst = genDecl.getInst(typeApply.getArgs().map(TypeNode::getType));
                            return mapper.apply(inst);
                        }
                        return null;
                    }, comparator, errorFunc);
                    typeApply.setElement(resolved);
                    return resolved;
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
                    },
                    this::compareCallCandidate,
                    expr -> Errors.cantResolveFunction(argTypes));
            if (call.getFunc().getType() instanceof FuncType funcType) {
                completeArgumentResolution(call.getArguments(), funcType.getParamTypes());
                if (func != null)
                    call.setElement(func);
            }
            return new NullResolver();
        }

        private int compareCallCandidate(Element e1, Element e2) {
            var funcType1 = (FuncType) ((ValueElement) e1).getType();
            var funcType2 = (FuncType) ((ValueElement) e2).getType();
            if (e1 instanceof MethodRef m1 && e2 instanceof MethodRef m2) {
                if (isOverride(m1, m2))
                    return -1;
                if (isOverride(m2, m1))
                    return 1;
            }
            if (isApplicable(funcType2.getParamTypes(), funcType1.getParamTypes()))
                return -1;
            if (isApplicable(funcType1.getParamTypes(), funcType2.getParamTypes()))
                return 1;
            return 0;
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

        Element resolve(Function<Element, Element> mapper) {
            return resolve(mapper, null, Errors::cantResolve);
        }

        abstract Element resolve(Function<Element, Element> mapper, @Nullable Comparator<Element> comparator, Function<Expr, Error> errorFunc);

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
        Element resolve(Function<Element, Element> mapper, @Nullable Comparator<Element> comparator, Function<Expr, Error> errorFunc) {
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

        public Element resolve(Function<Element, Element> mapper, @Nullable Comparator<Element> comparator, Function<Expr, Error> errorFunc) {
            var matches = List.<Element>nil();
            out: for (Element candidate : candidates) {
                ensureElementTyped(candidate);
                Element resolved;
                if ((resolved = mapper.apply(candidate)) != null) {
                    if (comparator == null) {
                        onResolved(resolved);
                        return resolved;
                    }
                    var newMatches = List.<Element>nil();
                    for (Element match : matches) {
                        var r = comparator.compare(resolved, match);
                        if (r > 0)
                            continue out;
                        if (r == 0)
                            newMatches = newMatches.prepend(match);
                    }
                    matches = newMatches.prepend(resolved);
                }
            }
            if (matches.nonEmpty() && matches.tail().isEmpty()) {
                onResolved(matches.head());
                return matches.head();
            }
            if (matches.isEmpty())
                log.error(expr, errorFunc.apply(expr));
            else
                log.error(expr, Errors.ambiguousReference(matches.join(", ")));
            onResolved(ErrorElement.instance);
            return ErrorElement.instance;
        }

        Element resolve() {
            var it = candidates.iterator();
            if (!it.hasNext()) {
                onResolved(ErrorElement.instance);
                log.error(expr, Errors.cantResolve(expr));
                return ErrorElement.instance;
            }
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
        Element resolve(Function<Element, Element> mapper, @Nullable Comparator<Element> comparator, Function<Expr, Error> errorFunc) {
            return null;
        }

    }

    private Element resolveFunc(ValueElement element, List<Type> argumentTypes) {
        if (Traces.traceAttr) {
            logger.trace("Checking applicable of element {}, type: {}, with argument types: {}",
                    element.getName(),element.getType().getTypeText(), argumentTypes.map(Type::getTypeText).join(", "));
        }
        if (element.getType() instanceof FuncType funcType && isApplicable(funcType.getParamTypes(), argumentTypes))
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
            logger.trace("Resolving constructor. class: {}, arg types: [{}]",
                    type.getTypeText(),
                    argumentTypes.map(Type::getTypeText).join(", ")
            );
        }
        for (var init : inits) {
            var m = (MethodRef) init;
            if (Traces.traceAttr) {
                logger.trace("Trying init: {}", init.getText());
            }
            if (m.getParamTypes().matches(argumentTypes, this::isApplicable)) {
                return m;
            }
        }
        return null;
    }

    private boolean isApplicable(List<Type> types1, List<Type> types2) {
        return types1.matches(types2, this::isApplicable);
    }

    private boolean isApplicable(Type parameterType, Type argumentType) {
        if (parameterType.isAssignableFrom(argumentType))
            return true;
        return widensTo(argumentType, parameterType);
    }

    private boolean isOverride(MethodRef override, MethodRef overridden) {
        return override.getName() == overridden.getName()
                && overridden.getDeclType() != override.getDeclType()
                && overridden.getDeclType().isAssignableFrom(override.getDeclType())
                && override.getParamTypes().equals(overridden.getParamTypes());
    }

    private boolean widensTo(Type t1, Type t2) {
        if (t1 instanceof PrimitiveType pt1) {
            if (t2 instanceof PrimitiveType pt2)
                return pt1.widensTo(pt2);
            else if (t2 instanceof UnionType ut)
                return ut.alternatives().anyMatch(alt -> widensTo(t1, alt));
        }
        return false;
    }

}
