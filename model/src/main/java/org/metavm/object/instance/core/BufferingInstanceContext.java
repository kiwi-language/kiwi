package org.metavm.object.instance.core;

import org.metavm.common.ErrorCode;
import org.metavm.entity.IdInitializer;
import org.metavm.entity.LoadingBuffer;
import org.metavm.entity.Tree;
import org.metavm.entity.VersionSource;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.BusinessException;
import org.metavm.util.ForwardingPointer;
import org.metavm.util.InstanceInput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.List;

public abstract class BufferingInstanceContext extends BaseInstanceContext {

    protected final LoadingBuffer loadingBuffer;
    protected final IdInitializer idInitializer;

    public BufferingInstanceContext(long appId,
                                    List<TreeSource> treeSources,
                                    VersionSource versionSource,
                                    IndexSource indexSource,
                                    IdInitializer idInitializer,
                                    @Nullable IInstanceContext parent,
                                    TypeDefProvider typeDefProvider,
                                    MappingProvider mappingProvider,
                                    boolean readonly) {
        super(appId, parent, readonly, indexSource, typeDefProvider, mappingProvider);
        this.idInitializer = idInitializer;
        this.loadingBuffer = new LoadingBuffer(this, treeSources);
    }


    @Override
    public void buffer(Id id) {
        if (parent != null && parent.contains(id))
            parent.buffer(id);
        else {
            if (id.tryGetTreeId() != null)
                loadingBuffer.buffer(id.getTreeId());
        }
    }

//    @Override
//    protected long getTypeId(long id) {
//        return idInitializer.getTypeId(id);
//    }

    @Override
    protected void initializeInstance(Id id) {
        try {
//            logger.debug("Start initializing instance: " + id);
            var result = loadingBuffer.getTree(id);
            var tree = result.tree();
            onTreeLoaded(tree);
            var input = createInstanceInput(new ByteArrayInputStream(tree.data()));
            readInstance(input);
            if(result.migrated())
                establishForwarding(result.forwardingPointers());
//            logger.debug("Finish initializing instance: {}, migrated: {}" ,id, result.migrated());
//            logger.debug("Request tree id: {}, actual tree id: {}", id.getTreeId(), tree.id());
        } catch (TreeNotFoundException e) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id);
        }
    }

    private void establishForwarding(List<ForwardingPointer> forwardingPointers) {
        for (ForwardingPointer fp : forwardingPointers) {
            mapManually(fp.sourceId(), get(fp.targetId()));
        }
    }

    protected void onTreeLoaded(Tree tree) {
    }

    private void readInstance(InstanceInput input) {
//        try (var entry = getProfiler().enter("readInstance")) {
        var instance = input.readMessage();
//            entry.addMessage("id", instance.getPhysicalId());
        onInstanceInitialized(instance);

        //        }
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean checkAliveInStore(Id id) {
        return loadingBuffer.tryGetTree(id) != null;
    }

    @Override
    public void invalidateCache(InstanceReference instance) {
        loadingBuffer.invalidateCache(List.of(instance.getTreeId()));
    }

    @Override
    public List<DurableInstance> batchGetRoots(List<Long> treeIds) {
        treeIds.forEach(loadingBuffer::buffer);
        return NncUtils.map(
                treeIds,
                treeId -> get(loadingBuffer.getRootId(treeId))
        );
    }
}
