package org.metavm.compiler.diag;

import org.metavm.compiler.file.DummySourceFile;
import org.metavm.compiler.util.List;

public class DummyLog implements Log {
    @Override
    public void error(int pos, Error error) {

    }

    @Override
    public void error(DiagPos pos, Error error) {

    }

    @Override
    public void warn(DiagPos pos, Warning warning) {

    }

    @Override
    public void note(DiagPos pos, Note note) {

    }

    @Override
    public void flush() {

    }

    @Override
    public List<Diag> getDiags() {
        return List.of();
    }

    @Override
    public void setDiags(List<Diag> diags) {

    }

    @Override
    public DiagSource getSource() {
        return new DiagSource(new DummySourceFile(""), this);
    }
}
