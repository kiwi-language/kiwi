package org.metavm.compiler.file;


import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.CharBuffer;
import java.nio.file.Path;

public class PathSourceFile implements SourceFile {
    private final Path path;
    private final FileManager fileManager;
    private @Nullable SoftReference<CharBuffer> content;

    public PathSourceFile(Path path, FileManager fileManager) {
        this.path = path;
        this.fileManager = fileManager;
    }

    @Override
    public CharBuffer getContent() throws IOException {
        CharBuffer c;
        if (content != null && (c = content.get()) != null)
            return c;
        c = fileManager.loadContent(path);
        content = new SoftReference<>(c);
        return c;
    }

    @Override
    public SourcePos computePos(int pos) throws IOException {
        var cb = getContent();
        if (pos < 0 || pos >= cb.length())
            return new SourcePos(-1, -1);
        var a = cb.array();
        var row = 0;
        var col = 0;
        for (int i = 0; i < pos; i++) {
            var c = a[i];
            if (c == '\r') {
                row++;
                col = 0;
                if (i < pos - 1 && a[i + 1] == '\n')
                    i++;
            }
            else if (c == '\n') {
                row++;
                col = 0;
            }
            else
                col++;
        }
        return new SourcePos(row, col);
    }

}
