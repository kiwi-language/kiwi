package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.autograph.mocks.ContinueFoo;

import java.io.IOException;

public class ContinueTransformerTest extends TestCase {

    public void test() throws IOException {
        var file = TranspileTestTools.getPsiJavaFile(ContinueFoo.class);
        TranspileTestTools.executeCommand(
                () -> {
                    file.accept(new QnResolver());
                    file.accept(new ActivityAnalyzer());
                    file.accept(new ContinueTransformer());
                }
        );
        System.out.println(file.getText());
    }


}