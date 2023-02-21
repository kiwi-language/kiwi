package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.*;
import tech.metavm.job.JobManager;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.LoadByPathsResponse;
import tech.metavm.object.meta.rest.dto.TypeDTO;
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
        MockRegistry.setUp(idProvider, instanceStore);

        instanceSearchService = new MemInstanceSearchService();

        InstanceContextFactory instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore, instanceSearchService);

        TransactionOperations transactionOperations = new MockTransactionOperations();

        EntityQueryService entityQueryService = new EntityQueryService(new InstanceQueryService(instanceSearchService));
        typeManager = new TypeManager(
                instanceContextFactory, entityQueryService,
                new JobManager(instanceContextFactory, transactionOperations),
                transactionOperations
        );
    }

    public void test() {
        TypeDTO typeDTO = TypeDTO.createClass(
                "Bat",
                List.of(
                        FieldDTO.create("name", StandardTypes.getStringType().getId())
                )
        );
        TypeDTO savedTypeDTO = typeManager.saveType(typeDTO);
        TypeDTO loadedTypeDTO = typeManager.getType(savedTypeDTO.id(), true, false);
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(savedTypeDTO));

        long nameFieldId = ((ClassParamDTO)savedTypeDTO.param()).fields().get(0).id();

        TypeDTO updatedTypeDTO = TypeDTO.createClass(
                savedTypeDTO.id(),
                "Bat Update",
                List.of(
                        FieldDTO.create(
                                nameFieldId,
                                "name",
                                savedTypeDTO.id(),
                                StandardTypes.getStringType().getId())
                )
        );

        typeManager.saveType(updatedTypeDTO);
        loadedTypeDTO = typeManager.getType(savedTypeDTO.id(), true, false);
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(updatedTypeDTO));
    }

    public void testRemove() {
        TypeDTO typeDTO = TypeDTO.createClass(
                "Bat",
                List.of(
                        FieldDTO.create("name", StandardTypes.getStringType().getId())
                )
        );
        TypeDTO savedTypeDTO = typeManager.saveType(typeDTO);

        Assert.assertTrue(instanceSearchService.contains(savedTypeDTO.id()));

        typeManager.remove(savedTypeDTO.id());
        Assert.assertFalse(instanceSearchService.contains(savedTypeDTO.id()));
    }

    public void testLoadByPaths() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        PrimitiveType stringType = MockRegistry.getStringType();
        String path1 = "傻.巴.编号";
        String path2 = "$" + fooType.getId() + ".巴子.*.巴巴巴巴.*.编号";
        LoadByPathsResponse response = typeManager.loadByPaths(List.of(path1, path2));

        Assert.assertEquals(
                Map.of(path1, stringType.getId(), path2, stringType.getId()),
                response.path2typeId()
        );

        Assert.assertEquals(response.types(), List.of(stringType.toDTO()));
    }

}