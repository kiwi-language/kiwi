package org.metavm.compiler.diag;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;

public class BasicDiagFmt implements DiagFmt {

    private final Messages messages = Messages.instance;
    public static final String INDENT = "    ";
    public static final boolean DISPLAY_SOURCE = true;
    public static final boolean ADD_CARET = true;

    @Override
    public String format(Diag diag, Locale l) {
        String ptn;
        try {
            ptn = messages.getMessage(l, diag.getCode());
        } catch (MissingResourceException e) {
            ptn = "Broken diagnostic format. arguments: {0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}";
        }
        var argStrs = new Object[diag.getArgs().length];
        for (int i = 0; i < diag.getArgs().length; i++) {
            argStrs[i] = formatArg(diag.getArgs()[i]);
        }
        var buf = new StringBuilder(MessageFormat.format(ptn, argStrs));
        addSourceLineIfNeeded(buf, diag);
        return buf.toString();
    }

    private String formatArg(Object arg) {
        if (arg instanceof Formattable fmt)
            return fmt.toString(Locale.getDefault(), messages);
        else
            return Objects.toString(arg);
    }


    private void addSourceLineIfNeeded(StringBuilder buf, Diag diag) {
        if (DISPLAY_SOURCE) {
            buf.append('\n');
            var pos = diag.diagPos().getIntPos();
            var line = diag.file().file().getLine(pos);
            if (line == null)
                return;
            buf.append(INDENT).append(line.line()).append('\n');
            var col = pos - line.startPos();
            if (ADD_CARET) {
                for (int i = 0; i < col; i++) {
                    buf.append(line.line().charAt(i) == '\t' ? '\t' : ' ');
                }
                buf.append(INDENT).append("^");
            }
        }
    }


}
