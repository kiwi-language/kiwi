package org.manul.compiler.diag;

public class Error extends DiagInfo {


    public Error(String code, Object...args) {
        super(code, args);
    }
}
