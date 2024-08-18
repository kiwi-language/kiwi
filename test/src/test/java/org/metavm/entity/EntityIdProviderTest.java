package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.mocks.Foo;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.*;
import org.metavm.system.IdGenerator;
import org.metavm.system.IdService;
import org.metavm.system.MemoryBlockRepository;
import org.metavm.system.RegionManager;
import org.metavm.system.persistence.MemRegionMapper;
import org.metavm.util.InternalException;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;
import org.metavm.util.TypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        Klass typeType = TestUtils.newKlassBuilder("Type", null).build();
        Klass fooType = TestUtils.newKlassBuilder("Foo", null).build();
        ArrayType fooArrayType = new ArrayType(fooType.getType(), ArrayKind.READ_WRITE);
        typeType.initId(PhysicalId.of(1L, 0L, TestUtils.mockClassType()));
        fooType.initId(PhysicalId.of(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType.getType()), 0L, typeType.getType()));
        fooArrayType.initId(PhysicalId.of(entityIdProvider.allocateOne(TestConstants.APP_ID, typeType.getType()), 0L, typeType.getType()));

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
        testAllocate(new IdService(new IdGenerator(new MemoryBlockRepository())));
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