package org.metavm.compiler.diag;

import org.metavm.compiler.file.SourceFile;

public record DiagSource(SourceFile file, Log log) {
}
