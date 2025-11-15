package org.metavm.compiler.analyze;

import org.metavm.compiler.type.Type;

public class ResultInfo {
    private Type type;

    public ResultInfo(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
