package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.element.BuiltinVariable;
import org.metavm.compiler.element.Method;
import org.metavm.compiler.element.MethodRef;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.ErrorType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class Extend extends Node {

    private Type type;
    private Expr expr;

    public Extend(Expr expr) {
        this.expr = expr;
    }

    public Type getType() {
        return type;
    }

    public Expr getExpr() {
        return expr;
    }

    public List<Expr> getArgs() {
        return expr instanceof Call call ? call.getArguments() : List.of();
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public Call makeSuperInit() {
        var call = (Call) expr;
        var method = (MethodRef) call.getElement();
        return NodeMaker.callExpr(
                NodeMaker.selectorExpr(
                        NodeMaker.ref(new BuiltinVariable(method.getDeclType(), Name.super_(), null, method.getDeclType())),
                        call.getElement()
                ),
                call.getArguments()
        );
    }

    @Override
    public void write(SyntaxWriter writer) {
        expr.write(writer);
    }

    public Type resolveType(Env env) {
        type = Types.resolveType(getTypeExpr(), env);
        if (type instanceof ClassType ct) {
            if (ct.isInterface()) {
                if (expr instanceof Call call)
                    expr = call.getFunc();
            }
            else  {
                if (!(expr instanceof Call))
                    expr = new Call(expr, List.of()).setPos(expr.getIntPos());
            }
        } else if (type != ErrorType.instance) {
            type = ErrorType.instance;
            env.getLog().error(expr, Errors.unexpectedType);
        }
        return type;

    }

    public Expr getTypeExpr() {
        return expr instanceof Call call ? call.getFunc() : expr;
    }


    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitExtend(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
    }

    @Override
    public Extend setPos(int pos) {
        return (Extend) super.setPos(pos);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Extend extend = (Extend) object;
        return Objects.equals(expr, extend.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }
}
