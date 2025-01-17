package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.*;
import org.metavm.system.IdGenerator;
import org.metavm.system.IdService;
import org.metavm.system.MemoryBlockRepository;
import org.metavm.system.RegionManager;
import org.metavm.system.persistence.MemRegionMapper;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.HashSet;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        Klass typeType = TestUtils.newKlassBuilder("Type", null).build();
        Klass fooType = TestUtils.newKlassBuilder("Foo", null).build();
        typeType.initId(PhysicalId.of(1L, 0L));
        fooType.initId(PhysicalId.of(entityIdProvider.allocateOne(TestConstants.APP_ID), 0L));
        int numIdsForClass = 10;
        var ids = entityIdProvider.allocate(TestConstants.APP_ID, numIdsForClass);
        Set<Long> visitedIds = new HashSet<>();
        Assert.assertNotNull(ids);
        Assert.assertEquals(numIdsForClass, ids.size());
        for (var id : ids) {
            Assert.assertFalse(visitedIds.contains(id));
            visitedIds.add(id);
        }
    }

    public void testAllocateForIdService() {
        RegionManager regionManager = new RegionManager(new MemRegionMapper());
        regionManager.initialize();
        testAllocate(new IdService(new IdGenerator(new MemoryBlockRepository())));
    }

    public void testAllocateForStdAllocators() {
        testAllocate(
                new BootIdProvider(
                        new StdAllocators(new MemAllocatorStore())
                )
        );
    }

}