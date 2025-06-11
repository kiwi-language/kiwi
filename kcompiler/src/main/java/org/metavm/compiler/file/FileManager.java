package org.metavm.compiler.file;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final Map<Path, SourceFile> fileMap = new HashMap<>();
    private final ByteBuf buf = new ByteBuf();
    private final Charset charset = StandardCharsets.UTF_8;

    public SourceFile getFile(Path path) {
        return fileMap.computeIfAbsent(path, p -> new PathSourceFile(p, this));
    }

    CharBuffer loadContent(Path path) throws IOException {
        try (var input = Files.newInputStream(path)) {
            buf.read(input);
            return buf.decode(charset);
        }
        finally {
            buf.clear();
        }
    }

    private void ensureCapacity() {

    }

}
