package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.LocalVar;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.Types;

public class Check extends StructuralNodeVisitor {

    private final Log log;
    private final Env env;

    public Check(Project project, Log log) {
        this.log = log;
        env = new Env(project, log);
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        try (var ignored = env.enterScope(methodDecl, methodDecl.getElement())) {
            return super.visitMethodDecl(methodDecl);
        }
    }

    @Override
    public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
        try (var ignored = env.enterScope(lambdaExpr, lambdaExpr.getElement())) {
            return super.visitLambdaExpr(lambdaExpr);
        }
    }

    @Override
    public Void visitAssignExpr(AssignExpr assignExpr) {
        if (assignExpr.lhs().getElement() instanceof LocalVar local) {
            if (local.getExecutable() != env.currentExecutable())
                log.error(assignExpr, Errors.cantModifyCapturedVar);
        }
        return super.visitAssignExpr(assignExpr);
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        var field = fieldDecl.getElement();
        if (field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(fieldDecl, Errors.summaryFieldMustBeString);
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        var field = classParamDecl.getField();
        if (field != null && field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(classParamDecl, Errors.summaryFieldMustBeString);
        return super.visitClassParamDecl(classParamDecl);
    }
}
