package org.metavm.compiler;

import java.nio.file.Path;
import java.util.Collection;

public class CompilationTaskBuilder {

    public static CompilationTaskBuilder newBuilder(Collection<Path> sourcePaths, Path targetDir) {
        return new CompilationTaskBuilder(sourcePaths, targetDir);
    }

    private final Collection<Path> sourcePaths;
    private final Path targetDir;
    private boolean senseLint;

    public CompilationTaskBuilder(Collection<Path> sourcePaths, Path targetDir) {
        this.sourcePaths = sourcePaths;
        this.targetDir = targetDir;
    }

    public CompilationTaskBuilder withSenseLint(boolean senseLint) {
        this.senseLint = senseLint;
        return this;
    }

    public CompilationTask build() {
        return new CompilationTask(sourcePaths, targetDir, senseLint);
    }

}
