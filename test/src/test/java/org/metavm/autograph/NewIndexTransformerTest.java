package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NewIndexTransformerTest extends TestCase {

    public static final List<String> SOURCES = List.of(
            "/Users/leen/workspace/object/lab/src/main/tmp1/index/IndexFoo.java"
    );

    public void test() {
        var units = new ArrayList<Unit>();
        for (String source : SOURCES) {
            units.add(new Unit(TranspileTestTools.getPsiJavaFile(source),
                    source.replace("/main/tmp1/index/", "/main/tmp2/index2/")
            ));
        }
        units.forEach(unit -> transform(unit.file));
        units.forEach(u -> emit(u.file, u.dest));
    }

    private record Unit(PsiJavaFile file, String dest) {}

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> {
            file.accept(new NewIndexTransformer());
        });
    }

    private void emit(PsiJavaFile file, String dest) {
        log.debug("Emitting to file {}", dest);
        NncUtils.writeFile(
                new File(dest),
                file.getText().replace("index", "index2")
        );
    }

}