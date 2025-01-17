package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExplicitTypeWideningTransformerTest extends TestCase {

    public static final List<String> SOURCES = List.of(
            "/Users/leen/workspace/object/lab/src/main/tmp1/conversion/ConversionFoo.java",
            "/Users/leen/workspace/object/lab/src/main/tmp1/conversion/Currency.java"
    );

    public void test() {
        var units = new ArrayList<Unit>();
        for (String source : SOURCES) {
            units.add(new Unit(TranspileTestTools.getPsiJavaFile(source),
                    source.replace("/main/tmp1/conversion/", "/main/tmp2/conversion2/")
            ));
        }
        units.forEach(u -> transform(u.file));
        units.forEach(u -> emit(u.file, u.dest));
    }

    private record Unit(PsiJavaFile file, String dest) {}

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> {
            file.accept(new ExplicitTypeWideningTransformer());
        });
    }

    private void emit(PsiJavaFile file, String dest) {
        log.info("Emitting to file {}", dest);
        Utils.writeFile(
                new File(dest),
                file.getText().replace("conversion", "conversion2")
        );
    }


}