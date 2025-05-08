package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.BuiltinVariable;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

@Slf4j
public class Extend extends Node {

    private final TypeNode type;
    private List<Expr> args;

    public Extend(TypeNode type, List<Expr> args) {
        this.type = type;
        this.args = args;
    }

    public Extend(TypeNode type) {
        this(type, List.nil());
    }

    public TypeNode getType() {
        return type;
    }

    public List<Expr> getArgs() {
        return args;
    }

    public void setArgs(List<Expr> args) {
        this.args = args;
    }

    public Call makeCallExpr() {
        var ct = (ClassType) type.getType();
        return NodeMaker.callExpr(
                NodeMaker.selectorExpr(
                        NodeMaker.ref(new BuiltinVariable(Name.super_(), null, ct)),
                        ct.getPrimaryInit()
                ),
                args
        );
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(type);
        if (args.nonEmpty()) {
            writer.write("(");
            writer.write(args);
            writer.write(")");
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitExtend(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(type);
        args.forEach(action);
    }

}
