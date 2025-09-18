package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.application.Application;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.MetaContextCache;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.metavm.util.TestConstants.APP_ID;

public class TypeManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TypeManagerTest.class);

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;
    private MetaContextCache metaContextCache;
    private Id userId;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        metaContextCache = bootResult.metaContextCache();
        apiClient = new ApiClient(new ApiService(entityContextFactory, metaContextCache,
                new InstanceQueryService(bootResult.instanceSearchService())));
        userId = bootResult.userId();
        ContextUtil.setAppId(APP_ID);
    }

    @Override
    protected void tearDown() {
        typeManager = null;
        entityContextFactory = null;
        schedulerAndWorker = null;
        apiClient = null;
        metaContextCache = null;
    }

    public void testShopping() {
        MockUtils.assemble("kiwi/shopping.kiwi", typeManager, schedulerAndWorker);
        try (var context = entityContextFactory.newContext(APP_ID)) {
            var productCls = context.getKlassByQualifiedName("Product");
            Assert.assertEquals(3, productCls.getFields().size());
            var orderStatusCls = context.getKlassByQualifiedName("OrderStatus");
            Assert.assertEquals(3, orderStatusCls.getEnumConstants().size());
        }
    }

    public void testSynchronizeSearch() {
        TestUtils.doInTransaction(() -> {
            try(var context = entityContextFactory.newContext(APP_ID)) {
                var fooKlass = TestUtils.newKlassBuilder("SynchronizeFoo").searchable(true).build();
                context.bind(fooKlass);
                context.finish();
                Assert.assertTrue(fooKlass.isSearchable());
                return fooKlass.getId();
            }
        });
//        TestUtils.waitForTaskDone(t -> t instanceof SynchronizeSearchTask, entityContextFactory);
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        try (var context = entityContextFactory.newContext(APP_ID)) {
            var k = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance("SynchronizeFoo"));
            Assert.assertNotNull(k);
        }
    }

    public void testDeployWithAppId() {
        ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
        ContextUtil.setUserId(userId);
        TestUtils.doInTransaction(() ->
            typeManager.deploy(APP_ID, false, MockUtils.compile("kiwi/simple_shopping.kiwi"))
        );
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
        try (var context = entityContextFactory.newContext(APP_ID)) {
            var klasses = context.loadKlasses();
            assertEquals(1, klasses.size());
            assertEquals("Product", klasses.getFirst().getName());
        }
    }

    public void testHashMap() {
        var defContext = ModelDefRegistry.getDefContext();
        var klass = defContext.getKlass(HashMap.class);
        Assert.assertEquals(HashMap.class.getName(), klass.getQualifiedName());
        Assert.assertEquals(HashSet.class.getName(), defContext.getKlass(HashSet.class).getQualifiedName());
    }

    public void testChangeStaticFields() {
        MockUtils.assemble("kiwi/static_fields.kiwi", typeManager, schedulerAndWorker);
        TestUtils.doInTransaction(() -> apiClient.callMethod("UpdateStaticFoo", "set", List.of(2)));
        metaContextCache.invalidate(APP_ID, false);
        var value = TestUtils.doInTransaction(() -> apiClient.callMethod("UpdateStaticFoo", "get", List.of()));
        Assert.assertEquals(2, value);

        var opt1Id = typeManager.getEnumConstantId("Option", "opt1");
        TestUtils.doInTransaction(() -> apiClient.callMethod(opt1Id, "setValue", List.of(1)));
        metaContextCache.invalidate(APP_ID, false);
        var optValue = TestUtils.doInTransaction(() -> apiClient.callMethod(opt1Id, "getValue", List.of()));
        Assert.assertEquals(1, optValue);
    }

//    public void testRemoveField() {
//        var fieldId = TestUtils.doInTransaction(() -> {
//            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
//                var klass = context.bind(TestUtils.newKlassBuilder("Foo").build());
//                var field = FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();
//                context.finish();
//                return field.getId();
//            }
//        });
//        TestUtils.doInTransactionWithoutResult(() -> {
//            try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
//                var field = context.getField(fieldId);
//                var klass = field.getDeclaringType();
//                context.remove(field);
//                klass.removeField(field);
//                Assert.assertEquals(0, klass.getFields().size());
//                Assert.assertEquals(0, klass.getIndices().size());
//                context.finish();
//            }
//        });
//
//    }

    public void testGetSourceTag() {
        ContextUtil.setUserId(userId);
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(APP_ID)) {
                var klass = TestUtils.newKlassBuilder("Foo").sourceTag(1).build();
                FieldBuilder.newBuilder("name", klass, Types.getStringType()).sourceTag(1).build();
                context.bind(klass);
                context.finish();
            }
        });
        ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
        Assert.assertEquals(Integer.valueOf(1), typeManager.getSourceTag(APP_ID, "Foo"));
        Assert.assertEquals(Integer.valueOf(1), typeManager.getSourceTag(APP_ID, "Foo.name"));
        try {
            typeManager.getSourceTag(APP_ID, "NotExist");
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.INVALID_ELEMENT_NAME, e.getErrorCode());
        }
        try {
            typeManager.getSourceTag(APP_ID, "Foo.notExist");
            Assert.fail();
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.INVALID_ELEMENT_NAME, e.getErrorCode());
        }
    }

    public void testEntityRefcount() {
        MockUtils.assemble("kiwi/del/del.kiwi", typeManager, schedulerAndWorker);
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(APP_ID)) {
                var barKlass = context.getKlassByQualifiedName("del.Bar");
                context.remove(barKlass);
                context.finish();
                fail("Removal should have failed");
            }
            catch (BusinessException e) {
                assertSame(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
            }
        });
    }

    public void testAppStatusCheck() {
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var platformCtx = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                var app = platformCtx.getEntity(Application.class, PhysicalId.of(APP_ID, 0));
                app.deactivate();
                platformCtx.finish();
            }
        });
        try {
            MockUtils.assemble("kiwi/foo.kiwi", typeManager, schedulerAndWorker);
            fail("Should have failed because application is inactive");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.APP_NOT_ACTIVE, e.getErrorCode());
        }
    }


}