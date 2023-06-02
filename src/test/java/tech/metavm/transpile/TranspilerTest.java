//package tech.metavm.transpile;
//
//import junit.framework.TestCase;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.FileWriter;
//import java.io.IOException;
//
//public class TranspilerTest extends TestCase {
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(TranspilerTest.class);
//
////    public static final String SOURCE_FILE = "/Users/leen/workspace/object/src/test/java/tech/metavm/mocks/Foo.java";
//
//    public static final String SOURCE_FILE = "/Users/leen/workspace/object/src/main/java/tech/metavm/util/InstanceUtils.java";
//
//    public static final String OUT_FILE = "/Users/leen/workspace/object/src/test/resources/ts/Transpiled.ts";
//
//    public void test() throws IOException {
//        var transpiler = Transpiler.createFromFileName(SOURCE_FILE);
//        String tsSource = transpiler.transpile();
//        try(FileWriter out = new FileWriter(OUT_FILE)) {
//            out.write(tsSource);
//        }
//    }
//
//}