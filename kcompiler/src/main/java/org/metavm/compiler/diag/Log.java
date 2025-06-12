package org.metavm.compiler.diag;

import org.metavm.compiler.util.List;

public interface Log {
    void error(int pos, Error error);

    void error(DiagPos pos, Error error);

    void warn(DiagPos pos, Warning warning);

    default void warn(int pos, Warning warning) {
        warn(new SimpDiagPos(pos), warning);
    }

    default void note(int pos, Note note) {
        note(new SimpDiagPos(pos), note);
    }

    void note(DiagPos pos, Note note);

    void flush();

    List<Diag> getDiags();

    void setDiags(List<Diag> diags);

    DiagSource getSource();

}
