package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MethodReferenceTransformerTest extends TestCase {

    public static final List<String> SOURCES = List.of(
            TestUtils.getResourcePath("tmp1/method_ref/MethodRefFoo.java")
    );

    public void test() {
        var units = new ArrayList<Unit>();
        for (String source : SOURCES) {
            units.add(new Unit(TranspileTestTools.getPsiJavaFile(source),
                    source.replace("/tmp1/method_ref/", "/tmp2/method_ref2/")
            ));
        }
        for (Unit unit : units) {
            transform(unit.file);
        }
        units.forEach(u -> emit(u.file, u.dest));
    }

    private record Unit(PsiJavaFile file, String dest) {}

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> file.accept(new MethodReferenceTransformer()));
    }

    private void emit(PsiJavaFile file, String dest) {
        log.info("Emitting to file {}", dest);
        Utils.writeFile(
                new File(dest),
                file.getText().replace("method_ref", "method_ref2")
        );
    }



}