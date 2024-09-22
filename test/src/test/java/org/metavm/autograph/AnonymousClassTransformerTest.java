package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

@Slf4j
public class AnonymousClassTransformerTest extends TestCase {

    public static final String SOURCE = "/Users/leen/workspace/object/lab/src/main/basics/anonymousclass/AnonymousClassFoo.java";
    public static final String TARGET = "/Users/leen/workspace/object/lab/src/main/basics/anonymousclass2/AnonymousClassFoo.java";

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(SOURCE);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new AnonymousClassTransformer());
        });
        log.debug("{}", file.getText());
        NncUtils.writeFile(TARGET, file.getText().replace("anonymousclass", "anonymousclass2"));
    }

}