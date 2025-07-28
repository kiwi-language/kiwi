package org.metavm.compiler;

import java.nio.file.Path;
import java.util.Collection;

public class CompilationTaskBuilder {

    public static CompilationTaskBuilder newBuilder(Collection<Path> sourcePaths, Path targetDir) {
        return new CompilationTaskBuilder(sourcePaths, targetDir);
    }

    private final Collection<Path> sourcePaths;
    private final Path targetDir;
    private boolean aiLint;

    public CompilationTaskBuilder(Collection<Path> sourcePaths, Path targetDir) {
        this.sourcePaths = sourcePaths;
        this.targetDir = targetDir;
    }

    public CompilationTaskBuilder withAiLint(boolean aiLint) {
        this.aiLint = aiLint;
        return this;
    }

    public CompilationTask build() {
        return new CompilationTask(sourcePaths, targetDir, aiLint);
    }

}
