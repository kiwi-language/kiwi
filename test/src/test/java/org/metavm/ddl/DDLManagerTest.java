package org.metavm.ddl;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.mocks.UpgradeFoo;
import org.metavm.task.GlobalPreUpgradeTask;
import org.metavm.task.PreUpgradeTask;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.ContextUtil;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDLManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLManagerTest.class);

    private DDLManager ddlManager;
    private EntityContextFactory entityContextFactory;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        var typeManager = managers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        ddlManager = new DDLManager(entityContextFactory, typeManager);
        ContextUtil.setAppId(TestConstants.APP_ID);

    }

    public void testPreUpgrade() {
        var fooKlass = ModelDefRegistry.getDefContext().getKlass(UpgradeFoo.class);
        var codeField = fooKlass.getFieldByCode("code");
        Assert.assertEquals(1, codeField.getSince());
        var request = ddlManager.buildUpgradePreparationRequest(0);
        Assert.assertEquals(1, request.fieldAdditions().size());
        Assert.assertEquals(codeField.getName(), request.fieldAdditions().get(0).fieldName());
        Assert.assertEquals(1, request.initializerKlasses().size());
        Assert.assertEquals(fooKlass.getCodeNotNull() + "Initializer", request.initializerKlasses().get(0).code());
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);
        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        var fooId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var foo = new UpgradeFoo("foo", "<missing>");
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });
        TestUtils.waitForTaskDone(t -> t instanceof GlobalPreUpgradeTask, entityContextFactory);
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);
        try(var context = newContext()) {
            var foo = context.getEntity(UpgradeFoo.class, fooId);
            Assert.assertEquals("foo", foo.getCode());
        }
        // Rerun the pre-upgrade task
        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}