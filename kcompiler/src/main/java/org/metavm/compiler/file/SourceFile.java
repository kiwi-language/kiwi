package org.metavm.compiler.file;

import java.io.IOException;
import java.nio.CharBuffer;

public interface SourceFile {
    CharBuffer getContent() throws IOException;

    SourcePos computePos(int pos) throws IOException;
}
