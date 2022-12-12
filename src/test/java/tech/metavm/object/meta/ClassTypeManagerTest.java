package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.PojoMatcher;

import java.util.List;

public class ClassTypeManagerTest extends TestCase {

    private InstanceContextFactory instanceContextFactory;
    private EntityQueryService entityQueryService;
    private MemInstanceStore instanceStore;
    private EntityIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        instanceStore = new MemInstanceStore();
        idProvider = new MockIdProvider();
        instanceContextFactory = new InstanceContextFactory(instanceStore).setIdService(idProvider);
        Bootstrap bootstrap = new Bootstrap(instanceContextFactory, new StdAllocators(new MemAllocatorStore()));
        bootstrap.bootAndSave();

        entityQueryService = new EntityQueryService(new InstanceQueryService(new MemInstanceSearchService()));
    }

    public void test() {
        ClassTypeManager classTypeManager = new ClassTypeManager(
                instanceContextFactory, entityQueryService, null
        );
        TypeDTO typeDTO = TypeDTO.createClass(
                "Bat",
                List.of(
                        FieldDTO.create("name", StandardTypes.getStringType().getId())
                )
        );
        TypeDTO savedTypeDTO = classTypeManager.saveType(typeDTO);

        TypeDTO loadedTypeDTO = classTypeManager.getType(savedTypeDTO.id(), true, false);
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

        classTypeManager.saveType(updatedTypeDTO);
        loadedTypeDTO = classTypeManager.getType(savedTypeDTO.id(), true, false);
        MatcherAssert.assertThat(loadedTypeDTO, PojoMatcher.of(updatedTypeDTO));
    }


}