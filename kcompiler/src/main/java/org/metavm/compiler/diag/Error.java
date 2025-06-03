package org.metavm.compiler.diag;

public class Error extends DiagInfo {


    public Error(DiagCode code, Object...args) {
        super(code, args);
    }
}
