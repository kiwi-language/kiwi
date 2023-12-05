package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.system.IdService;
import tech.metavm.system.MemBlockMapper;
import tech.metavm.system.MemRegionMapper;
import tech.metavm.system.RegionManager;
import tech.metavm.mocks.Foo;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        ClassType typeType = ClassBuilder.newBuilder("Type", null).build();
        ClassType fooType = ClassBuilder.newBuilder("Foo", null).build();
        ArrayType fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);
        typeType.initId(1L);
        fooType.initId(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType));
        fooArrayType.initId(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType));

        int numIdsForClass = 10, numIdsForArray = 5;
        Map<Type, Integer> type2count = Map.of(
                fooType, numIdsForClass,
                fooArrayType, numIdsForArray
        );

        Map<Type, List<Long>> idMap = entityIdProvider.allocate(
                TestConstants.APP_ID, type2count
        );

        Set<Long> visitedIds = new HashSet<>();
        for (Type type : List.of(fooType, fooArrayType)) {
            List<Long> idsForType = idMap.get(type);
            Assert.assertNotNull(idsForType);
            Assert.assertEquals((int) type2count.get(type), idsForType.size());
            for (Long id : idsForType) {
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