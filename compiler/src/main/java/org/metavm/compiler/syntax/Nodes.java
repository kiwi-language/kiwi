package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;

import javax.annotation.Nullable;

public class Nodes {
    public static boolean isSelfInitCall(Stmt stmt) {
        return stmt instanceof ExprStmt exprStmt
                && exprStmt.expr() instanceof Call call
                && call.getFunc() instanceof Ident ident
                && ident.getName() == Name.init();
    }

    public static boolean isSuperInitCall(Stmt stmt) {
        return stmt instanceof ExprStmt exprStmt
                && exprStmt.expr() instanceof Call call
                && call.getFunc() instanceof SelectorExpr selectorExpr
                && selectorExpr.sel() == Name.init()
                && selectorExpr.x() instanceof Ident ident
                && ident.getName() == Name.super_();
    }

    public static @Nullable Name getRefName(Expr expr) {
        return switch (expr) {
            case Ident ident -> ident.getName();
            case SelectorExpr selectorExpr -> selectorExpr.sel();
            case TypeApply typeApply -> getRefName(typeApply.getExpr());
            default -> null;
        };
    }
}
