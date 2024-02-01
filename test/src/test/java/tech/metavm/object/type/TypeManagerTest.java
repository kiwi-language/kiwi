package tech.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.FlowManager;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;

public class TypeManagerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManagerTest.class);

    private TypeManager typeManager;
    private FlowManager flowManager;
    private MemInstanceSearchServiceV2 instanceSearchService;
    @SuppressWarnings("FieldCanBeLocal")
    private MemInstanceStore instanceStore;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        EntityContextFactory entityContextFactory = bootResult.entityContextFactory();
        instanceStore = bootResult.instanceStore();
        instanceSearchService = bootResult.instanceSearchService();
        var transactionOperations = new MockTransactionOperations();
        var entityQueryService = new EntityQueryService(new InstanceQueryService(instanceSearchService));
        typeManager = new TypeManager(
                entityContextFactory, entityQueryService,
                new TaskManager(entityContextFactory, transactionOperations),
                transactionOperations);
        instanceManager = new InstanceManager(
                entityContextFactory, instanceStore, new InstanceQueryService(instanceSearchService)
        );
        typeManager.setInstanceManager(instanceManager);
        flowManager = new FlowManager(entityContextFactory);
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        typeManager = null;
        flowManager = null;
        instanceSearchService = null;
        instanceStore = null;
        instanceManager = null;
    }

    public void test() {
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .tmpId(NncUtils.randomNonNegative())
                .addField(
                        FieldDTOBuilder.newBuilder("name", StandardTypes.getStringType().getRef())
                                .tmpId(NncUtils.randomNonNegative())
                                .build()
                )
                .build();
        TypeDTO savedTypeDTO = TestUtils.doInTransaction(() -> typeManager.saveType(typeDTO));
        TypeDTO loadedTypeDTO = typeManager.getType(new GetTypeRequest(savedTypeDTO.id(), true)).type();
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(savedTypeDTO));
        TypeDTO updatedTypeDTO = ClassTypeDTOBuilder.newBuilder("Bat Update")
                .id(savedTypeDTO.id())
                .fields(null)
                .methods(null)
                .mappings(null)
                .build();

        TestUtils.doInTransactionWithoutResult(() -> typeManager.saveType(updatedTypeDTO));
        loadedTypeDTO = typeManager.getType(new GetTypeRequest(savedTypeDTO.id(), true)).type();
        TestUtils.logJSON(LOGGER, loadedTypeDTO);
        TestUtils.logJSON(LOGGER, updatedTypeDTO);
        Assert.assertEquals(loadedTypeDTO.name(), updatedTypeDTO.name());
    }

    public void testRemove() {
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .tmpId(NncUtils.randomNonNegative())
                .addField(
                        FieldDTOBuilder.newBuilder("name", StandardTypes.getStringType().getRef())
                                .tmpId(NncUtils.randomNonNegative())
                                .build()
                )
                .build();
        TypeDTO savedTypeDTO = TestUtils.doInTransaction(() -> typeManager.saveType(typeDTO));
        Assert.assertTrue(instanceSearchService.contains(savedTypeDTO.id()));
        TestUtils.doInTransactionWithoutResult(() -> typeManager.remove(savedTypeDTO.id()));
        Assert.assertFalse(instanceSearchService.contains(savedTypeDTO.id()));
    }

    public void testLoadByPaths() {
        ContextUtil.setAppId(Constants.ROOT_APP_ID);
        var fooType = ModelDefRegistry.getClassType(Foo.class);
        var stringType = StandardTypes.getStringType();
        String path1 = "傻.巴.编号";
        String path2 = "$$" + fooType.getId() + ".巴子.*.巴列表.*.编号";
        LoadByPathsResponse response = typeManager.loadByPaths(List.of(path1, path2));

        Assert.assertEquals(
                Map.of(path1, stringType.getId(), path2, stringType.getId()),
                response.path2typeId()
        );
//        try (var context = entityContextFactory.newContext(10L)) {
        Assert.assertEquals(response.types(), List.of(stringType.toDTO()));
//        }


    }

    public void testShopping() {
        var typeIds = MockUtils.createShoppingTypes(typeManager, flowManager);
        var productTypeDTO = typeManager.getType(new GetTypeRequest(typeIds.productTypeId(), false)).type();
        Assert.assertEquals(2, productTypeDTO.getClassParam().fields().size());
        var couponStateType = typeManager.getType(new GetTypeRequest(typeIds.couponStateTypeId(), false)).type();
        Assert.assertEquals(2, couponStateType.getClassParam().enumConstants().size());
        TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(
                        List.of(productTypeDTO),
                        List.of(),
                        List.of()
                )
        ));
    }

    public void testAddFieldWithDefaultValueToTemplate() {
        var nodeTypeIds = MockUtils.createNodeTypes(typeManager, flowManager);
        var nodeType = typeManager.getParameterizedType(
                new GetParameterizedTypeRequest(
                        RefDTO.fromId(nodeTypeIds.nodeTypeId()),
                        List.of(StandardTypes.getStringType().getRef()),
                        List.of()
                )
        ).type();
        var labelFieldId = TestUtils.getFieldIdByCode(nodeType, "label");
        var valueFieldId = TestUtils.getFieldIdByCode(nodeType, "value");
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.create(new InstanceDTO(
                null,
                nodeType.getRef(),
                null,
                null,
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(labelFieldId, PrimitiveFieldValue.createString("node001")),
                                InstanceFieldDTO.create(valueFieldId, PrimitiveFieldValue.createString("hello"))
                        )
                )
        )));
        TestUtils.doInTransactionWithoutResult(() -> typeManager.saveField(FieldDTOBuilder.newBuilder("编号", StandardTypes.getStringType().getRef())
                .declaringTypeId(nodeTypeIds.nodeTypeId())
                .defaultValue(PrimitiveFieldValue.createString("000"))
                .build()));
    }

}