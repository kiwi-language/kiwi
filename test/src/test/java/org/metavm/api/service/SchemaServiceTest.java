package org.metavm.api.service;

import junit.framework.TestCase;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.io.File;

public class SchemaServiceTest extends TestCase  {
    public static final String SRC_DIR = "kiwi" + File.separator;

    private SchemaService schemaService;
    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var commonManagers = TestUtils.createCommonManagers(bootResult);
        schemaService = new SchemaService(bootResult.entityContextFactory());
        typeManager = commonManagers.typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();

    }

    @Override
    protected void tearDown() throws Exception {
        schemaService = null;
        typeManager = null;
        schedulerAndWorker = null;
    }

    public void test() {
        deploy("display/display.kiwi");
        var scheme = schemaService.getSchema(TestConstants.APP_ID);
        assertEquals(2, scheme.classes().size());
        var productCls = Utils.findRequired(scheme.classes(), c -> c.name().equals("Product"));
        assertNull(productCls.beanName());
        assertEquals("Product", productCls.label());
        var fields = productCls.fields();
        assertTrue(fields.getFirst().summary());
        assertEquals("Product Name", fields.getFirst().label());
        assertEquals("Product Price", fields.get(1).label());
        assertEquals("Product Stock", fields.get(2).label());

        var initParams = productCls.constructor().parameters();
        assertEquals("Product Name", initParams.getFirst().label());
        assertEquals("Product Price", initParams.get(1).label());
        assertEquals("Product Stock", initParams.get(2).label());

        var method = productCls.methods().getFirst();
        assertEquals("Remove Product Stock", method.label());
        var methodParams = method.parameters();
        assertEquals("Removed Quantity", methodParams.getFirst().label());

        var categoryCls = Utils.findRequired(scheme.classes(), c -> c.name().equals("Category"));
        var enumConsts = categoryCls.enumConstants();
        assertEquals("Electronics", enumConsts.getFirst().label());
        assertEquals("Clothing", enumConsts.get(1).label());
        assertEquals("Other", enumConsts.get(2).label());
    }

    private void deploy(String source) {
        MockUtils.assemble(SRC_DIR + source, typeManager, true, false, schedulerAndWorker);
    }

}