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

    private Expr expr;

    public ClassTypeNode(Expr expr) {
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

    private Object resolveExpr(Expr expr, Env env) {
        if (expr instanceof Ident ref) {
            var element = env.lookupFirst(ref.getName(), EnumSet.of(ResolveKind.TYPE, ResolveKind.PACKAGE));
            if (element == null)
                throw new ResolutionException("Symbol " + ref.getText() + " not found");
            if (element instanceof ClassType ct)
                return ct;
            else
                return element;
        } else if (expr instanceof SelectorExpr qualName) {
            var name = qualName.sel();
            var scope = resolveExpr(qualName.x(), env);
            if (scope instanceof Package pkg) {
                var clazz = pkg.getTable().lookupFirst(name, e -> e instanceof Clazz);
                if (clazz != null)
                    return clazz;
                var subPkg = pkg.getTable().lookupFirst(name, e -> e instanceof Package);
                if (subPkg != null)
                    return subPkg;
                throw new ResolutionException("Symbol " + expr.getText() + " not found");
            }
            else if (scope instanceof ClassType owner) {
                var clazz = owner.getClazz().getClass(name);
                return clazz.getInst(owner, clazz.getTypeParams());
            }
        } else if (expr instanceof TypeApply typeApply) {
            var clazz = (ClassType) resolveExpr(typeApply.getExpr(), env);
            return clazz.getClazz().getInst(
                    clazz.getOwner(),
                    typeApply.getArgs().map(env::resolveType)
            );
        }
        throw new ResolutionException("Invalid class name " + expr.getText() + " (class: " + expr.getClass().getName() + ")");
    }

}