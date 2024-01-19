package tech.metavm.util;

import tech.metavm.entity.Bootstrap;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.object.type.MemAllocatorStore;
import tech.metavm.object.type.MemColumnStore;
import tech.metavm.object.type.StdAllocators;

public class BootstrapUtils {

    public static void bootstrap(EntityContextFactory entityContextFactory) {
        var bootstrap = new Bootstrap(
                entityContextFactory, entityContextFactory.getInstanceContextFactory(),
                new StdAllocators(new MemAllocatorStore()),
                new MemColumnStore()
        );
        bootstrap.boot();
        TestUtils.startTransaction();
        bootstrap.save(true);
        TestUtils.commitTransaction();
    }

}
