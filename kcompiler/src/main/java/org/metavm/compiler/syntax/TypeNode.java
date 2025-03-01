package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;

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
}
