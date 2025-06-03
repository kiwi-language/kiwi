package org.metavm.compiler.diag;

import org.metavm.compiler.file.SourceFile;

import java.io.PrintWriter;

public class Log {
    private final DiagSource source;
    private final DiagFactory diagFactory;
    private final PrintWriter out;
    private final PrintWriter errOut;
    private final DiagBuf buf = new DiagBuf();

    public Log(SourceFile file, DiagFactory diagFactory, PrintWriter out, PrintWriter errOut) {
        this.source = new DiagSource(file, this);
        this.diagFactory = diagFactory;
        this.out = out;
        this.errOut = errOut;
    }

    public void error(int pos, Error error) {
        error(new SimpDiagPos(pos), error);
    }

    public void error(DiagPos pos, Error error) {
        report(diagFactory.error(source, pos, error));
    }

    public void warn(DiagPos pos, Warning warning) {
        report(diagFactory.warn(source, pos, warning));
    }

    public void note(DiagPos pos, Note note) {
        report(diagFactory.note(source, pos, note));
    }

    private void report(Diag diag) {
        buf.add(diag);
    }

    private void write(Diag diag) {
        var out = getOut(diag);
        printMsg(diag.toString(), out);
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
    }

    private PrintWriter getOut(Diag diag) {
        return switch (diag.getInfo()) {
            case Warning ignored -> errOut;
            case Error ignored -> errOut;
            default -> out;
        };
    }

    public void flush() {
        buf.flush(this::write);
        out.flush();
        errOut.flush();
    }

}
