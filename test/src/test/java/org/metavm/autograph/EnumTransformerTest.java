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
public class EnumTransformerTest extends TestCase {

    public static final List<String> SOURCES = List.of(
            TestUtils.getResourcePath("tmp1/enum_/EnumFoo.java"),
            TestUtils.getResourcePath("tmp1/enum_/EnumConstantImpl.java")

    );

    public void test() {
        var units = new ArrayList<Unit>();
        for (String source : SOURCES) {
            units.add(new Unit(TranspileTestTools.getPsiJavaFile(source),
                    source.replace("/tmp1/enum_/", "/tmp2/enum_2/")
            ));
        }
        units.forEach(u -> transform(u.file));
        units.forEach(u -> emit(u.file, u.dest));
    }

    private record Unit(PsiJavaFile file, String dest) {}

    private void executeStage(CompileStage stage, List<Unit> unites) {
        unites.forEach(file -> {
            if(stage.filter().test(file.file))
                TranspileTestTools.executeCommand(() -> stage.action().accept(file.file));
        });
    }

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> {
            file.accept(new EnumTransformer());
        });
    }

    private void emit(PsiJavaFile file, String dest) {
//        file.accept(new TypePrinter());
        TranspileTestTools.executeCommand(
                () -> {
//                    file.accept(new DiscardedElementRemover());
//                    file.accept(new TypePrinter());
                }
        );
        log.info("Emitting to file {}", dest);
        Utils.writeFile(
                new File(dest),
                file.getText().replace("enum_", "enum_2")
        );
    }

}