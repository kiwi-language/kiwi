package org.metavm.compiler.analyze;

import org.metavm.compiler.element.*;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

public class TypeResolver extends StructuralNodeVisitor {

    private final Env env = new Env();
    private Package pkg;

    @Override
    public Void visitDeclStmt(DeclStmt declStmt) {
        var e = declStmt.getDecl().getElement();
        if (e instanceof Clazz clazz)
            env.currentScope().add(clazz);
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
            var prevPkg = pkg;
            pkg = file.getPackage();
            for (Import imp : file.getImports()) {
                imp.getElements().forEach(scope::add);
            }
            pkg.getClasses().forEach(scope::add);
            super.visitFile(file);
            pkg = prevPkg;
            return null;
        }
    }

    @Override
    public Void visitLocalVarDecl(LocalVarDecl localVarDecl) {
        super.visitLocalVarDecl(localVarDecl);
        var v = localVarDecl.getElement();
        var t = localVarDecl.type();
        if (t != null)
            v.setType(t.getType());
        return null;
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        super.visitFieldDecl(fieldDecl);
        var field = fieldDecl.getElement();
        field.setType(fieldDecl.type().getType());
        return null;
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        super.visitParamDecl(paramDecl);
        var param = paramDecl.getElement();
        param.setType(paramDecl.type().getType());
        return null;
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        try (var scope = env.enterScope()) {
            var method = methodDecl.getElement();
            method.getTypeParameters().forEach(scope::add);
            super.visitMethodDecl(methodDecl);
            var ret = methodDecl.returnType();
            method.setReturnType(ret != null ? ret.getType() : PrimitiveType.VOID);
            return null;
        }
    }

    @Override
    public Void visitLambdaExpr(LambdaExpr lambdaExpr) {
        try (var ignored = env.enterScope()) {
            super.visitLambdaExpr(lambdaExpr);
            var lambda = (Lambda) lambdaExpr.getElement();
            var retType = lambdaExpr.returnType();
            if (retType != null)
                lambda.setReturnType(retType.getType());
            return null;
        }
    }

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        try (var scope = env.enterScope()) {
            var clazz = classDecl.getElement();
            clazz.getTypeParameters().forEach(scope::add);
            clazz.getClasses().forEach(scope::add);
            if (clazz.isEnum()) {
                var enumClass = pkg.getRoot().subPackage("java").subPackage("lang")
                        .getClass("Enum");
                clazz.setInterfaces(List.of(enumClass.getType(List.of(clazz.getType()))));
                classDecl.enumConstants().forEach(ec -> ec.accept(this));
                for (Method method : clazz.getMethods()) {
                    if (method.getName() == SymName.from("values") && method.isStatic() && method.getParameters().isEmpty()) {
                        method.setReturnType(Types.instance.getArrayType(clazz.getType()));
                    }
                    else if (method.getName() == SymName.from("valueOf") && method.isStatic() &&
                            method.getParameterTypes().equals(List.of(Types.instance.getNullableString()))) {
                        method.setReturnType(Types.instance.getNullableType(clazz.getType()));
                    }
                }
            } else {
                classDecl.getImplements().forEach(t -> t.accept(this));
                clazz.setInterfaces(classDecl.getImplements().map(t -> {
                    var it = (ClassType) t.getType();
                    it.getClazz().getClasses().forEach(scope::add);
                    return it;
                }));
                classDecl.getTypeParameters().forEach(tv -> tv.accept(this));
            }
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
