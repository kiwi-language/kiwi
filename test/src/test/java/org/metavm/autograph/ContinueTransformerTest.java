package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.ContinueFoo;
import org.metavm.util.NncUtils;

import java.io.IOException;

public class ContinueTransformerTest extends TestCase {

    public void test() throws IOException {
        var file = TranspileTestTools.getPsiJavaFile(ContinueFoo.class);
        TranspileTestTools.executeCommand(
                () -> {
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new ForTransformer());
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new BreakTransformer());
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new ContinueTransformer());
                }
        );
        System.out.println(file.getText());
//        var dest = "/Users/leen/workspace/object/lab/src/main/tmp/continue2/ContinueFoo.java";
//        NncUtils.writeFile(dest,
//                file.getText().replace("package org.metavm.autograph.mocks;", "package continue2;"));
    }


}