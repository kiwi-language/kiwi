package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.autograph.mocks.VarargsFoo;

public class VarargsTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(VarargsFoo.class);
        TranspileTestTools.executeCommand(() -> file.accept(new VarargsTransformer()));
        System.out.println(file.getText());
    }

}