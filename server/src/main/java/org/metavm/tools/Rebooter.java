package org.metavm.tools;

import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.StdIdGenerator;
import org.metavm.object.type.DirectoryAllocatorStore;
import org.metavm.object.type.StdAllocators;

public class Rebooter {

    public static void reboot() {
        var saveDir = "/Users/leen/workspace/kiwi/model/src/main/resources";
        var allocatorStore = new DirectoryAllocatorStore(saveDir);

        ModelDefRegistry.setDefContext(null);
        var stdAllocators = new StdAllocators(allocatorStore);
//        var eventQueue = new MockEventQueue();
//        var indexEntryMapper = new MemIndexEntryMapper();
//        var instanceStore = new MemInstanceStore(new LocalCache());
//        var idProvider = new MockIdProvider();

        var idGenerator = new StdIdGenerator(() -> stdAllocators.allocate(1).getFirst());
        idGenerator.generate();

        idGenerator.getIds().forEach((identity, id) -> {
            if (id.getNodeId() == 0L)
                stdAllocators.putId(identity, id, idGenerator.getNextNodeId(identity));
            else
                stdAllocators.putId(identity, id);
        });
        stdAllocators.save();


//        var instanceContextFactory = new InstanceContextFactory(instanceStore, eventQueue);
//        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
//        instanceContextFactory.setIdService(idProvider);
//        instanceContextFactory.setCache(new MockCache());
//        entityContextFactory.setInstanceLogService(new MockInstanceLogService());
//        var bootstrap = new Bootstrap(entityContextFactory,
//                stdAllocators,
//                new FileColumnStore(saveDir),
//                new FileTypeTagStore(saveDir));
//        MockTransactionUtils.doInTransactionWithoutResult(bootstrap::bootAndSave);
    }

}
