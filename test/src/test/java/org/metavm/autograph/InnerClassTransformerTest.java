package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InnerClassTransformerTest extends TestCase {

    public static final List<String> SOURCES = List.of(
            "/Users/leen/workspace/object/lab/src/main/tmp1/innerclass/InnerClassFoo.java",
            "/Users/leen/workspace/object/lab/src/main/tmp1/innerclass/Warehouse.java",
            "/Users/leen/workspace/object/lab/src/main/tmp1/innerclass/service/WarehouseService.java"
    );

    public void test() {
        var units = new ArrayList<Unit>();
        for (String source : SOURCES) {
            units.add(new Unit(TranspileTestTools.getPsiJavaFile(source),
                    source.replace("/main/tmp1/innerclass/", "/main/tmp2/innerclass2/")
            ));
        }
        Compiler.prepareStages.forEach(stage -> executeStage(stage, units));
        units.forEach(u -> emit(u.file, u.dest));
    }

    private record Unit(PsiJavaFile file, String dest) {}

    private void prepare(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> file.accept(new InnerClassCopier()));
    }

    private void executeStage(CompileStage stage, List<Unit> unites) {
        unites.forEach(file -> {
            if(stage.filter().test(file.file))
                TranspileTestTools.executeCommand(() -> stage.action().accept(file.file));
        });
    }

    private void transform(PsiJavaFile file) {
        TranspileTestTools.executeCommand(() -> {
            file.accept(new InnerClassQualifier());
            file.accept(new InnerClassTransformer());
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
                file.getText().replace("innerclass", "innerclass2")
        );
    }

}