package org.metavm.compiler.syntax;

import javassist.bytecode.stackmap.TypeData;
import org.metavm.compiler.element.*;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.object.instance.core.Id;

public class NodeMaker {

    public static ParamDecl makeParamDecl(SymName name, Type type, Executable executable) {
        var paramDecl = new ParamDecl(
                makeType(type),
                new Ident(name)
        );
        paramDecl.setElement(new Parameter(name, type, executable));
        return paramDecl;
    }

    public static TypeNode makeType(Type type) {
        return type.makeNode();
    }

    public static RefExpr makeRefExpr(SymName name, Element element) {
        var refExpr = new RefExpr(new Ident(name));
        refExpr.setElement(element);
        return refExpr;
    }

    public static CallExpr makeCallExpr(Expr func,
                                        List<Type> typeArguments,
                                        List<Expr> arguments) {
        assert func.getType() instanceof FunctionType : "Expression '" + func.getText() + "' is not a function expression";
        var expr = new CallExpr(
                func,
                typeArguments.map(Type::makeNode),
                arguments
        );
        expr.setElement(func.getElement());
        return expr;
    }

    public static NewExpr makeNewExpr(MethodInst init, List<Expr> arguments) {
        var expr = new NewExpr(
                null,
                init.getDeclaringType().makeNode(),
                arguments
        );
        expr.setElement(init);
        return expr;
    }

    public static Literal makeLiteral(Object value) {
        return new Literal(value);
    }

    public static NewArrayExpr makeNewArrayExpr(Type elementType, List<Expr> elements) {
        var expr = new NewArrayExpr(elementType.makeNode(), false, elements);
        expr.setType(Types.instance.getArrayType(elementType));
        return expr;
    }

    public static RefExpr makeClassRef(Clazz clazz) {
        var expr = new RefExpr(new Ident(clazz.getName()));
        expr.setElement(clazz);
        return expr;
    }

    public static RefExpr makeFuncRef(FreeFuncInst func) {
        var expr = new RefExpr(new Ident(func.getName()));
        expr.setElement(func);
        return expr;
    }

    public static RefExpr makeStaticMethodRef(MethodInst method) {
        var expr = new RefExpr(new Ident(method.getName()));
        expr.setElement(method);
        return expr;
    }

    public static RefExpr makeStaticFieldRef(FieldRef field) {
        var expr = new RefExpr(new Ident(field.getName()));
        expr.setElement(field);
        return expr;
    }

    public static RefExpr makeParamRef(Parameter param) {
        var expr = new RefExpr(new Ident(param.getName()));
        expr.setElement(param);
        return expr;
    }

    public static MethodDecl makeMethodDecl(Method method, List<Stmt> body) {
        var mods = List.<Modifier>builder();
        if (method.isStatic())
            mods.append(new Modifier(ModifierTag.STATIC));
        if (method.isAbstract())
            mods.append(new Modifier(ModifierTag.ABSTRACT));
        switch (method.getAccess()) {
            case PUBLIC -> mods.append(new Modifier(ModifierTag.PUBLIC));
            case PRIVATE -> mods.append(new Modifier(ModifierTag.PRIVATE));
            case PROTECTED -> mods.append(new Modifier(ModifierTag.PROTECTED));
        }
        var methodDecl = new MethodDecl(
                mods.build(),
                method.getTypeParameters().map(NodeMaker::makeTypeVariableDecl),
                new Ident(method.getName()),
                method.getParameters().map(NodeMaker::makeParamDecl),
                method.getReturnType().makeNode(),
                method.isAbstract() ? null : new Block(body)
        );
        methodDecl.setElement(method);
        return methodDecl;
    }

    public static ParamDecl makeParamDecl(Parameter parameter) {
        var paramDecl = new ParamDecl(parameter.getType().makeNode(), new Ident(parameter.getName()));
        paramDecl.setElement(parameter);
        return paramDecl;
    }

    public static TypeVariableDecl makeTypeVariableDecl(TypeVariable typeVariable) {
        var decl = new TypeVariableDecl(new Ident(typeVariable.getName()), typeVariable.getBound().makeNode());
        decl.setElement(typeVariable);
        return decl;
    }

}
