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
                buf.append(INDENT);
                for (int i = 0; i < col; i++) {
                    var c = line.line().charAt(i);
                    if (c == '\t')
                        buf.append('\t');
                    else if (isFullWidth(c))
                        buf.append("　");
                    else
                        buf.append(" ");
                }
                buf.append("^");
            }
        }
    }

    private boolean isFullWidth(char c) {
        // This heuristic is based on Unicode character blocks.
        // It covers most common CJK characters and symbols.
        return !(c < 256 ||                  // Basic Latin, Latin-1 Supplement (fast path)
                (c >= 0xff61 && c <= 0xff9f) || // Half-width Katakana
                (c >= 0xffa0 && c <= 0xffdc) || // Half-width Hangul Jamo
                (c >= 0xffe8 && c <= 0xffee));  // Half-width symbols
    }

}
