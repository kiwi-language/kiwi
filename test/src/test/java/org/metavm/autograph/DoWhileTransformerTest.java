package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.util.NncUtils;


public class DoWhileTransformerTest extends TestCase {

    public void test() {
        var source = "/Users/leen/workspace/object/lab/src/main/basics/dowhile/DoWhileFoo.java";
        var file = TranspileTestTools.getPsiJavaFile(source);
        TranspileTestTools.executeCommand(() -> file.accept(new DoWhileTransformer()));
        var output = "/Users/leen/workspace/object/lab/src/main/tmp/dowhile2/DoWhileFoo.java";
        NncUtils.writeFile(output, file.getText().replace("package dowhile;", "package dowhile2;"));
    }

}