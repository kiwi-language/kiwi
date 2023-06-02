package tech.metavm.transpile2;

import junit.framework.TestCase;
import org.junit.Assert;
import spoon.Launcher;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.factory.Factory;

public class RecordTransformerTest extends TestCase {

    public static final String FILE =
            "/Users/leen/workspace/object/src/test/java/tech/metavm/transpile2/MockRecord.java";

    public static final String OUT_FILE = "/Users/leen/workspace/object/target/ts/Lab.ts";

    private CtRecord record;
    private Factory factory;

    @Override
    protected void setUp() throws Exception {
        Launcher launcher = TranspileTestHelper.getLauncher();
        launcher.addInputResource(FILE);
        factory = launcher.getFactory();
        var model = launcher.buildModel();
        record = (CtRecord) model.getAllTypes().iterator().next();
    }

    public void test() {
        var klass = RecordTransformer.transform(record, new TranspileContext(record, factory));

        var method = klass.getMethod("value");
        Assert.assertNotNull(method);

        TokenOutput out = new FileTokenOutput(OUT_FILE);
        ClassWriter cw = new ClassWriter(out, new TranspileContext(klass, factory));
        cw.write(klass);
        out.close();
    }

}