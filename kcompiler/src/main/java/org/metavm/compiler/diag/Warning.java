package org.metavm.compiler.diag;

public class Warning extends DiagInfo {
    public Warning(DiagCode code, Object...args) {
        super(code, args);
    }
}
