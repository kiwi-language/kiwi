package org.metavm.compiler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KiwiEnv {

    public static Path getStdLibPath() {
        // 1. Check for Native Image (GraalVM)
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null)
            return getNativeStdLibPath();

        // 2. Check for Standard JVM (Jar or IDE)
        return getJvmStdLibPath();
    }

    private static Path getNativeStdLibPath() {
        try {
            // In Java 9+, ProcessHandle allows us to get the path of the running executable
            String binaryPath = ProcessHandle.current().info().command().orElse(null);

            if (binaryPath == null) {
                throw new RuntimeException("Running in Native Image, but could not locate executable path.");
            }

            // Expected structure:
            //   .../kiwi/bin/kiwi (executable)
            //   .../kiwi/pkg/stdlib

            Path executable = Paths.get(binaryPath).toAbsolutePath().normalize();
            return executable.getParent() // bin
                    .getParent() // kiwi root
                    .resolve("pkg/std")
                    .normalize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate stdlib in Native Image environment", e);
        }
    }

    private static Path getJvmStdLibPath() {
        try {
            var codeSource = KiwiEnv.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                // This can happen in some obscure classloading scenarios, unlikely in standard apps
                throw new IllegalStateException("Code source is null");
            }

            File codeLocation = new File(codeSource.getLocation().toURI());

            // Case A: Running from JAR (Production / Dist)
            if (codeLocation.isFile() && codeLocation.getName().endsWith(".jar")) {
                // Structure: .../kiwi/bin/compiler.jar
                // Target:    .../kiwi/pkg/stdlib
                return codeLocation.toPath()
                        .getParent() // bin
                        .getParent() // kiwi root
                        .resolve("pkg/std")
                        .normalize();
            }

            // Case B: Running from Classes (IDE / Unit Test / Maven)
            else if (codeLocation.isDirectory()) {
                // Structure: .../kiwi/compiler/target/classes
                // Target:    .../kiwi/stdlib
                Path root = codeLocation.toPath()
                        .getParent() // target
                        .getParent() // compiler
                        .getParent(); // kiwi project root

                if (root != null) {
                    Path devStdLib = root.resolve("stdlib");
                    if (Files.exists(devStdLib)) {
                        return devStdLib.normalize();
                    }
                }
            }

            throw new RuntimeException("Could not auto-detect stdlib path from CodeSource: " + codeLocation);

        } catch (Exception e) {
            throw new RuntimeException("Failed to locate Kiwi Standard Library", e);
        }
    }
}