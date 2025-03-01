package org.metavm.compiler.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompilerUtils {

    public static void createArchive(String buildDir) {
        var targetDir = Paths.get(buildDir);
        var zipFilePath = targetDir + "/target.mva";
        try(var zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            var files = Files.walk(targetDir)) {
            files.filter(f -> f.toString().endsWith(".mvclass"))
                    .forEach(f -> {
                        var zipEntry = new ZipEntry(targetDir.relativize(f).toString());
                        try {
                            zipOut.putNextEntry(zipEntry);
                            Files.copy(f, zipOut);
                            zipOut.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
