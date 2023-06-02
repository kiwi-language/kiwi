package tech.metavm.transpile2;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import tech.metavm.util.InternalException;

import java.io.File;

public class TranspileTestHelper {

    public static final String TEST_SOURCE_DIR = "/Users/leen/workspace/object/src/test/java/";
    public static final String SOURCE_DIR = "/Users/leen/workspace/object/src/main/java/";

    public static Launcher getLauncher() {
        var l = new Launcher();
        l.getEnvironment().setComplianceLevel(19);
        l.getEnvironment().setPreviewFeaturesEnabled(true);
        return l;
    }

    public static CtClass<?> getCtClass(Class<?> klass) {
        return (CtClass<?>) getType(klass);
    }

    public static CtType<?> getType(Class<?> klass) {
        String subPath = klass.getName().replace('.', '/') + ".java";
        String path;
        if(pathExists(TEST_SOURCE_DIR, subPath)) {
            path = TEST_SOURCE_DIR + subPath;
        }
        else if(pathExists(SOURCE_DIR, subPath)) {
            path = SOURCE_DIR + subPath;
        }
        else {
            throw new InternalException("Can not find source for class " + klass.getName());
        }
        var l = getLauncher();
        l.addInputResource(path);
        var model = l.buildModel();
        return model.getAllTypes().iterator().next();
    }

    private static boolean pathExists(String dir, String fileName) {
        return new File(dir + fileName).exists();
    }

}
