package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.type.*;
import tech.metavm.system.IdService;
import tech.metavm.system.RegionManager;
import tech.metavm.system.persistence.MemBlockMapper;
import tech.metavm.system.persistence.MemRegionMapper;
import tech.metavm.util.InternalException;
import tech.metavm.util.TestConstants;
import tech.metavm.util.TestUtils;
import tech.metavm.util.TypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        Klass typeType = ClassTypeBuilder.newBuilder("Type", null).build();
        Klass fooType = ClassTypeBuilder.newBuilder("Foo", null).build();
        ArrayType fooArrayType = new ArrayType(fooType.getType(), ArrayKind.READ_WRITE);
        typeType.initId(PhysicalId.ofObject(1L, 0L, TestUtils.mockClassType()));
        fooType.initId(PhysicalId.ofObject(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType.getType()), 0L, typeType.getType()));
        fooArrayType.initId(PhysicalId.ofObject(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType.getType()), 0L, typeType.getType()));

        int numIdsForClass = 10;
        Map<Type, Integer> type2count = Map.of(
                fooType.getType(), numIdsForClass
        );

        var idMap = entityIdProvider.allocate(
                TestConstants.APP_ID, type2count
        );

        Set<Long> visitedIds = new HashSet<>();
        for (var type : List.of(fooType.getType())) {
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