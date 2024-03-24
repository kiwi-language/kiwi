package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.DefaultPhysicalId;
import tech.metavm.object.instance.core.TaggedPhysicalId;
import tech.metavm.object.type.*;
import tech.metavm.system.IdService;
import tech.metavm.system.RegionManager;
import tech.metavm.system.persistence.MemBlockMapper;
import tech.metavm.system.persistence.MemRegionMapper;
import tech.metavm.util.InternalException;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        ClassType typeType = ClassTypeBuilder.newBuilder("Type", null).build();
        ClassType fooType = ClassTypeBuilder.newBuilder("Foo", null).build();
        ArrayType fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);
        typeType.initId(DefaultPhysicalId.ofObject(1L, 0L, TaggedPhysicalId.ofClass(1L, 0L)));
        fooType.initId(DefaultPhysicalId.ofObject(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType), 0L, typeType));
        fooArrayType.initId(DefaultPhysicalId.ofObject(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType), 0L, typeType));

        int numIdsForClass = 10, numIdsForArray = 5;
        Map<Type, Integer> type2count = Map.of(
                fooType, numIdsForClass,
                fooArrayType, numIdsForArray
        );

        var idMap = entityIdProvider.allocate(
                TestConstants.APP_ID, type2count
        );

        Set<Long> visitedIds = new HashSet<>();
        for (Type type : List.of(fooType, fooArrayType)) {
            var idsForType = idMap.get(type);
            Assert.assertNotNull(idsForType);
            Assert.assertEquals((int) type2count.get(type), idsForType.size());
            for (var id : idsForType) {
                Assert.assertFalse(visitedIds.contains(id));
                Assert.assertTrue(type.getCategory().idRangeContains(id));
                visitedIds.add(id);
            }
        }
    }

    private java.lang.reflect.Type getJavaType(Type type) {
        if(type instanceof ClassType) {
            return Foo.class;
        }
        else if(type instanceof ArrayType) {
            return new TypeReference<ReadWriteArray<Foo>>(){}.getGenericType();
        }
        throw new InternalException();
    }

    public void testAllocateForIdService() {
        RegionManager regionManager = new RegionManager(new MemRegionMapper());
        regionManager.initialize();
        testAllocate(new IdService(new MemBlockMapper(), regionManager));
    }

    public void testAllocateForStdAllocators() {
        testAllocate(
                new BootIdProvider(
                        new StdAllocators(new MemAllocatorStore()),
                        this::getJavaType
                )
        );
    }

}