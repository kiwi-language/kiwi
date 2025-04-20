package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

public class FieldInitializerMoverTest extends TestCase {

    public void test() {
        var src = TestUtils.getResourcePath("tmp1/fieldinitializer/MoveFieldInitializerFoo.java");
        var file = TranspileTestTools.getPsiJavaFile(src);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new DefaultConstructorCreator());
            file.accept(new FieldInitializerSetter());
            file.accept(new FieldInitializerMover());
        });
        var dest = src.replace("/tmp1/fieldinitializer", "/tmp2/fieldinitializer2");
        Utils.writeFile(
                dest,
                file.getText().replace("fieldinitializer", "fieldinitializer2")
        );
    }

}