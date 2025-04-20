package org.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.metavm.autograph.env.IrCoreApplicationEnvironment;
import org.metavm.autograph.env.IrCoreProjectEnvironment;
import org.metavm.autograph.env.LightVirtualFileBase;
import org.metavm.util.InternalException;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileTestTools {

    public static final Logger logger = LoggerFactory.getLogger(TranspileTestTools.class);

//    private static final String BASE_MOD = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home/jmods/java.base.jmod";
    private static final String JAVA_BASE_JMOD_PATH; // Will be determined dynamically

    private static final IrCoreApplicationEnvironment APP_ENV;
    private static final IrCoreProjectEnvironment PROJECT_ENV;
    private static final Project PROJECT;
    private static final LightVirtualFileBase.MyVirtualFileSystem FILE_SYSTEM = LightVirtualFileBase.ourFileSystem;

    static {
        JAVA_BASE_JMOD_PATH = findJavaBaseJmodPath();
        APP_ENV = new IrCoreApplicationEnvironment(() -> {
        });
        PROJECT_ENV = new IrCoreProjectEnvironment(() -> {
        }, APP_ENV);
//        var javaBaseDir = APP_ENV.getJarFileSystem().findFileByPath(BASE_MOD + "!/classes");
        var javaBaseDir = APP_ENV.getJarFileSystem().findFileByPath(JAVA_BASE_JMOD_PATH + "!/classes");
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(javaBaseDir));
        var targetRoot = TestUtils.getResourcePath("");
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(targetRoot)));
//        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(API_SOURCE_ROOT)));
//        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(SOURCE_ROOT)));
//        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(TEST_SOURCE_ROOT)));
//        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(TMP_SOURCE_ROOT)));
        PROJECT = PROJECT_ENV.getProject();
        initTranspilerUtils();
    }

    /**
     * Dynamically finds the path to the java.base.jmod file.
     * Prioritizes JAVA_HOME environment variable, then falls back to java.home system property.
     *
     * @return The absolute path to java.base.jmod.
     * @throws IllegalStateException if the jmod file cannot be found.
     */
    private static String findJavaBaseJmodPath() {
        Path jmodPath = null;

        // 1. Try JAVA_HOME environment variable
        String javaHomeEnv = System.getenv("JAVA_HOME");
        if (javaHomeEnv != null && !javaHomeEnv.isEmpty()) {
            Path potentialPath = Paths.get(javaHomeEnv, "jmods", "java.base.jmod");
            if (Files.isRegularFile(potentialPath)) {
                jmodPath = potentialPath;
                logger.debug("Found java.base.jmod using JAVA_HOME: {}", jmodPath);
            } else {
                logger.warn("JAVA_HOME is set ('{}'), but '{}' not found or not a file.", javaHomeEnv, potentialPath);
            }
        } else {
            logger.debug("JAVA_HOME environment variable not set. Trying java.home system property.");
        }

        // 2. If not found, try java.home system property
        if (jmodPath == null) {
            String javaHomeProp = System.getProperty("java.home");
            if (javaHomeProp != null && !javaHomeProp.isEmpty()) {
                Path javaHomePath = Paths.get(javaHomeProp);

                // Check <java.home>/jmods/java.base.jmod (e.g., JDK path is java.home)
                Path potentialPath1 = javaHomePath.resolve("jmods").resolve("java.base.jmod");
                if (Files.isRegularFile(potentialPath1)) {
                    jmodPath = potentialPath1;
                    logger.debug("Found java.base.jmod using java.home directly: {}", jmodPath);
                } else {
                    // Check <java.home>/../jmods/java.base.jmod (e.g., java.home is JRE path inside JDK)
                    Path parentPath = javaHomePath.getParent();
                    if (parentPath != null) {
                        Path potentialPath2 = parentPath.resolve("jmods").resolve("java.base.jmod");
                        if (Files.isRegularFile(potentialPath2)) {
                            jmodPath = potentialPath2;
                            logger.debug("Found java.base.jmod using parent of java.home: {}", jmodPath);
                        }
                    }
                }

                if (jmodPath == null) {
                    logger.warn("Checked java.home ('{}') and its parent, but failed to find java.base.jmod.", javaHomeProp);
                }
            } else {
                logger.warn("java.home system property is not set or empty.");
            }
        }

        // 3. Error out if not found
        if (jmodPath == null) {
            throw new IllegalStateException(
                    "Could not find java.base.jmod. " +
                            "Please ensure a valid JDK is available and either the JAVA_HOME environment variable " +
                            "is set correctly, or the test execution environment's 'java.home' property allows locating the 'jmods' directory."
            );
        }

        return jmodPath.toAbsolutePath().toString();
    }


    public static void initTranspilerUtils() {
        TranspileUtils.init(PROJECT.getService(PsiElementFactory.class), PROJECT);
    }

    public static PsiJavaFile findFile(PsiDirectory directory, Class<?> klass) {
        return (PsiJavaFile) Objects.requireNonNull(directory.findFile(klass.getSimpleName() + ".java"));
    }

    public static PsiJavaFile getPsiJavaFileByName(String className) {
        return getPsiJavaFileByName(className, false);
    }

    public static PsiClass getPsiClass(String className) {
        var file = getPsiJavaFileByName(className);
        return Utils.findRequired(
                file.getClasses(),
                k -> Objects.equals(k.getQualifiedName(), className)
        );
    }

    public static PsiJavaFile getPsiJavaFileByName(String className, boolean writable) {
        return getPsiJavaFile(getClassFilePath(className), writable);
    }

    private static String getClassFilePath(String className) {
        return TestUtils.getResourcePath(className.replace('.', '/') + ".java");
    }

    public static void executeCommand(Runnable command) {
        CommandProcessor.getInstance().executeCommand(
                getProject(),
                () -> {
                    try {
                        command.run();
                    } catch (Throwable e) {
                        logger.error("fail to execute command", e);
                        if (e instanceof RuntimeException runtimeException) {
                            throw runtimeException;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                },
                null,
                null
        );
    }

    public static PsiJavaFile getPsiJavaFile(String path) {
        return getPsiJavaFile(path, false);
    }

    public static PsiJavaFile getPsiJavaFile(String path, boolean writable) {
        var file = Objects.requireNonNull(FILE_SYSTEM.findFileByPath(path));
        try {
            if (writable) file.setWritable(true);
            return (PsiJavaFile) PsiManager.getInstance(PROJECT).findFile(file);
        } catch (IOException e) {
            throw new InternalException("Fail to set writable of file " + path, e);
        }
    }

    public static Project getProject() {
        return PROJECT;
    }

    public static void touch() {
        initTranspilerUtils();
    }

}
