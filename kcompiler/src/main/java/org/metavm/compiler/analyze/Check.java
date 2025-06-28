package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.LocalVar;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.Types;

import java.util.EnumSet;
import java.util.Set;

import static org.metavm.compiler.syntax.ModifierTag.*;

public class Check extends StructuralNodeVisitor {

    private final Log log;
    private final Env env;

    public Check(Project project, Log log) {
        this.log = log;
        env = new Env(project, log);
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        if (classDecl.getElement().isInner())
            checkMods(classDecl, INNER_CLASS_ALLOWED_MODS);
        else
            checkMods(classDecl, CLASS_ALLOWED_MODS);
        return super.visitClassDecl(classDecl);
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        checkMods(methodDecl, METHOD_ALLOWED_MODS);
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
        checkMods(fieldDecl, FIELD_ALLOWED_MODS);
        var field = fieldDecl.getElement();
        if (field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(fieldDecl, Errors.summaryFieldMustBeString);
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        checkMods(classParamDecl, CLASS_PARAM_ALLOWED_MODS);
        var field = classParamDecl.getField();
        if (field != null) {
            if (field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
                log.error(classParamDecl, Errors.summaryFieldMustBeString);
        }
        return super.visitClassParamDecl(classParamDecl);
    }


    private static final Set<ModifierTag> CLASS_PARAM_ALLOWED_MODS = EnumSet.of(PUB, PROT, PRIV);
    private static final Set<ModifierTag> FIELD_ALLOWED_MODS = EnumSet.of(PUB, PROT, PRIV, STATIC, DELETED);
    private static final Set<ModifierTag> CLASS_ALLOWED_MODS = EnumSet.of(
            PUB, ABSTRACT, VALUE, TEMP
    );

    private static final Set<ModifierTag> INNER_CLASS_ALLOWED_MODS = EnumSet.of(
            PUB, PROT, PRIV, ABSTRACT, VALUE, TEMP, STATIC
    );

    private static final Set<ModifierTag> METHOD_ALLOWED_MODS = EnumSet.of(
            PUB, PROT, PRIV, ABSTRACT, STATIC
    );

    private void checkMods(ModifiedDecl<?> node, Set<ModifierTag> allowedMods) {
        for (Modifier mod : node.getMods()) {
            if (!allowedMods.contains(mod.tag()))
                log.error(mod, Errors.modifierNotAllowedHere(mod.tag()));
        }
    }

}
