package tech.metavm.tools;

import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.type.DirectoryAllocatorStore;
import tech.metavm.object.type.FileColumnStore;
import tech.metavm.object.type.FileTypeTagStore;
import tech.metavm.object.type.StdAllocators;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockTransactionUtils;

public class Rebooter {

    public static void reboot() {
        var saveDir = "/Users/leen/workspace/object/model/src/main/resources";
        var allocatorStore = new DirectoryAllocatorStore(saveDir);

        ModelDefRegistry.setDefContext(null);
        var stdAllocators = new StdAllocators(allocatorStore);
        var eventQueue = new MockEventQueue();
        var indexEntryMapper = new MemIndexEntryMapper();
        var instanceStore = new MemInstanceStore();
        var idProvider = new MockIdProvider();

        var instanceContextFactory = new InstanceContextFactory(instanceStore, eventQueue);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
        instanceContextFactory.setIdService(idProvider);
        instanceContextFactory.setCache(new MockCache());
        entityContextFactory.setInstanceLogService(new MockInstanceLogService());
        var bootstrap = new Bootstrap(entityContextFactory,
                stdAllocators,
                new FileColumnStore(saveDir),
                new FileTypeTagStore(saveDir),
                new MemoryStdIdStore());
        MockTransactionUtils.doInTransactionWithoutResult(bootstrap::bootAndSave);
    }

}
