package org.manul.compiler.diag;

import org.jetbrains.annotations.NotNull;
import org.manul.compiler.file.SourceFile;

public record DiagSource(@NotNull SourceFile file, Log log) {
}
