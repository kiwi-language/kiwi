package tech.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;

public class TypeManagerTest extends TestCase {

    private TypeManager typeManager;
    private MemInstanceSearchService instanceSearchService;
    @SuppressWarnings("FieldCanBeLocal")
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        instanceStore = new MemInstanceStore();
        EntityIdProvider idProvider = new MockIdProvider();
        instanceSearchService = new MemInstanceSearchService();
        InstanceContextFactory instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        entityContextFactory.setInstanceLogService(new MockInstanceLogService());

        BootstrapUtils.bootstrap(entityContextFactory);

        TransactionOperations transactionOperations = new MockTransactionOperations();

        EntityQueryService entityQueryService = new EntityQueryService(new InstanceQueryService(instanceSearchService));
        typeManager = new TypeManager(
                entityContextFactory, entityQueryService,
                new TaskManager(entityContextFactory, transactionOperations),
                transactionOperations);
        var instanceManager = new InstanceManager(
                entityContextFactory, instanceStore, new InstanceQueryService(instanceSearchService)
        );
        typeManager.setInstanceManager(instanceManager);
    }

    public void test() {
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .addField(
                        FieldDTOBuilder.newBuilder("name", StandardTypes.getStringType().getRef())
                                .build()
                )
                .build();
        TypeDTO savedTypeDTO = typeManager.saveType(typeDTO);
        TypeDTO loadedTypeDTO = typeManager.getType(new GetTypeRequest(savedTypeDTO.id(), true)).type();
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(savedTypeDTO));

        long nameFieldId = ((ClassTypeParam) savedTypeDTO.param()).fields().get(0).id();

        TypeDTO updatedTypeDTO = ClassTypeDTOBuilder.newBuilder("Bat Update")
                .id(savedTypeDTO.id())
                .addField(
                        FieldDTOBuilder.newBuilder("name", StandardTypes.getStringType().getRef())
                                .id(nameFieldId)
                                .declaringTypeId(savedTypeDTO.id())
                                .build()
                )
                .build();

        typeManager.saveType(updatedTypeDTO);
        loadedTypeDTO = typeManager.getType(new GetTypeRequest(savedTypeDTO.id(), true)).type();
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(updatedTypeDTO));
    }

    public void testRemove() {
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .addField(
                        FieldDTOBuilder.newBuilder("name", StandardTypes.getStringType().getRef())
                                .build()
                )
                .build();
        TypeDTO savedTypeDTO = typeManager.saveType(typeDTO);

        Assert.assertTrue(instanceSearchService.contains(savedTypeDTO.id()));

        typeManager.remove(savedTypeDTO.id());
        Assert.assertFalse(instanceSearchService.contains(savedTypeDTO.id()));
    }

    public void testLoadByPaths() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        PrimitiveType stringType = MockRegistry.getStringType();
        String path1 = "傻.巴.编号";
        String path2 = "$" + fooType.tryGetId() + ".巴子.*.巴巴巴巴.*.编号";
        LoadByPathsResponse response = typeManager.loadByPaths(List.of(path1, path2));

        Assert.assertEquals(
                Map.of(path1, stringType.getId(), path2, stringType.getId()),
                response.path2typeId()
        );
        try (var context = MockRegistry.newContext(10L)) {
            Assert.assertEquals(response.types(), List.of(stringType.toDTO()));
        }
    }

    public void testShopping() {
        var typeIds = MockUtils.createShoppingTypes(typeManager);
        var productTypeDTO = typeManager.getType(new GetTypeRequest(typeIds.productTypeId(), false)).type();
        Assert.assertEquals(2, productTypeDTO.getClassParam().fields().size());
        var couponStateType = typeManager.getType(new GetTypeRequest(typeIds.couponStateTypeId(), false)).type();
        Assert.assertEquals(2, couponStateType.getClassParam().enumConstants().size());

    }

}