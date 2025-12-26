package org.metavm.compiler.diag;

public class SimpDiagPos implements DiagPos {

    private final int pos;

    public SimpDiagPos(int pos) {
        this.pos = pos;
    }

    @Override
    public int getIntPos() {
        return pos;
    }
}
