package tech.metavm.object.instance.core;

import tech.metavm.entity.*;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.InstanceInput;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class BufferingInstanceContext extends BaseInstanceContext {

    protected final LoadingBuffer loadingBuffer;
    protected final IdInitializer idInitializer;

    public BufferingInstanceContext(long appId,
                                    List<TreeSource> treeSources,
                                    VersionSource versionSource,
                                    IndexSource indexSource,
                                    IdInitializer idInitializer,
                                    @Nullable IInstanceContext parent,
                                    TypeProvider typeProvider,
                                    MappingProvider mappingProvider,
                                    ParameterizedFlowProvider parameterizedFlowProvider,
                                    boolean readonly) {
        super(appId, parent, readonly, indexSource, typeProvider, mappingProvider, parameterizedFlowProvider);
        this.idInitializer = idInitializer;
        this.loadingBuffer = new LoadingBuffer(this, treeSources, versionSource);
    }


    @Override
    public void buffer(Id id) {
        if (parent != null && parent.contains(id))
            parent.buffer(id);
        else {
            if(id.tryGetPhysicalId() != null)
                loadingBuffer.buffer(id);
        }
    }

//    @Override
//    protected long getTypeId(long id) {
//        return idInitializer.getTypeId(id);
//    }

    @Override
    protected void initializeInstance(DurableInstance instance) {
        var tree = loadingBuffer.getTree(instance.tryGetId());
        onTreeLoaded(tree);
        var input = new InstanceInput(new ByteArrayInputStream(tree.data()), this::internalGet);
        readInstance(input);
    }

    protected void onTreeLoaded(Tree tree) {
    }

    private Instance readInstance(InstanceInput input) {
//        try (var entry = getProfiler().enter("readInstance")) {
            var instance = input.readMessage();
//            entry.addMessage("id", instance.getPhysicalId());
            onInstanceInitialized(instance);
            return instance;
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
    public void invalidateCache(DurableInstance instance) {
        loadingBuffer.invalidateCache(List.of(requireNonNull(instance.tryGetId())));
    }
}
