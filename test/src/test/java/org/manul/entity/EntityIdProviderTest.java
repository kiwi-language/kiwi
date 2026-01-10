package org.manul.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.object.instance.core.PhysicalId;
import org.manul.object.type.*;
import org.manul.system.IdGenerator;
import org.manul.system.IdService;
import org.manul.system.MemoryBlockRepository;
import org.manul.util.TestConstants;
import org.manul.util.TestUtils;

import java.util.HashSet;
import java.util.Set;

public class EntityIdProviderTest extends TestCase {

    private void testAllocate(EntityIdProvider entityIdProvider) {
        TestUtils.newKlassBuilder(PhysicalId.of(1L, 0L), "Type", null).build();
        TestUtils.newKlassBuilder(PhysicalId.of(entityIdProvider.allocateOne(TestConstants.APP_ID), 0L),
                "Foo", null).build();
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