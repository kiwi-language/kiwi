package org.metavm.asm;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.FlowSavingContext;
import org.metavm.flow.Flows;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ClassInstanceWrap;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AssemblerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(AssemblerTest.class);

    private EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
    }

    public void testParentChild() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/ParentChild.masm");
    }

    public void testMyList() {
//        assemble(List.of(source));
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/List.masm");
    }

    public void testShopping() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm");
        // redeploy
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Shopping.masm");
    }

    public void testLivingBeing() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/LivingBeing.masm");
    }

    public void testUtils() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Utils.masm");
    }

    public void testGenericOverloading() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/GenericOverloading.masm");
    }

    public void testLambda() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/Lambda.masm");
    }

    public void testCreateArray() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/CreateArray.masm");
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance("Utils")));
            var method = klass.getMethod("createArray", List.of());
            var result = Flows.invoke(method.getRef(), null, List.of(), context);
            Assert.assertNotNull(result);
            var array = result.resolveArray();
            Assert.assertSame(ArrayKind.CHILD, array.getInstanceType().getKind());
        }
    }

    public void testInstanceOf() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/instanceof.masm");
    }

    public void testUpdateField() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/update_field.masm");
    }

    public void testTreeSet() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/tree_set.masm");
        var id = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("TreeSetLab", "create", List.of()));
        var elements = List.of(5,4,3,2,1);
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "addAll", List.of(elements)));
        var containsAll = (boolean) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "containsAll", List.of(elements)));
        Assert.assertTrue(containsAll);
        var removed = (boolean) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "remove", List.of(1)));
        Assert.assertTrue(removed);
        var first = TestUtils.doInTransaction(() -> apiClient.callMethod(id, "first", List.of()));
        Assert.assertEquals(2, first);
        TestUtils.doInTransaction(() -> apiClient.callMethod(id, "retainAll", List.of(List.of(5, 3, 2))));
        var size = (int) TestUtils.doInTransaction(() -> apiClient.callMethod(id, "size", List.of()));
        Assert.assertEquals(3, size);
    }

    public void testSwapSuper() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/swap_super_before.masm");
        var id = saveInstance("Derived", Map.of(
                "value1", 1, "value2", 2, "value3", 3
        ));
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/swap_super_after.masm");
        Assert.assertEquals(
                2,
                callMethod(id, "getValue2", List.of())
        );
    }

    public void testCircularReference() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/circular_ref.masm");
        var id = saveInstance("Foo", Map.of());
        getObject(id);
    }

    public void testSmallInt() {
        deploy("/Users/leen/workspace/object/test/src/test/resources/asm/smallint.masm");
        var className = "SmallIntFoo";
        Assert.assertEquals((short) 3, callMethod(className, "addShorts", List.of(1, 2)));
        Assert.assertEquals(3.0, callMethod(className, "addShortAndDouble", List.of(1, 2)));
    }

    private void assemble(List<String> sources, Assembler assembler) {
        assembler.assemble(sources);
        assembler.generateClasses(TestConstants.TARGET);
    }

    private String saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    private Object callMethod(String qualifier, String methodName,List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    private ClassInstanceWrap getObject(String id) {
        return apiClient.getObject(id);
    }

    private void deploy(String source) {
        FlowSavingContext.initConfig();
        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            context.loadKlasses();
            var assembler = AssemblerFactory.createWithStandardTypes();
            assemble(List.of(source), assembler);
            ContextUtil.setAppId(TestConstants.APP_ID);
            TestUtils.doInTransaction(() -> {
                try(var input = new FileInputStream(TestConstants.TARGET + "/target.mva")) {
                    typeManager.deploy(input);
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            TestUtils.waitForDDLPrepared(schedulerAndWorker);
        }
    }

}