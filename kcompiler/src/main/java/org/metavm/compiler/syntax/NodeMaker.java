package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;

@Slf4j
public class NodeMaker {

    public static ClassParamDecl classParamDecl(Name name, Type type, Clazz clazz) {
        var decl = new ClassParamDecl(
                List.nil(),
                false,
                false,
                type.makeNode(),
                name
        );
        var init = Objects.requireNonNull(clazz.getPrimaryInit());
        decl.setElement(new Param(name, type, init));
        return decl;
    }

    public static ParamDecl paramDecl(Name name, Type type, Executable executable) {
        var paramDecl = new ParamDecl(
                makeType(type),
                name
        );
        paramDecl.setElement(new Param(name, type, executable));
        return paramDecl;
    }

    public static TypeNode makeType(Type type) {
        return type.makeNode();
    }

    public static ExprStmt exprStmt(Expr expr) {
        return new ExprStmt(expr);
    }

    public static RetStmt retStmt(@Nullable Expr expr) {
        return new RetStmt(expr);
    }

    public static TypeApply typeApplyExpr(Expr expr, List<? extends Type> typeArgs) {
        var typeApply = new TypeApply(expr, typeArgs.map(Type::makeNode));
        var genDecl = (GenericDecl) expr.getElement();
        typeApply.setElement(genDecl.getInst(List.into(typeArgs)));
        log.debug("type apply {}, element: {}", typeApply.getText(), typeApply.getElement().getText());
        return typeApply;
    }

    public static Call callExpr(Expr func,
                                List<Expr> arguments) {
        assert func.getType() instanceof FuncType :
                "Expression '" + func.getText() + "' is not a function expression. type: " + func.getType().getTypeText();
        var expr = new Call(
                func,
                arguments
        );
        expr.setElement(func.getElement());
        return expr;
    }

    public static Call makeNewExpr(MethodRef init, List<Expr> arguments) {
        var expr = new Call(
                initRef(init),
                arguments
        );
        expr.setElement(init);
        return expr;
    }

    public static Ident initRef(MethodRef inst) {
        var ref = new Ident(inst.getDeclType().getClazz().getName());
        ref.setElement(inst);
        return ref;
    }

    public static AssignExpr makeAssignExpr(Expr lhs, Expr rhs) {
        var expr = new AssignExpr(
                null,
                lhs,
                rhs
        );
        expr.setType(lhs.getType());
        return expr;
    }

    public static Literal literal(Object value) {
        return new Literal(value);
    }

    public static NewArrayExpr makeNewArrayExpr(Type elementType, List<Expr> elements) {
        var expr = new NewArrayExpr(elementType.makeNode(), false, elements);
        expr.setType(Types.instance.getArrayType(elementType));
        return expr;
    }

    public static Ident ref(Element element) {
        var expr = new Ident(element.getName());
        expr.setElement(element);
        return expr;
    }

    public static SelectorExpr selectorExpr(Expr qualifier, Element element) {
        var expr = new SelectorExpr(qualifier, element.getName());
        expr.setElement(element);
        return expr;
    }

    public static MethodDecl methodDecl(Method method, List<Stmt> stmts) {
        var mods = List.<Modifier>builder();
        if (method.isStatic())
            mods.append(new Modifier(ModifierTag.STATIC));
        if (method.isAbstract())
            mods.append(new Modifier(ModifierTag.ABSTRACT));
        switch (method.getAccess()) {
            case PUBLIC -> mods.append(new Modifier(ModifierTag.PUB));
            case PRIVATE -> mods.append(new Modifier(ModifierTag.PRIV));
            case PROTECTED -> mods.append(new Modifier(ModifierTag.PROT));
        }
        var methodDecl = new MethodDecl(
                mods.build(),
                method.getTypeParams().map(NodeMaker::makeTypeVariableDecl),
                method.getName(),
                method.getParams().map(NodeMaker::paramDecl),
                method.isInit() ? null : method.getRetType().makeNode(),
                method.isAbstract() ? null : new Block(stmts)
        );
        methodDecl.setElement(method);
        return methodDecl;
    }

    public static ParamDecl paramDecl(Param param) {
        var paramDecl = new ParamDecl(param.getType().makeNode(), param.getName());
        paramDecl.setElement(param);
        return paramDecl;
    }

    public static TypeVariableDecl makeTypeVariableDecl(TypeVar typeVar) {
        var decl = new TypeVariableDecl(typeVar.getName(), typeVar.getBound().makeNode());
        decl.setElement(typeVar);
        return decl;
    }

    public static LambdaExpr lambdaExpr(Lambda l, Node body) {
        var expr = new LambdaExpr(
                l.getParams().map(NodeMaker::paramDecl),
                l.getRetType().makeNode(),
                body
        );
        expr.setElement(l);
        return expr;
    }
}
