package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.Utils;

@Slf4j
public class TypeResolver extends StructuralNodeVisitor {

    private final Env env;
    private Package pkg;

    public TypeResolver(Project project) {
         env = new Env(project);
    }

    @Override
    public Void visitDeclStmt(DeclStmt declStmt) {
        var e = declStmt.getDecl().getElement();
        if (e instanceof Clazz clazz)
            env.currentScope().add(clazz);
        return super.visitDeclStmt(declStmt);
    }

    @Override
    public Void visitBlock(Block block) {
        try (var ignored = env.enterScope(block)) {
            return super.visitBlock(block);
        }
    }

    @Override
    public Void visitNewArrayExpr(NewArrayExpr newArrayExpr) {
        super.visitNewArrayExpr(newArrayExpr);
        newArrayExpr.setType(Types.instance.getArrayType(newArrayExpr.elementType().getType()));
        return null;
    }

    @Override
    public Void visitFile(File file) {
        try (var scope = env.enterScope(file)) {
            var prevPkg = pkg;
            pkg = file.getPackage();
            for (Import imp : file.getImports()) {
                imp.getElements().forEach(scope::add);
            }
            file.getPackage().getRoot().getPackages().forEach(scope::add);
            enterPackage(pkg, scope);
            enterPackage(pkg.getRoot().subPackage("java").subPackage("lang"), scope);
            super.visitFile(file);
            pkg = prevPkg;
            return null;
        }
    }

    private void enterPackage(Package pkg, Scope scope) {
        pkg.getClasses().forEach(scope::add);
    }

    @Override
    public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
        super.visitLocalVarDecl(localVarDecl);
        var v = localVarDecl.getElement();
        var t = localVarDecl.getType();
        if (t != null)
            v.setType(t.getType());
        return null;
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        super.visitFieldDecl(fieldDecl);
        var field = fieldDecl.getElement();
        if (fieldDecl.getType() != null)
            field.setType(fieldDecl.getType().getType());
        return null;
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        super.visitClassParamDecl(classParamDecl);
        var param = classParamDecl.getElement();
        var field = classParamDecl.getField();
        if (classParamDecl.getType() != null) {
            var type = classParamDecl.getType().getType();
            param.setType(type);
            if (field != null)
                field.setType(type);
        } else {
            param.setType(PrimitiveType.ANY);
            if (field != null)
                field.setType(PrimitiveType.ANY);
        }
        return null;
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        super.visitParamDecl(paramDecl);
        var param = paramDecl.getElement();
        if (paramDecl.getType() != null)
            param.setType(paramDecl.getType().getType());
        else
            param.setType(PrimitiveType.ANY);
        return null;
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        try (var scope = env.enterScope(methodDecl)) {
            var method = methodDecl.getElement();
            method.getTypeParams().forEach(scope::add);
            methodDecl.getTypeParameters().forEach(tp -> tp.accept(this));
            methodDecl.getParams().forEach(p -> p.accept(this));
            if (methodDecl.returnType() != null) {
                methodDecl.returnType().accept(this);
                method.setRetType(methodDecl.returnType().getType());
            }
            else
                method.setRetType(PrimitiveType.VOID);
            Utils.safeCall(methodDecl.body(), b -> b.accept(this));
            return null;
        }
    }

    @Override
    public Void visitReturnStmt(RetStmt retStmt) {
        retStmt.setType(env.currentExecutable().getRetType());
        return super.visitReturnStmt(retStmt);
    }

    @Override
    public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
        try (var ignored = env.enterScope(lambdaExpr)) {
            return super.visitLambdaExpr(lambdaExpr);
        }
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var scope = env.enterScope(classDecl)) {
            var clazz = classDecl.getElement();
            clazz.getTypeParams().forEach(scope::add);
            clazz.getClasses().forEach(scope::add);
            if (clazz.isEnum()) {
                var enumClass = pkg.getRoot().subPackage("java").subPackage("lang")
                        .getClass("Enum");
                clazz.setInterfaces(List.of(enumClass.getInst(List.of(clazz))));
                classDecl.enumConstants().forEach(ec -> ec.accept(this));
                for (Method method : clazz.getMethods()) {
                    if (method.getName() == Name.from("values") && method.isStatic() && method.getParams().isEmpty()) {
                        method.setRetType(Types.instance.getArrayType(clazz));
                    }
                    else if (method.getName() == Name.from("valueOf") && method.isStatic() &&
                            method.getParamTypes().equals(List.of(Types.instance.getNullableString()))) {
                        method.setRetType(Types.instance.getNullableType(clazz));
                    }
                }
            } else {
                if (classDecl.getImplements().nonEmpty()) {
                    classDecl.getImplements().forEach(t -> t.accept(this));
                    clazz.setInterfaces(classDecl.getImplements().map(t -> {
                        var it = (ClassType) t.getType().getType();
                        it.getClazz().getClasses().forEach(scope::add);
                        return it;
                    }));
                }
                classDecl.getTypeParameters().forEach(tv -> tv.accept(this));
            }
            classDecl.getParams().forEach(p -> p.accept(this));
            classDecl.getMembers().forEach(m -> m.accept(this));
            return null;
        }
    }

    @Override
    public Void visitTypeVariableDecl(TypeVariableDecl typeVariableDecl) {
        super.visitTypeVariableDecl(typeVariableDecl);
        var typeVar = typeVariableDecl.getElement();
        var bound = typeVariableDecl.getBound();
        if (bound != null)
            typeVar.setBound(bound.getType());
        return null;
    }

    @Override
    public Void visitTypeNode(TypeNode typeNode) {
        env.resolveType(typeNode);
        return super.visitTypeNode(typeNode);
    }

}
