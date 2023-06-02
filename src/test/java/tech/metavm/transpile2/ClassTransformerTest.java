package tech.metavm.transpile2;

import junit.framework.TestCase;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.spoon.Foo;
import tech.metavm.util.NncUtils;

public class ClassTransformerTest extends TestCase {

    private Launcher launcher;

    @Override
    protected void setUp() throws Exception {
        launcher = TranspileTestHelper.getLauncher();
    }

    public void test() {
        launcher.addInputResource("/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/IFoo.java");
        launcher.addInputResource("/Users/leen/workspace/object/src/test/java/tech/metavm/spoon/Foo.java");
        var model = launcher.buildModel();
        var context = new TranspileContext(model, launcher.getFactory());

        var klass = (CtClass<?>) NncUtils.find(model.getAllTypes(), t -> t.getActualClass().equals(Foo.class));
        ClassTransformer.transform(klass, context);
        System.out.println(klass);
    }

    public void testDataConstructor() {
        var type = TranspileTestHelper.getCtClass(InstancePO.class);
        ClassTransformer.transform(type, new TranspileContext(type, type.getFactory()));
        System.out.println(type);

    }

}