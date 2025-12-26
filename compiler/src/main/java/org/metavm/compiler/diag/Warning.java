package org.metavm.compiler.diag;

public class Warning extends DiagInfo {
    public Warning(String code, Object...args) {
        super(code, args);
    }
}
