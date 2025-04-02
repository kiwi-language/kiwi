package org.metavm.compiler;

import org.metavm.compiler.util.MockEnter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        var sources = listFilePathsRecursively("src");
        var task = new CompilationTask(sources, "target");
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        task.generate();
    }

    public static List<String> listFilePathsRecursively(String dirPath) throws IOException {
        Path start = Paths.get(dirPath);
        if (!Files.exists(start) || !Files.isDirectory(start)) {
            throw new IllegalArgumentException("Provided path is not an existing directory: " + dirPath);
        }

        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath) // Convert Path to absolute Path
                    .map(Path::toString)       // Convert absolute Path to String
                    .collect(Collectors.toList());
        }
    }


}
