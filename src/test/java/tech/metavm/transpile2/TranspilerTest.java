package tech.metavm.transpile2;

import junit.framework.TestCase;

import java.util.List;

public class TranspilerTest extends TestCase {

    public static final String OUTPUT_DIR = "/Users/leen/workspace/front/src/generated/";

    public static final String LAB_DIR = "/Users/leen/workspace/object/target/ts/";

    public static final String SMOKING_FILE =
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/entity/BaseEntityContext.java";
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/entity/IndexDef.java";
//                "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/TranspileFoo.java";
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/util/NncUtils.java";
//    "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/PatternLab.java";
//    "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/CollectionLab.java";
    "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/LambdaFoo.java";

    public static final List<String> LAB_FILES = List.of(
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/util/NncUtils.java",
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/Instance.java",
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/ReferenceRT.java"
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/IInstanceStore.java",
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/BaseInstanceStore.java",
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/InterfaceFieldLab.java",
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/IBar.java"
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/MockInstancePO.java",
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/persistence/InstancePO.java"
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/ClassInstance.java",
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/object/instance/Instance.java"
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/util/NncUtils.java",

//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/TranspileFoo.java",
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/ITranspileFoo.java",
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/TranspileFooBase.java",
            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/EnumFoo.java",
            "/Users/leen/workspace/object/src/main/java/tech/metavm/transpile2/builtin/Enum.java"

    );

    public static final String CLASS_FILE =
        "/Users/leen/workspace/object/src/main/java/tech/metavm/transpile2/WriterBase.java";
//    "/Users/leen/workspace/object/src/main/java/tech/metavm/util/NncUtils.java";

    public static final String ENUM_FILE =
//            "/Users/leen/workspace/object/src/test/java/tech/metavm/transpile2/MockEnum.java";
    "/Users/leen/workspace/object/src/main/java/tech/metavm/transpile2/TsKeyword.java";

    public static final String INTERFACE_FILE =
            "/Users/leen/workspace/object/src/test/java/tech/metavm/transpile2/MockInterface.java";

    public static final String RECORD_FILE =
            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/SpoonRecord.java";

    public static final String SWITCH_FILE =
            "/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/SwitchLab.java";


    public static final List<String> DIR_LIST = List.of(
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/transpile2/"
            "/Users/leen/workspace/object/src/main/java/tech/metavm/entity",
            "/Users/leen/workspace/object/src/main/java/tech/metavm/util",
            "/Users/leen/workspace/object/src/main/java/tech/metavm/object"
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/flow"
    );

    public void testSmoking() {
        newTranspiler(LAB_DIR, SMOKING_FILE).build();
    }

    public void testLab() {
        newTranspiler(LAB_DIR, LAB_FILES).build();
    }

    public void testClass() {
        newTranspiler(CLASS_FILE).build();
    }

    public void testEnum() {
        newTranspiler(ENUM_FILE).build();
    }

    public void testInterface() {
        newTranspiler(INTERFACE_FILE).build();
    }

    public void testRecord() {
        newTranspiler(RECORD_FILE).build();
    }

    public void testSwitchTransform() {
        newTranspiler(SWITCH_FILE).build();
    }

    public void testDir() {
        newTranspiler(DIR_LIST).build();
    }

    private Transpiler newTranspiler(String path) {
        return newTranspiler(OUTPUT_DIR, path);
    }

    private Transpiler newTranspiler(List<String> paths) {
        return newTranspiler(OUTPUT_DIR, paths);
    }

    private Transpiler newTranspiler(String outDir, String path) {
        return new Transpiler(outDir, path);
    }

    private Transpiler newTranspiler(String outDir, List<String> paths) {
        return new Transpiler(outDir, paths);
    }

}