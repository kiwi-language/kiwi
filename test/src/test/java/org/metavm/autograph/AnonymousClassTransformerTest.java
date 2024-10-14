package org.metavm.autograph;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

@Slf4j
public class AnonymousClassTransformerTest extends TestCase {

    public static final String SOURCE = "/Users/leen/workspace/object/lab/src/main/basics/anonymousclass/AnonymousClassFoo.java";
    public static final String TARGET = "/Users/leen/workspace/object/lab/src/main/tmp/anonymousclass2/AnonymousClassFoo.java";

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

    public void testLocalClass() {
        String src = "/Users/leen/workspace/object/lab/src/main/basics/localclass/LocalClassFoo.java";
        String target = "/Users/leen/workspace/object/lab/src/main/tmp/localclass2/LocalClassFoo.java";
        var file = TranspileTestTools.getPsiJavaFile(src);
        TranspileTestTools.executeCommand(() -> {
            file.accept(new DefaultConstructorCreator());
            file.accept(new SuperCallInserter());
            file.accept(new QnResolver());
            file.accept(new ActivityAnalyzer());
            file.accept(new AnonymousClassTransformer());
        });
        log.debug("{}", file.getText());
        NncUtils.writeFile(target, file.getText().replace("localclass", "localclass2"));
    }
}