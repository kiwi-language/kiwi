package tech.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import junit.framework.TestCase;
import tech.metavm.autograph.mocks.ContinueFoo;

import java.io.IOException;

public class ContinueTransformerTest extends TestCase {

    public void test() throws IOException {
        var file = TranspileTestTools.getPsiJavaFile(ContinueFoo.class);
        var doc = FileDocumentManager.getInstance().getCachedDocument(file.getVirtualFile());
        var cmdProcessor = CommandProcessor.getInstance();
        cmdProcessor.executeCommand(
                null,
                () -> file.accept(new ContinueTransformer())
                , null, null, doc
        );
        System.out.println(file.getText());
    }

}