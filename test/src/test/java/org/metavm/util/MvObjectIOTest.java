package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.core.ClassInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MvObjectIOTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        entityContextFactory = BootstrapUtils.bootstrap().entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() throws IOException, ClassNotFoundException {
        var ref = new Object() { byte[] bytes;};
        TestUtils.doInTransactionWithoutResult( () -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var foo = context.bind(new Foo("foo", new Bar("bar001")));
                context.initIds();
                var output = new MarkingInstanceOutput();
                var objOut = context.bind( MvObjectOutputStream.create(output, context));
                var objOutInst = (ClassInstance) context.getInstance(objOut);
                Flows.invoke(StdMethod.objectOutputStreamWriteObject.get(),
                        objOutInst,
                        List.of(context.getInstance(foo).getReference()),
                        context);
                Assert.assertTrue(output.size() > 0);
                ref.bytes = output.toByteArray();
                context.finish();
            }
        });
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var input = context.getInstanceContext().createInstanceInput(new ByteArrayInputStream(ref.bytes));
            var objInput = context.bind(new MvObjectInputStream(input, context));
            var objInputInst = (ClassInstance) context.getInstance(objInput);
            var readObjectMethod = StdMethod.objectInputStreamReadObject.get();
            var r = Objects.requireNonNull(Flows.invoke(readObjectMethod, objInputInst, List.of(), context));
            var inst = r.resolveObject();
            Assert.assertEquals(Instances.stringInstance("foo"), inst.getField("name"));
        }
    }

}