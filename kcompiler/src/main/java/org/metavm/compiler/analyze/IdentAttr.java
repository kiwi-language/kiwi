package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.List;

@Slf4j
public class IdentAttr extends StructuralNodeVisitor {

    private final Env env = new Env();

    private MatchBindings matchBindings = MatchBindings.empty;

    @Override
    public Void visitIdent(Ident ident) {
        ident.setCandidates(env.lookupAll(ident.getName()));
        return super.visitIdent(ident);
    }

    @Override
    public Void visitForeachStmt(ForeachStmt foreachStmt) {
        try (var scope = env.enterScope(foreachStmt)) {
            scope.add(foreachStmt.getVar().getElement());
            return super.visitForeachStmt(foreachStmt);
        }
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        try (var scope = env.enterScope(whileStmt)) {
            whileStmt.cond().accept(this);
            matchBindings.enterTrue(scope);
            whileStmt.body().accept(this);
            return null;
        }
    }

    @Override
    public Void visitDeclStmt(DeclStmt declStmt) {
        env.currentScope().add(declStmt.getDecl().getElement());
        return super.visitDeclStmt(declStmt);
    }

    @Override
    public Void visitIsExpr(IsExpr isExpr) {
        super.visitIsExpr(isExpr);
        if(isExpr.getVar() != null)
            matchBindings = new MatchBindings(List.of(isExpr.getVar().getElement()), List.of());
        return null;
    }

    @Override
    public Void visitExpr(Expr expr) {
        matchBindings = MatchBindings.empty;
        return super.visitExpr(expr);
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        ifStmt.cond().accept(this);
        var mb = matchBindings;
        try (var scope = env.enterScope(ifStmt.body())) {
            mb.enterTrue(scope);
            ifStmt.body().accept(this);
        }
        if (ifStmt.else_() != null) {
            try(var scope = env.enterScope(ifStmt.else_())) {
                mb.enterFalse(scope);
                ifStmt.else_().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitDoWhileStmt(DoWhileStmt doWhileStmt) {
        try (var ignored = env.enterScope(doWhileStmt)) {
            return super.visitDoWhileStmt(doWhileStmt);
        }
    }

    @Override
    public Void visitPrefixExpr(PrefixExpr prefixExpr) {
        super.visitPrefixExpr(prefixExpr);
        if (prefixExpr.op() == PrefixOp.NOT)
            matchBindings = matchBindings.not();
        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr binaryExpr) {
        var op = binaryExpr.op();
        binaryExpr.lhs().accept(this);
        var lmb = matchBindings;
        if (op == BinOp.AND) {
            try (var scope = env.enterScope(binaryExpr.rhs())) {
                lmb.enterTrue(scope);
                binaryExpr.rhs().accept(this);
            }
            matchBindings = lmb.and(matchBindings);
        }
        else if (op == BinOp.OR) {
            try (var scope = env.enterScope(binaryExpr.rhs())) {
                lmb.enterFalse(scope);
                binaryExpr.rhs().accept(this);
            }
            matchBindings = lmb.or(matchBindings);
        }
        else
            binaryExpr.rhs().accept(this);
        return null;
    }

    @Override
    public Void visitBlock(Block block) {
        try (var ignored = env.enterScope(block)) {
            return super.visitBlock(block);
        }
    }

    @Override
    public Void visitFile(File file) {
        try (var scope = env.enterScope(file)) {
            for (Import imp : file.getImports()) {
                imp.getElements().forEach(scope::add);
            }
            file.getPackage().getRoot().getPackages().forEach(scope::add);
            enterPackage(file.getPackage().getRoot(), scope);
            enterPackage(file.getPackage().getRoot().subPackage("java").subPackage("lang"), scope);
            if (!file.getPackage().isRoot())
                enterPackage(file.getPackage(), scope);
            return super.visitFile(file);
        }
    }

    private void enterPackage(Package pkg, Scope scope) {
        pkg.getClasses().forEach(scope::add);
        pkg.getFunctions().forEach(scope::add);
    }

    @Override
    public Void visitLabeledStmt(LabeledStmt labeledStmt) {
        try (var ignored = env.enterScope(labeledStmt)) {
            return super.visitLabeledStmt(labeledStmt);
        }
    }

    @Override
    public Void visitCatcher(Catcher catcher) {
        try (var scope = env.enterScope(catcher)) {
            scope.add(catcher.getParam().getElement());
            return super.visitCatcher(catcher);
        }
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var scope = env.enterScope(classDecl)) {
            var clazz = classDecl.getElement();
            clazz.getTypeParams().forEach(scope::add);
            scope.addAll(clazz.getTable());
            return super.visitClassDecl(classDecl);
        }
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        try (var scope = env.enterScope(methodDecl)) {
            var method = methodDecl.getElement();
            method.getTypeParams().forEach(scope::add);
            method.getParams().forEach(scope::add);
            return super.visitMethodDecl(methodDecl);
        }
    }


    @Override
    public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
        try (var scope = env.enterScope(lambdaExpr)) {
            var lambda = lambdaExpr.getElement();
            lambdaExpr.setType(env.types().getFuncType(
                    lambda.getParams().map(Param::getType),
                    lambda.getRetType()
            ));
            lambda.getParams().forEach(scope::add);
            return super.visitLambdaExpr(lambdaExpr);
        }
    }

    @Override
    public Void visitBreakStmt(BreakStmt breakStmt) {
        breakStmt.setTarget(env.findJumpTarget(breakStmt.getLabel()));
        return null;
    }

    @Override
    public Void visitContinueStmt(ContinueStmt continueStmt) {
        continueStmt.setTarget(env.findJumpTarget(continueStmt.getLabel()));
        return null;
    }

    private static class MatchBindings {

            private final List<LocalVar> trueBindings;
            private final List<LocalVar> falseBindings;

            private MatchBindings(List<LocalVar> trueBindings, List<LocalVar> falseBindings) {
                this.trueBindings = trueBindings;
                this.falseBindings = falseBindings;
            }

            MatchBindings not() {
                return new MatchBindings(falseBindings, trueBindings);
            }

            void enterTrue(Scope scope) {
                trueBindings.forEach(scope::add);
            }


            void enterFalse(Scope scope) {
                falseBindings.forEach(scope::add);
            }

            MatchBindings or(MatchBindings that) {
                return new MatchBindings(intersect(trueBindings, that.trueBindings), union(falseBindings, that.falseBindings));
            }

            MatchBindings and(MatchBindings that) {
                return new MatchBindings(union(trueBindings, that.trueBindings), intersect(falseBindings, that.falseBindings));
            }

            /** @noinspection unused*/
            private static List<LocalVar> intersect(List<LocalVar> lhs, List<LocalVar> rhs) {
                return List.of();
            }

            private static List<LocalVar> union(List<LocalVar> lhs, List<LocalVar> rhs) {
                var list = List.builder(lhs);
                for (LocalVar vr : rhs) {
                    for (LocalVar vl : lhs) {
                        if (vl.getName() == vr.getName())
                            throw new AnalysisException("Duplicate binding name: " + vr.getName());
                    }
                    list.append(vr);
                }
                return list.build();
            }

            public static final MatchBindings empty = new MatchBindings(List.nil(), List.nil()) {
                @Override
                MatchBindings not() {
                    return this;
                }

                @Override
                void enterTrue(Scope scope) {

                }

                @Override
                void enterFalse(Scope scope) {

                }
            };
        }

}
