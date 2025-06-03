package org.metavm.compiler.diag;

import org.metavm.compiler.util.List;

import java.util.function.Consumer;

public class DiagBuf {
    private List.Builder<Diag> buf = List.builder();

    public void add(Diag diag) {
        buf.append(diag);
    }

    public void flush(Consumer<Diag> action) {
        buf.forEach(action);
        buf = List.builder();
    }

}
