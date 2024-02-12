package tech.metavm;

import tech.metavm.autograph.LabVisitor;
import tech.metavm.autograph.TranspileTestTools;
import tech.metavm.autograph.mocks.RecordFoo;

import java.io.IOException;

public class Lab {

    public static void main(String[] args) throws IOException {
        var file = TranspileTestTools.getPsiJavaFile(RecordFoo.class);
        TranspileTestTools.executeCommand(() -> file.accept(new LabVisitor()));
    }

}
