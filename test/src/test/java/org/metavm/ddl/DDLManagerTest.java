package org.metavm.ddl;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.entity.*;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Function;
import org.metavm.mocks.UpgradeBar;
import org.metavm.mocks.UpgradeFoo;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.*;
import org.metavm.task.GlobalPreUpgradeTask;
import org.metavm.task.PreUpgradeTask;
import org.metavm.util.BootstrapResult;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.metavm.util.Constants.ROOT_APP_ID;

public class DDLManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLManagerTest.class);

    private DDLManager ddlManager;
    private EntityContextFactory entityContextFactory;
    private BootstrapResult bootResult;

    @Override
    protected void setUp() throws Exception {
        bootResult = BootstrapUtils.bootstrap();
        var managers = TestUtils.createCommonManagers(bootResult);
        var typeManager = managers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        ddlManager = new DDLManager(entityContextFactory, typeManager);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        bootResult = null;
        entityContextFactory = null;
        ddlManager = null;
    }

    public void testPreUpgrade() {
        var fooKlass = ModelDefRegistry.getDefContext().getKlass(UpgradeFoo.class);
        var field = fooKlass.getFieldByCode("bar");
        Assert.assertEquals(1, field.getSince());
        var barKlass = ModelDefRegistry.getDefContext().getKlass(UpgradeBar.class);
        Assert.assertEquals(1, barKlass.getSince());
        var request = ddlManager.buildUpgradePreparationRequest(0);
//        Assert.assertEquals(1, request.fieldAdditions().size());
//        Assert.assertEquals(field.getName(), request.fieldAdditions().get(0).fieldName());
//        Assert.assertEquals(1, request.initializerKlasses().size());
//        Assert.assertEquals(fooKlass.getCodeNotNull() + "Initializer", request.initializerKlasses().get(0).code());
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);

        bootResult = BootstrapUtils.create(false, false, bootResult.allocatorStore(), bootResult.columnStore(), bootResult.typeTagStore(),
                Set.of(UpgradeBar.class, KlassFlags.class), Set.of(ReflectionUtils.getField(UpgradeFoo.class, "bar"), ReflectionUtils.getField(Klass.class, "flags")));
        entityContextFactory = bootResult.entityContextFactory();
        var typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        ddlManager = new DDLManager(entityContextFactory, typeManager);

        var fooId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var foo = new UpgradeFoo("foo", null);
                context.bind(foo);
                context.finish();
                return foo.getId();
            }
        });

        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        TestUtils.waitForTaskDone(t -> t instanceof GlobalPreUpgradeTask, entityContextFactory);
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);

        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        TestUtils.waitForTaskDone(t -> t instanceof GlobalPreUpgradeTask, entityContextFactory);
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);

        var bootstrap1 = new Bootstrap(entityContextFactory, new StdAllocators(bootResult.allocatorStore()),
                bootResult.columnStore(), bootResult.typeTagStore(), bootResult.stdIdStore());
        bootstrap1.boot();
        TestUtils.doInTransactionWithoutResult(() -> bootstrap1.save(false));
        var dc = ModelDefRegistry.getDefContext();
        var barKlass1 = dc.getKlass(UpgradeBar.class);
        Assert.assertEquals(barKlass.getId(), barKlass1.getId());
        Assert.assertTrue(dc.containsEntity(Object.class, barKlass.getId()));

        try(var context = newContext()) {
            var foo = context.getEntity(UpgradeFoo.class, fooId);
            Assert.assertEquals(new UpgradeBar("foo-bar"), foo.getBar());
        }
    }

    public void testPreUpgradeWithNewKlassField() {
        var klassKlass = ModelDefRegistry.getDefContext().getKlass(Klass.class);
        var field = klassKlass.getFieldByCode("flags");
        Assert.assertEquals(1, field.getSince());
        var klassFlagsKlass = ModelDefRegistry.getDefContext().getKlass(KlassFlags.class);
        Assert.assertEquals(1, klassFlagsKlass.getSince());
        var request = ddlManager.buildUpgradePreparationRequest(0);
        TestUtils.writeJson("/Users/leen/workspace/object/test.json", request);

        bootResult = BootstrapUtils.create(false, false, bootResult.allocatorStore(), bootResult.columnStore(), bootResult.typeTagStore(),
                Set.of(KlassFlags.class, UpgradeBar.class),
                Set.of(ReflectionUtils.getField(Klass.class, "flags"), ReflectionUtils.getField(UpgradeFoo.class, "bar")));
        entityContextFactory = bootResult.entityContextFactory();
        var typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        ddlManager = new DDLManager(entityContextFactory, typeManager);

        var fooKlassId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var fooKlass = TestUtils.newKlassBuilder("Foo").build();
                context.bind(fooKlass);
                context.finish();
                return fooKlass.getId();
            }
        });

        TestUtils.doInTransactionWithoutResult(() -> ddlManager.preUpgrade(request));
        TestUtils.waitForTaskDone(t -> t instanceof GlobalPreUpgradeTask, entityContextFactory);
        TestUtils.waitForTaskDone(t -> t instanceof PreUpgradeTask, entityContextFactory);

        var bootstrap1 = new Bootstrap(entityContextFactory, new StdAllocators(bootResult.allocatorStore()),
                bootResult.columnStore(), bootResult.typeTagStore(), bootResult.stdIdStore());
        bootstrap1.boot();
        TestUtils.doInTransactionWithoutResult(() -> bootstrap1.save(false));

        try(var context = newContext()) {
            var fooKlass = context.getEntity(Klass.class, fooKlassId);
            Assert.assertNotNull(fooKlass.getFlags());
            Assert.assertTrue(fooKlass.isFlag1());
        }
    }

    public void testCopyDefContext() {
        var request = ddlManager.buildUpgradePreparationRequest(0);
        var sysDefContext = ModelDefRegistry.getDefContext();
        var klassKlass = sysDefContext.getKlass(Klass.class);
        var klassKlassId = klassKlass.getId();
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
                new DefaultIdInitializer(bootResult.idProvider()), bridge, wal, null, null, false,
                builder -> builder.timeout(0L).typeDefProvider(sysDefContext)
        );
        var defContext = new ReversedDefContext(standardInstanceContext, sysDefContext);
        ModelDefRegistry.setLocalDefContext(defContext);
        bridge.setEntityContext(defContext);
        defContext.initializeFrom(sysDefContext, List.of());

        var klassKlass1 = defContext.getEntity(Klass.class, klassKlassId);
        Assert.assertNotNull(klassKlass1);
        Assert.assertEquals(klassKlass.getName(), klassKlass1.getName());
        Assert.assertNotSame(klassKlass, klassKlass1);
        for (Column column : ColumnKind.columns()) {
            var columnId = sysDefContext.getInstance(column).getId();
            Assert.assertTrue(defContext.containsEntity(Object.class, columnId));
        }
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

        try(var context1 = entityContextFactory.newContext(TestConstants.APP_ID, defContext)) {
            Assert.assertSame(context1.getDefContext(), defContext);
            Assert.assertSame(context1.getParent(), defContext);
            var quxKlass = context1.getKlass(quxKlassId);
            Assert.assertEquals("Qux", quxKlass.getName());
        }

        for (StdFunction stdFunc : StdFunction.values()) {
            var func = stdFunc.get();
            func.forEachDescendant(e -> {
                Assert.assertTrue(defContext.containsEntity(e));
                defContext.getInstance(e);
            });
        }
        defContext.close();
    }

    public void testWalWithKlassInst() {
        var sysDefContext = ModelDefRegistry.getDefContext();
        var klassKlassInst = sysDefContext.getInstance(sysDefContext.getKlass(Klass.class));
        var instancePOs = List.of(klassKlassInst.toPO(ROOT_APP_ID));
        var wal = new WAL(ROOT_APP_ID);
        wal.saveInstances(ChangeList.inserts(instancePOs));
        try(var dc = DefContextUtils.createReversedDefContext(wal, bootResult.entityContextFactory(), List.of())) {
            EntityUtils.ensureTreeInitialized(dc.getKlass(Klass.class));
        }
    }

    public void testCleanup() {
        var wal = new WAL(ROOT_APP_ID);
        try(var dc = DefContextUtils.createReversedDefContext(wal, bootResult.entityContextFactory(), List.of())) {
        }
        var defContext = ModelDefRegistry.getDefContext();
        MatcherAssert.assertThat(defContext, CoreMatchers.instanceOf(SystemDefContext.class));
        var vh = (HybridValueHolder<?>) StdField.enumName.getValueHolder();
        Assert.assertFalse(vh.isLocalPresent());
        Assert.assertTrue(defContext.containsEntity(StdField.enumName.get()));
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}