package tech.metavm;

import tech.metavm.util.ByteUtils;
import tech.metavm.util.TestUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Scanner;

public abstract class Lab {

    public static final String FILE = "/Users/leen/DeskTop/wad.txt";

    public static final String BYTES_FILE = "/Users/leen/workspace/object/src/test/resources/bytes/test";

    public static void main(String[] args) throws Exception {
        var buf = ByteBuffer.allocateDirect(1024);
    }

    private static void testBytes() {
        var bytes = TestUtils.readBytes(BYTES_FILE);
        TestUtils.printJSON(ByteUtils.convertToJSON(bytes, false));
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

