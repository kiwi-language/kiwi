package org.manul.tools;

import org.manul.entity.ModelDefRegistry;
import org.manul.entity.StdIdGenerator;
import org.manul.object.type.DirectoryAllocatorStore;
import org.manul.object.type.StdAllocators;

public class Rebooter {

    public static void reboot() {
        var saveDir = "/Users/leen/workspace/manul/model/src/main/resources";
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
