package org.metavm.compiler.diag;

public class DiagFactory {

    public static final DiagFactory instance = new DiagFactory(new BasicDiagFmt());

    public final DiagFmt formatter;

    public DiagFactory(DiagFmt formatter) {
        this.formatter = formatter;
    }

    public Diag error(DiagSource source, DiagPos pos, Error error) {
        return create(source, pos, error);
    }

    public Diag warn(DiagSource source, DiagPos pos, Warning warning) {
        return create(source, pos, warning);
    }

    public Diag note(DiagSource source, DiagPos pos, Note note) {
        return create(source, pos, note);
    }

    private Diag create(DiagSource source, DiagPos pos, DiagInfo info) {
        return new Diag(formatter, source, pos, info);
    }

}
