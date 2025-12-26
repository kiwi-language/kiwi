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
    public String getPath() {
        return path.toAbsolutePath().toString();
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

}
