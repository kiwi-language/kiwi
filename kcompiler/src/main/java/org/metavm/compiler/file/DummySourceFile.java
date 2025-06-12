package org.metavm.compiler.file;

import java.nio.CharBuffer;

public record DummySourceFile(String text) implements SourceFile{
    @Override
    public String getPath() {
        return "dummy.kiwi";
    }

    @Override
    public CharBuffer getContent() {
        return CharBuffer.wrap(text.toCharArray());
    }
}
