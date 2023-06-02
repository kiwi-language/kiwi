package tech.metavm.transpile;

import javax.annotation.Nullable;

import static tech.metavm.transpile.JavaParser.*;

public interface StatementVisitor {

    void visitReturn(@Nullable ExpressionContext expression);

    void visitYield(ExpressionContext expression);

}
