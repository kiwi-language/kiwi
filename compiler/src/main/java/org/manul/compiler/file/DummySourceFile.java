package org.manul.compiler.file;

import java.nio.CharBuffer;

public record DummySourceFile(String text) implements SourceFile{
    @Override
    public String getPath() {
        return "dummy.mnl";
    }

    @Override
    public CharBuffer getContent() {
        return CharBuffer.wrap(text.toCharArray());
    }
}
