package org.metavm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KiwiServerManager {

    private static final String APP_NAME = "KiwiServer";
    
    private static final Path SCRIPT_DIR = getScriptDir();
    private static final Path APP_ROOT_DIR = SCRIPT_DIR.getParent().toAbsolutePath().normalize();

    // Configurable paths
    private static final Path CONFIG_FILE = APP_ROOT_DIR.resolve("conf").resolve("kiwi.yml");
    private static final Path LOG_DIR = APP_ROOT_DIR.resolve("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve(APP_NAME + ".log");

    public static void main(String[] args) {
        try {
            // Ensure log directory exists
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }
            startServer();
        } catch (Exception e) {
            System.exit(1);
        }
    }

    private static void startServer() {

        System.out.println("Starting " + APP_NAME);

        try {
            ObjectApplication.main(new String[]{"-config", CONFIG_FILE.toString()});
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Error: Failed to start " + APP_NAME + ". Check " + LOG_FILE + " for details.");
            System.exit(1);
        }

        System.out.println(APP_NAME + " started successfully. Output logged to " + LOG_FILE);
    }

    public static Path getScriptDir() {
        // 1. Check for Native Image (GraalVM)
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null)
            return getNativeImagePath();

        // 2. Check for Standard JVM (Jar or IDE)
        return getJarPath();
    }

    private static Path getNativeImagePath() {
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
            return executable
                    .getParent() // bin
                    .normalize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate stdlib in Native Image environment", e);
        }
    }

    private static Path getJarPath() {
        try {
            var codeSource = KiwiServerManager.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                // This can happen in some obscure classloading scenarios, unlikely in standard apps
                throw new IllegalStateException("Code source is null");
            }

            File codeLocation = new File(codeSource.getLocation().toURI());

            if (codeLocation.isFile() && codeLocation.getName().endsWith(".jar")) {
                // Structure: .../kiwi/bin/compiler.jar
                // Target:    .../kiwi/pkg/stdlib
                return codeLocation.toPath()
                        .getParent() // bin
                        .normalize();
            }

            throw new RuntimeException("Could not auto-detect stdlib path from CodeSource: " + codeLocation);

        } catch (Exception e) {
            throw new RuntimeException("Failed to locate Kiwi Standard Library", e);
        }
    }

}