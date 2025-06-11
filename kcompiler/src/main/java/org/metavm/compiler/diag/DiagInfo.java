package org.metavm.compiler.diag;

public class DiagInfo {
    private final String code;
    private final Object[] args;

    public DiagInfo(String code, Object[] args) {
        this.code = code;
        this.args = args;
    }

    public String code() {
        return code;
    }

    public Object[] args() {
        return args;
    }

}
