package tech.metavm.transpile2;

import junit.framework.TestCase;
import org.junit.Assert;

public class ImportEntitiesGeneratorTest extends TestCase {

    public void test() {
        var type = TranspileTestHelper.getCtClass(RecordWriter.class);
        TypeContext context = new TypeContext(type);
        context.extractReferences();
        var refs = ImportEntitiesGenerator.generate(context);
        Assert.assertFalse(refs.isEmpty());
    }

}