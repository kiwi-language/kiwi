package tech.metavm.util;

import java.io.*;

public class GenerateMavenCoordinates {

    public static final String INPUT_FILE = "/Users/leen/Desktop/wad.txt";
    public static final String OUTPUT_FILE = "/Users/leen/Desktop/output.txt";

    public static final String VERSION = "232.9559.64";


    public static void main(String[] args) {
        try (var reader = new BufferedReader(new FileReader(INPUT_FILE));
             PrintStream out = new PrintStream(new FileOutputStream(OUTPUT_FILE))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                int dotIdx = line.lastIndexOf('.');
                if (dotIdx == -1) {
                    throw new RuntimeException("Invalid jar file name '" + line + "'");
                }
                String artifact = line.substring(0, dotIdx);
                out.println("<dependency>");
                out.println("<groupId>com.jetbrains.intellij.platform</groupId>");
                out.println("<artifactId>" + artifact + "</artifactId>");
                out.println("<version>" + VERSION + "</version>");
                out.println("</dependency>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
