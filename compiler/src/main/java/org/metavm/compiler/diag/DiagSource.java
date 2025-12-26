package org.metavm.compiler.diag;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.file.SourceFile;

public record DiagSource(@NotNull SourceFile file, Log log) {
}
