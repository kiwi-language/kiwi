package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentAttr extends StructuralNodeVisitor {

    public static final Logger logger = LoggerFactory.getLogger(IdentAttr.class);

    private final Env env;
    private final Log log;
    private MatchBindings matchBindings = MatchBindings.empty;

    public IdentAttr(Project project, Log log) {
        env = new Env(project, log);
        this.log = log;
    }

    @Override
    public Void visitIdent(Ident ident) {
        ident.setCandidates(env.lookupAll(ident.getName()));
        return super.visitIdent(ident);
    }

    @Override
    public Void visitForeachStmt(ForeachStmt foreachStmt) {
        try (var ignored = env.enterScope(foreachStmt)) {
            add(foreachStmt.getVar().getElement());
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
        if (declStmt.getDecl().getElement() instanceof Variable v) {
            super.visitDeclStmt(declStmt);
            add(v);
            return null;
        } else {
            add(declStmt.getDecl().getElement());
            return super.visitDeclStmt(declStmt);
        }
    }

    @Override
    public Void visitIsExpr(IsExpr isExpr) {
        super.visitIsExpr(isExpr);
        if(isExpr.getVar() != null)
            matchBindings = new MatchBindings(List.of(isExpr.getVar().getElement()), List.of(), log);
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
            var rootPkg =  file.getPackage().getRoot();
            rootPkg.getPackages().forEach(scope::add);
            enterPackage(rootPkg, scope);
            enterPackage(rootPkg.subPackage("java").subPackage("lang"), scope);
            enterPackage(rootPkg.subPackage("org").subPackage("metavm").subPackage("api"), scope);
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
        try (var ignored = env.enterScope(catcher)) {
            add(catcher.getParam().getElement());
            return super.visitCatcher(catcher);
        }
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        var clazz = classDecl.getElement();

        // Step 1: Process static members
        try (var scope = env.enterScope(classDecl)) {
            clazz.getTypeParams().forEach(scope::add);
            clazz.getTable().forEach(e -> {
                if (e instanceof Method m && m.isStatic() || e instanceof Clazz c && c.isStatic()
                        || e instanceof Field f && f.isStatic() && f.getDeclClass() != clazz)
                    add(e);
            });
            classDecl.enumConstants().forEach(ecd -> {
                ecd.accept(this);
                add(ecd.getElement());
            });
            classDecl.getMembers().forEach(node -> {
                if (node instanceof FieldDecl fd && fd.getElement().isStatic()) {
                    fd.accept(this);
                    add(fd.getElement());
                }
            });
            classDecl.getMembers().forEach(node -> {
                if (node instanceof MethodDecl md && md.getElement().isStatic()
                        || node instanceof ClassDecl cd && cd.getElement().isStatic())
                    node.accept(this);
            });
        }

        // Step 2: Process instance fields and init blocks
        try (var scope = env.enterScope(classDecl)) {
            clazz.getTypeParams().forEach(scope::add);
            for (var paramDecl : classDecl.getParams()) {
                add(paramDecl.getElement());
            }
            classDecl.getImplements().forEach(ext -> ext.accept(this));
            clazz.getTable().forEach(e -> {
                if (e instanceof Field field && !field.isStatic() && field.getDeclClass() == clazz)
                    return;
                add(e);
            });
            classDecl.getMembers().forEach(node -> {
                if (node instanceof FieldDecl fieldDecl && !fieldDecl.getElement().isStatic()) {
                    node.accept(this);
                    var e = env.lookupFirst(fieldDecl.getName());
                    if (e instanceof Param p && p.getExecutable() instanceof Method m && m.getDeclClass() == clazz)
                        return; // This field is shadowed by a class parameter
                    add(fieldDecl.getElement());
                } else if (node instanceof Init)
                    node.accept(this);
            });
        }

        // Step 3: Process instance methods and inner classes
        try (var scope = env.enterScope(classDecl)) {
            clazz.getTypeParams().forEach(scope::add);
            scope.addAll(clazz.getTable());
            classDecl.getMembers().forEach(node -> {
                if (node instanceof MethodDecl md && !md.getElement().isStatic()
                        || node instanceof ClassDecl cd && !cd.getElement().isStatic())
                    node.accept(this);
            });
        }

        return null;
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

    private void add(Element e) {
        if (e instanceof Variable v && v.getNode() != null && v.getName() != NameTable.instance.children) {
            var e1 = env.lookupEntry(v.getName());
            while (!e1.isNil()) {
                if (e1.value() instanceof Variable v1 && v1 != v) {
                    if (v1.getScope() == v.getScope())
                        log.error(v.getNode(), Errors.variableAlreadyDefined(v.getName().toString()));
                    break;
                }
                e1 = e1.next();
            }
        } else if (e instanceof Method m && m.getNode() != null) {
            var e1 = env.lookupEntry(m.getName());
            while (!e1.isNil()) {
                if (e1.value() instanceof Method m1 && m1 != m) {
                    if (m1.getDeclClass() != m.getDeclClass())
                        break;
                    if (m1.getParamTypes().equals(m.getParamTypes())) {
                        log.error(m.getNode(), Errors.functionAlreadyDefined(m.getSignature()));
                        break;
                    }
                }
                e1 = e1.next();
            }
        }
        env.currentScope().add(e);
    }

    private static class MatchBindings {

        private final List<LocalVar> trueBindings;
        private final List<LocalVar> falseBindings;
        private final Log log;

        private MatchBindings(List<LocalVar> trueBindings, List<LocalVar> falseBindings, Log log) {
            this.trueBindings = trueBindings;
            this.falseBindings = falseBindings;
            this.log = log;
        }

        MatchBindings not() {
            return new MatchBindings(falseBindings, trueBindings, log);
        }

        void enterTrue(Scope scope) {
            trueBindings.forEach(scope::add);
        }


        void enterFalse(Scope scope) {
            falseBindings.forEach(scope::add);
        }

        MatchBindings or(MatchBindings that) {
            return new MatchBindings(intersect(trueBindings, that.trueBindings), union(falseBindings, that.falseBindings), log);
        }

        MatchBindings and(MatchBindings that) {
            return new MatchBindings(union(trueBindings, that.trueBindings), intersect(falseBindings, that.falseBindings), log);
        }

        /** @noinspection unused*/
        private List<LocalVar> intersect(List<LocalVar> lhs, List<LocalVar> rhs) {
            return List.of();
        }

        private List<LocalVar> union(List<LocalVar> lhs, List<LocalVar> rhs) {
            var list = List.builder(lhs);
            out: for (LocalVar vr : rhs) {
                for (LocalVar vl : lhs) {
                    if (vl.getName() == vr.getName()) {
                        log.error(vr.getNode(), Errors.duplicateBindingName);
                        continue out;
                    }
                }
                list.append(vr);
            }
            return list.build();
        }

        public static final MatchBindings empty = new MatchBindings(List.nil(), List.nil(), new DummyLog()) {
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
