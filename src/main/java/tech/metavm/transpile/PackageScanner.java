package tech.metavm.transpile;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class PackageScanner {

    public static final String SOURCE_PACKAGE_PREFIX = "tech.metavm.";

    public static final String DEFAULT_SRC_ROOT = "/Users/leen/workspace/object/target/classes";
    public static final String FILE_SUFFIX = ".class";

    private final String sourceRoot;

    public PackageScanner() {
        this(DEFAULT_SRC_ROOT);
    }

    public PackageScanner(String sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public List<String> getClassNames(String packageName) {
        if(packageName.startsWith(SOURCE_PACKAGE_PREFIX)) {
            String packagePath = sourceRoot + "/" + packageName.replaceAll("\\.", "/");
            File dir = new File(packagePath);
            List<String> classNames = new ArrayList<>();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    var fileName = file.getName();
                    if (!fileName.contains("$") && fileName.endsWith(FILE_SUFFIX)) {
                        String className = packageName + "." + fileName.substring(0, fileName.length() - FILE_SUFFIX.length());
//                        Class<?> klass = ReflectUtils.classForName(className);
//                        if (!Annotation.class.isAssignableFrom(klass)) {
                    classNames.add(className);
//                        }
                    }
                }
            }
            return classNames;
        }
        else {
            return scanLibrary(packageName);
        }
    }

    private List<String> scanLibrary(String packageName) {
        var reflections = new Reflections(packageName, new SubTypesScanner(false));
        return NncUtils.filterAndMap(
                reflections.getSubTypesOf(Object.class),
                c -> !(Annotation.class.isAssignableFrom(c)),
                Class::getName
        );
    }

}
