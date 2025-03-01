package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.analyze.ResolveKind;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.ResolutionException;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;

import java.util.EnumSet;
import java.util.function.Consumer;

public final class ClassTypeNode extends TypeNode {

    private Node expr;

    public ClassTypeNode(Node expr) {
        this.expr = expr;
    }

    public Node getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(expr);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitClassTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(expr);
    }

    @Override
    protected Type actualResolve(Env env) {
        if (resolveExpr(expr, env) instanceof Type t)
            return t;
        else
            throw new ResolutionException("Invalid class type " + expr.getText());
    }

    private Object resolveExpr(Node expr, Env env) {
        if (expr instanceof Ident name) {
            var element = env.lookupFirst(name.value(), EnumSet.of(ResolveKind.TYPE, ResolveKind.PACKAGE));
            if (element == null)
                throw new ResolutionException("Symbol " + name.getText() + " not found");
            if (element instanceof Clazz clazz)
                return clazz.getType();
            else
                return element;
        } else if (expr instanceof QualifiedName qualName) {
            var name = qualName.getIdent().value();
            var scope = resolveExpr(qualName.getQualifier(), env);
            if (scope instanceof Package pkg) {
                var clazz = pkg.findClass(name);
                if (clazz != null)
                    return clazz.getType();
                throw new ResolutionException("Class " + expr.getText() + " not found");
            }
            else if (scope instanceof ClassType owner) {
                var clazz = owner.getClazz().getClass(name);
                return clazz.getType(owner, clazz.getTypeParameters());
            }
        } else if (expr instanceof TypeApply typeApply) {
            var clazz = (ClassType) resolveExpr(typeApply.clazz(), env);
            return clazz.getClazz().getType(
                    clazz.getOwner(),
                    typeApply.typeArguments().map(env::resolveType)
            );
        }
        throw new ResolutionException("Invalid class name " + expr.getText() + " (class: " + expr.getClass().getName() + ")");
    }

}