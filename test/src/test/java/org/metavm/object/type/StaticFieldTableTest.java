package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

public class StaticFieldTableTest extends TestCase {

    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
    }

    public void test() {
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.bind(TestUtils.newKlassBuilder("Foo").build());
            var sft = StaticFieldTable.getInstance(klass.getInstanceType(), context);
            var sft1 = StaticFieldTable.getInstance(klass.getInstanceType(), context);
            Assert.assertSame(sft, sft1);
        }
    }

}