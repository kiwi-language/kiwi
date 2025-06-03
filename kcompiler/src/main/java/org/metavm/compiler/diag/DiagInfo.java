package org.metavm.compiler.diag;

public class DiagInfo {
    private final DiagCode code;
    private final Object[] args;

    public DiagInfo(DiagCode code, Object[] args) {
        this.code = code;
        this.args = args;
    }

    public DiagCode code() {
        return code;
    }

    public Object[] args() {
        return args;
    }

}
