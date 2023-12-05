package tech.metavm;

import tech.metavm.util.BytesUtils;
import tech.metavm.util.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Lab {

    public static final String FILE = "/Users/leen/DeskTop/wad.txt";

    public static final String BYTES_FILE = "/Users/leen/workspace/object/src/test/resources/bytes/test";

    public static final DecimalFormat DF = new DecimalFormat("000000");

    public static void main(String[] args) throws Exception {

    }

    private static void testBytes() {
        var bytes = TestUtils.readBytes(BYTES_FILE);
        TestUtils.printJSON(BytesUtils.convertToJSON(bytes, false));
    }

    private static void calcTime() throws Exception {
        try(var input = new FileInputStream(FILE)) {
            Scanner scanner = new Scanner(input);
            long t = 0;
            while (scanner.hasNextLine()) {
                t += Integer.parseInt(scanner.nextLine());
            }
            System.out.println(t);
        }
    }

}

