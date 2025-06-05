package org.metavm.compiler.diag;

public interface Log {
    void error(int pos, Error error);

    void error(DiagPos pos, Error error);

    void warn(DiagPos pos, Warning warning);

    void note(DiagPos pos, Note note);

    void flush();
}
