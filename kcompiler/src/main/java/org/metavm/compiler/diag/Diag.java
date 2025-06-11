package org.metavm.compiler.diag;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.file.SourcePos;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;

public final class Diag {
    private final DiagFmt formatter;
    private final DiagSource source;
    private final DiagPos diagPos;
    private @Nullable SourcePos sourcePos;
    private final DiagInfo info;

    public Diag(
            DiagFmt formatter,
            @NotNull DiagSource source,
            DiagPos diagPos,
            DiagInfo info
    ) {
        this.formatter = formatter;
        this.source = source;
        this.diagPos = diagPos;
        this.info = info;
    }

    public DiagFmt formatter() {
        return formatter;
    }

    public DiagSource file() {
        return source;
    }

    public DiagInfo getInfo() {
        return info;
    }

    public DiagPos diagPos() {
        return diagPos;
    }

    public SourcePos sourcePos() {
        if (sourcePos == null)
            sourcePos = buildSourcePos();
        return sourcePos;
    }

    public String getCode() {
        return info.code();
    }

    public Object[] getArgs() {
        return info.args();
    }

    private SourcePos buildSourcePos() {
        try {
            return source.file().computePos(diagPos.getIntPos());
        } catch (IOException e) {
            return new SourcePos(-1, -1);
        }
    }

    public String toString() {
        var msg = formatter.format(this, Locale.getDefault());
        return String.format("%s:%d: %s", source.file().getPath(), sourcePos().line(), msg);
    }

}
