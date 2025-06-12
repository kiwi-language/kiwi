package org.metavm.compiler.file;

import org.metavm.compiler.util.CompilationException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.CharBuffer;

public interface SourceFile {

    String getPath();

    CharBuffer getContent() throws IOException;

    default SourcePos computePos(int pos) throws IOException {
        return computePos(getContent(), pos);
    }

    default SourceLine getLine(int pos) {
        try {
            var cb = getContent();
            return getLine(cb, pos);
        } catch (IOException e) {
            throw new CompilationException("Failed to read source file " + getPath(), e);
        }
    }

    static @Nullable SourceLine getLine(CharBuffer buf, int pos) {
        assert pos >= 0;
        var len = buf.length();
        var lineStart = 0;
        for (var i = 0 ;i < len; i++) {
            if (i == pos) {
                var j = i;
                for (; j < len; j++) {
                    var c = buf.charAt(j);
                    if (c == '\r' || c == '\n')
                        break;
                }
                return new SourceLine(
                        buf.subSequence(lineStart, j).toString(),
                        lineStart
                );
            }
            var c = buf.charAt(i);
            switch (c) {
                case '\n' -> lineStart = i + 1;
                case '\r' -> {
                    if (i < len - 1 && buf.charAt(i + 1) == '\n')
                        i++;
                    lineStart = i + 2;
                }
            }
        }
        return null;
    }

    static SourcePos computePos(CharBuffer cb, int pos) {
        if (pos < 0 || pos >= cb.length())
            return new SourcePos(-1, -1);
        var a = cb.array();
        var line = 1;
        var column = 1;
        for (int i = 0; i < pos; i++) {
            var c = a[i];
            if (c == '\r') {
                line++;
                column = 1;
                if (i < pos - 1 && a[i + 1] == '\n')
                    i++;
            }
            else if (c == '\n') {
                line++;
                column = 1;
            }
            else
                column++;
        }
        return new SourcePos(line, column);

    }

}
