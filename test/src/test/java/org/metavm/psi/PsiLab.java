package org.metavm.psi;

import com.intellij.core.JavaCoreApplicationEnvironment;
import com.intellij.core.JavaCoreProjectEnvironment;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.testFramework.LightVirtualFile;

import java.io.FileReader;
import java.io.IOException;

public class PsiLab {

    public static void main(String[] args) {

        var appEnv = new JavaCoreApplicationEnvironment(() -> {});
        var projectEnv = new JavaCoreProjectEnvironment(() -> {}, appEnv);
        var def = LanguageParserDefinitions.INSTANCE.forLanguage(JavaLanguage.INSTANCE);
        var psiManager = PsiManager.getInstance(projectEnv.getProject());
        var fvp = psiManager.findViewProvider(new LightVirtualFile("PsiFoo.java", readSource()));
        assert fvp != null;
        var file = (PsiJavaFile) def.createFile(fvp);

        var klass = (PsiExtensibleClass) file.getClasses()[0];
        var methods = klass.getOwnMethods();
        System.out.println(methods);
    }

    public static final String FILE = "/Users/leen/workspace/object/src/test/java/org.metavm/psi/PsiFoo.java";
    public static final char[] buf = new char[8*1024*1024];

    private static String readSource() {
        try(FileReader reader = new FileReader(FILE)) {
            int n = reader.read(buf);
            return new String(buf, 0, n);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
