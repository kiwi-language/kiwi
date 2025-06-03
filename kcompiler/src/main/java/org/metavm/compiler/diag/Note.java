package org.metavm.compiler.diag;

public class Note extends DiagInfo {
    public Note(DiagCode code, Object...args) {
        super(code, args);
    }
}
