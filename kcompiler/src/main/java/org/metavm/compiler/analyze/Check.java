package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.LocalVar;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.entity.AttributeNames;

import java.util.EnumSet;
import java.util.Set;

import static org.metavm.compiler.syntax.ModifierTag.*;

public class Check extends StructuralNodeVisitor {

    private final Log log;
    private final Env env;
    private Annotation currentUserAnnotation;
    private Annotation authTokenAnnotation;
    private boolean tokenValidatorPresent;

    public Check(Project project, Log log) {
        this.log = log;
        env = new Env(project, log);
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var ignored = env.enterScope(classDecl, classDecl.getElement())) {
            if (classDecl.getElement().isInner())
                checkMods(classDecl, INNER_CLASS_ALLOWED_MODS);
            else
                checkMods(classDecl, CLASS_ALLOWED_MODS);
            var klass = classDecl.getElement();
            if (env.getProject().getTokenValidatorClass() != null &&
                    env.getProject().getTokenValidatorClass().isAssignableFrom(klass) &&
                    klass.getAttributes().anyMatch(a -> a.name().equals(AttributeNames.BEAN_NAME))) {
                tokenValidatorPresent = true;
            }
            for (Field field : klass.getFields()) {
                if (field.getType() instanceof ClassType ct && ct.getClazz() == env.getProject().getIndexClass()) {
                    if (!field.isStatic())
                        log.error(field.getNode(), Errors.nonStaticIndexField);
                    if (ct.getTypeArguments().getLast() instanceof ClassType valueType) {
                        if (valueType.getClazz() != klass)
                            log.error(field.getNode(), Errors.misplacedIndexField);
                    } else
                        log.error(field.getNode(), Errors.invalidIndexValueType);
                }
            }
            return super.visitClassDecl(classDecl);
        }
    }

    @Override
    public Void visitInit(Init init) {
        var constructor = env.currentClass().getPrimaryInit();
        try (var ignored = env.enterScope(init, constructor)) {
            return super.visitInit(init);
        }
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
        var lambda = lambdaExpr.getElement();
        try (var ignored = env.enterScope(lambdaExpr, lambda)) {
            if (lambdaExpr.body() instanceof Expr expr
                    && !lambda.getRetType().isVoid()
                    && !Types.isConvertible(expr.getType(), lambda.getRetType()))
                log.error(expr, Errors.incompatibleTypes(expr.getType().getTypeText(), lambda.getRetType().getTypeText()));
            return super.visitLambdaExpr(lambdaExpr);
        }
    }

    @Override
    public Void visitAssignExpr(AssignExpr assignExpr) {
        if (!assignExpr.lhs().isMutable())
            log.error(assignExpr.lhs(), Errors.cantAssignToImmutableValue);
        if (assignExpr.lhs().getElement() instanceof LocalVar local) {
           if (local.getExecutable() != env.currentExecutable())
                log.error(assignExpr, Errors.cantModifyCapturedVar);
        }
        if (!Types.isConvertible(assignExpr.rhs().getType(), assignExpr.lhs().getType())) {
            log.error(assignExpr, Errors.incompatibleTypes(
                    assignExpr.rhs().getType().getTypeText(),
                    assignExpr.lhs().getType().getTypeText()
            ));
        }
        return super.visitAssignExpr(assignExpr);
    }

    @Override
    public Void visitReturnStmt(RetStmt retStmt) {
        if (retStmt.result() != null && !Types.isConvertible(retStmt.result().getType(), retStmt.getType())) {
            log.error(retStmt.result(), Errors.incompatibleTypes(
                    retStmt.result().getType().getTypeText(),
                    retStmt.getType().getTypeText()
            ));
        }
        return super.visitReturnStmt(retStmt);
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        checkMods(fieldDecl, FIELD_ALLOWED_MODS);
        var field = fieldDecl.getElement();
        if (field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(fieldDecl, Errors.summaryFieldMustBeString);
        if (!field.getType().isNullable() && fieldDecl.getInitial() == null)
            log.error(fieldDecl, Errors.fieldNotInitialized);
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

    @Override
    public Void visitAnnotation(Annotation annotation) {
        if (annotation.getName() == NameTable.instance.CurrentUser)
            currentUserAnnotation = annotation;
        else if (annotation.getName() == NameTable.instance.AuthToken)
            authTokenAnnotation = annotation;
        return super.visitAnnotation(annotation);
    }

    @Override
    public Void visitCastExpr(CastExpr castExpr) {
        var sourceType = castExpr.expr().getType();
        var targetType = castExpr.type().getType();
        if (!Types.isConvertible(targetType, sourceType) && !Types.isConvertible(sourceType, targetType)) {
            log.error(castExpr, Errors.illegalCast(
                    castExpr.expr().getType().getTypeText(),
                    castExpr.type().getType().getTypeText()
            ));
        }
        return super.visitCastExpr(castExpr);
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr binaryExpr) {
        var op = binaryExpr.op();
        var lhsType = binaryExpr.lhs().getType();
        var rhsType = binaryExpr.rhs().getType();
        if (!op.check(lhsType, rhsType)) {
            log.error(binaryExpr, Errors.operatorCantBeApplied(
                    op.op(),
                    lhsType.getTypeText(),
                    rhsType.getTypeText()
            ));
        }
        return super.visitBinaryExpr(binaryExpr);
    }

    @Override
    public Void visitPrefixExpr(PrefixExpr prefixExpr) {
        if (!prefixExpr.op().check(prefixExpr.x().getType())) {
            log.error(prefixExpr.x(), Errors.operatorCantBeApplied(
                    prefixExpr.op().op(),
                    prefixExpr.x().getType().getTypeText()
            ));
        }
        return super.visitPrefixExpr(prefixExpr);
    }

    @Override
    public Void visitPostfixExpr(PostfixExpr postfixExpr) {
        if (!postfixExpr.op().check(postfixExpr.x().getType())) {
            log.error(postfixExpr.x(), Errors.operatorCantBeApplied(
                    postfixExpr.op().op(),
                    postfixExpr.x().getType().getTypeText()
            ));
        }
        return super.visitPostfixExpr(postfixExpr);
    }

    @Override
    public Void visitClassTypeNode(ClassTypeNode classTypeNode) {
        return null;
    }

    @Override
    public Void visitExpr(Expr expr) {
        if (expr.getElement() instanceof Type)
            log.error(expr, Errors.illegalUseOfType);
        return super.visitExpr(expr);
    }

    @Override
    public Void visitSelectorExpr(SelectorExpr selectorExpr) {
        if (selectorExpr.getElement() instanceof Type)
            log.error(selectorExpr, Errors.illegalUseOfType);
        return null;
    }

    private static final Set<ModifierTag> CLASS_PARAM_ALLOWED_MODS = EnumSet.of(PUB, PROT, PRIV, INTERNAL);
    private static final Set<ModifierTag> FIELD_ALLOWED_MODS = EnumSet.of(PUB, PROT, PRIV, INTERNAL, STATIC, DELETED);
    private static final Set<ModifierTag> CLASS_ALLOWED_MODS = EnumSet.of(
            PUB, INTERNAL, ABSTRACT, VALUE, TEMP
    );

    private static final Set<ModifierTag> INNER_CLASS_ALLOWED_MODS = EnumSet.of(
            PUB, PROT, PRIV, INTERNAL, ABSTRACT, VALUE, TEMP, STATIC
    );

    private static final Set<ModifierTag> METHOD_ALLOWED_MODS = EnumSet.of(
            PUB, PROT, PRIV, INTERNAL, ABSTRACT, STATIC
    );

    private void checkMods(ModifiedDecl<?> node, Set<ModifierTag> allowedMods) {
        for (Modifier mod : node.getMods()) {
            if (!allowedMods.contains(mod.tag()))
                log.error(mod, Errors.modifierNotAllowedHere(mod.tag()));
        }
    }

    public void finalCheck() {
        if (!tokenValidatorPresent) {
            if (currentUserAnnotation != null)
                log.error(currentUserAnnotation, Errors.tokenValidatorRequired("CurrentUser"));
            else if (authTokenAnnotation != null)
                log.error(authTokenAnnotation, Errors.tokenValidatorRequired("AuthToken"));
        }
    }

}
