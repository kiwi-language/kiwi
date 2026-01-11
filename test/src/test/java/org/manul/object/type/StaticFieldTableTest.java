package org.manul.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.entity.EntityContextFactory;
import org.manul.util.BootstrapUtils;
import org.manul.util.TestConstants;
import org.manul.util.TestUtils;

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