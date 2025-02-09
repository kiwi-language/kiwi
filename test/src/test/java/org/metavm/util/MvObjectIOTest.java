package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.mocks.Bar;
import org.metavm.mocks.Foo;

import java.io.ByteArrayInputStream;
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
                var foo = new Foo(context.allocateRootId(), "foo", null);
                foo.setBar(new Bar(context.allocateRootId(), foo, "bar001"));
                context.bind(foo);
                context.initIds();
                var output = new MarkingInstanceOutput();
                var objOut = context.bind(MvObjectOutputStream.create(output));
                Flows.invoke(StdMethod.objectOutputStreamWriteObject.get().getRef(),
                        objOut,
                        List.of(foo.getReference()),
                        context);
                Assert.assertTrue(output.size() > 0);
                ref.bytes = output.toByteArray();
                context.finish();
            }
        });
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var input = context.createInstanceInput(new ByteArrayInputStream(ref.bytes));
            var objInput = context.bind(new MvObjectInputStream(input));
            var readObjectMethod = StdMethod.objectInputStreamReadObject.get();
            var r = Objects.requireNonNull(Flows.invoke(readObjectMethod.getRef(), objInput, List.of(), context));
            var inst = (Foo) r.resolveObject();
            Assert.assertEquals("foo", inst.getName());
        }
    }

}