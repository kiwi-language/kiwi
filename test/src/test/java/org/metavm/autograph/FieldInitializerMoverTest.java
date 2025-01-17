package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.util.Utils;

public class FieldInitializerMoverTest extends TestCase {

    public void test() {
        var src = "/Users/leen/workspace/object/lab/src/main/tmp1/fieldinitializer/MoveFieldInitializerFoo.java";
        var file = TranspileTestTools.getPsiJavaFile(src);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new DefaultConstructorCreator());
            file.accept(new FieldInitializerSetter());
            file.accept(new FieldInitializerMover());
        });
        var dest = "/Users/leen/workspace/object/lab/src/main/tmp2/fieldinitializer2/MoveFieldInitializerFoo.java";
        Utils.writeFile(
                dest,
                file.getText().replace("fieldinitializer", "fieldinitializer2")
        );
    }

}