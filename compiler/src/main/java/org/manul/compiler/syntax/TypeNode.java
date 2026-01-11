package org.manul.compiler.syntax;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.type.Type;

public abstract class TypeNode extends Node {

    protected Type type;

    public Type resolve(Env env) {
        if (type == null)
            type = actualResolve(env);
        return type;
    }

    protected abstract Type actualResolve(Env env);

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public TypeNode setPos(int pos) {
        return (TypeNode) super.setPos(pos);
    }
}
