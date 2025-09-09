package org.metavm.compiler.diag;

import org.metavm.compiler.file.SourceFile;
import org.metavm.compiler.util.List;

import java.io.PrintWriter;

public class DefaultLog implements Log {
    private DiagSource source;
    private final DiagFactory diagFactory;
    private final PrintWriter out;
    private final PrintWriter errOut;
    private List<Diag> diags = List.of();
    private int errorCount;
    private boolean delayed = true;

    public DefaultLog(SourceFile file, DiagFactory diagFactory, PrintWriter out, PrintWriter errOut) {
        this.source = new DiagSource(file, this);
        this.diagFactory = diagFactory;
        this.out = out;
        this.errOut = errOut;
    }

    @Override
    public void error(int pos, Error error) {
        error(new SimpDiagPos(pos), error);
    }

    @Override
    public void error(DiagPos pos, Error error) {
        report(diagFactory.error(source, pos, error));
    }

    @Override
    public void warn(DiagPos pos, Warning warning) {
        report(diagFactory.warn(source, pos, warning));
    }

    @Override
    public void note(DiagPos pos, Note note) {
        report(diagFactory.note(source, pos, note));
    }

    public int getErrorCount() {
        return errorCount;
    }

    private void report(Diag diag) {
        diags = diags.prepend(diag);
        if (diag.getInfo() instanceof Error)
            errorCount++;
        if (!delayed)
            flush();
    }

    private void write(Diag diag) {
        printMsg(diag.toString(), getOut(diag));
    }

    private void printMsg(String msg, PrintWriter out) {
        int idx;
        while ((idx = msg.indexOf('\n')) != -1) {
            var line = msg.substring(0, idx);
            msg = msg.substring(idx + 1);
            out.println(line);
        }
        if (!msg.isEmpty())
            out.print(msg);
        out.println();
    }

    private PrintWriter getOut(Diag diag) {
        return switch (diag.getInfo()) {
            case Warning ignored -> errOut;
            case Error ignored -> errOut;
            default -> out;
        };
    }

    @Override
    public void flush() {
        diags.forEachBackwards(this::write);
        diags = List.of();
        out.flush();
        errOut.flush();
    }

    @Override
    public List<Diag> getDiags() {
        return diags;
    }

    @Override
    public void setDiags(List<Diag> diags) {
        this.diags = diags;
    }

    @Override
    public DiagSource getSource() {
        return source;
    }

    @Override
    public void setSourceFile(SourceFile sourceFile) {
        source = new DiagSource(sourceFile, this);
    }

    public void setSource(DiagSource source) {
        this.source = source;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

}
