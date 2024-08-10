package org.metavm.ddl;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Function;
import org.metavm.mocks.UpgradeBar;
import org.metavm.mocks.UpgradeFoo;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ClassKind;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassBuilder;
import org.metavm.task.GlobalPreUpgradeTask;
import org.metavm.task.PreUpgradeTask;
import org.metavm.util.BootstrapResult;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.metavm.util.Constants.ROOT_APP_ID;

public class DDLManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLManagerTest.class);

    private DDLManager ddlManager;
    private EntityContextFactory entityContextFactory;
    private BootstrapResult bootResult;
    private EntityIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        var typeManager = managers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        ddlManager = new DDLManager(entityContextFactory, typeManager);
        idProvider = bootResult.idProvider();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        bootResult = null;
        entityContextFactory = null;
        ddlManager = null;
        idProvider = null;
    }

    public void testPreUpgrade() {
        var fooKlass = ModelDefRegistry.getDefContext().getKlass(UpgradeFoo.class);
        var field = fooKlass.getFieldByCode("bar");
        Assert.assertEquals(1, field.getSince());
        var barKlass = ModelDefRegistry.getDefContext().getKlass(UpgradeBar.class);
        var klassKlass = ModelDefRegistry.getDefContext().getKlass(Klass.class);
        Assert.assertEquals(1, barKlass.getSince());
        var request = ddlManager.buildUpgradePreparationRequest(0);
        Assert.assertEquals(1, request.fieldAdditions().size());
        Assert.assertEquals(field.getName(), request.fieldAdditions().get(0).fieldName());
        Assert.assertEquals(1, request.initializerKlasses().size());
        Assert.assertEquals(fooKlass.getCodeNotNull() + "Initializer", request.initializerKlasses().get(0).code());
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);
        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        var fooId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var foo = new UpgradeFoo("foo", new UpgradeBar("<missing>"));
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });
        var defContext = (SystemDefContext) ModelDefRegistry.getDefContext();
        defContext.evict(barKlass);
        TestUtils.waitForTaskDone(t -> t instanceof GlobalPreUpgradeTask, entityContextFactory);
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);
        defContext.putBack(barKlass);
        try(var context = newContext()) {
            var foo = context.getEntity(UpgradeFoo.class, fooId);
            Assert.assertEquals(new UpgradeBar("foo-bar"), foo.getBar());
        }
        // Rerun the pre-upgrade task
        defContext.evict(barKlass);
        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);
    }

    public void testCopyDefContext() {
        var request = ddlManager.buildUpgradePreparationRequest(0);
        var sysDefContext = ModelDefRegistry.getDefContext();
        var klassKlass = sysDefContext.getKlass(Klass.class);
        var klassKlassId = klassKlass.getId();
//        var barKlass = sysDefContext.getKlass(UpgradeBar.class);
//        var barKlassId = barKlass.getId();
        DebugEnv.klass = sysDefContext.getKlass(Klass.class);
        var wal = new WAL(ROOT_APP_ID);
        wal.setData(request.walContent());
        var quxKlassId = TestUtils.doInTransaction(() -> {
            try (var context = newContext()) {
                var quxKlass = KlassBuilder.newBuilder("Qux", "Qux").build();
                context.bind(quxKlass);
                context.finish();
                return quxKlass.getId();
            }
        });
        logger.debug("QuxKlass ID: {}", quxKlassId);

        var bridge = new EntityInstanceContextBridge();
        var standardInstanceContext = (InstanceContext) entityContextFactory.newBridgedInstanceContext(
                ROOT_APP_ID, false, null, null,
                new DefaultIdInitializer(idProvider), bridge, wal, null, null, false,
                builder -> builder.timeout(0L).typeDefProvider(sysDefContext)
        );
        var defContext = new ReversedDefContext(standardInstanceContext, sysDefContext);
        bridge.setEntityContext(defContext);
        defContext.initializeFrom(sysDefContext);

        var klassKlass1 = defContext.getEntity(Klass.class, klassKlassId);
        Assert.assertNotNull(klassKlass1);
        Assert.assertEquals(klassKlass.getName(), klassKlass1.getName());
        Assert.assertNotSame(klassKlass, klassKlass1);

        var ec = defContext.getInstance(ClassKind.CLASS);
        var kInst = (ClassInstance) defContext.getInstanceContext().get(klassKlassId);
        var k = defContext.getKlass(Klass.class);
        Assert.assertSame(k, kInst.getKlass());
        Assert.assertSame(defContext.getInstanceContext().get(ec.getId()), ec);

        defContext.getIndexConstraint(Klass.UNIQUE_CODE);

        var setSourceFunc = defContext.selectFirstByKey(Function.UNIQUE_IDX_CODE, StdFunction.setSource.getName());
        Assert.assertNotNull(setSourceFunc);
        Assert.assertEquals(2, setSourceFunc.getParameters().size());

        Assert.assertSame(
                defContext.getKlass(Klass.class),
                defContext.selectFirstByKey(Klass.UNIQUE_CODE, klassKlass.getCodeNotNull()));

        entityContextFactory.setDefContext(defContext);
        try(var context1 = entityContextFactory.newContext()) {
            var quxKlass = context1.getKlass(quxKlassId);
            Assert.assertEquals("Qux", quxKlass.getName());
        }
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}