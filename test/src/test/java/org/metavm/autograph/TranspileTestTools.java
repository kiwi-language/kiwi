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
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileTestTools {

    public static final Logger logger = LoggerFactory.getLogger(TranspileTestTools.class);

    private static final String BASE_MOD = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home/jmods/java.base.jmod";
    private static final IrCoreApplicationEnvironment APP_ENV;
    private static final IrCoreProjectEnvironment PROJECT_ENV;
    private static final Project PROJECT;
    private static final LightVirtualFileBase.MyVirtualFileSystem FILE_SYSTEM = LightVirtualFileBase.ourFileSystem;

    static {
        APP_ENV = new IrCoreApplicationEnvironment(() -> {
        });
        PROJECT_ENV = new IrCoreProjectEnvironment(() -> {
        }, APP_ENV);
        var javaBaseDir = APP_ENV.getJarFileSystem().findFileByPath(BASE_MOD + "!/classes");
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
