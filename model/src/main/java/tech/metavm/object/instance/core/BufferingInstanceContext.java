package tech.metavm.object.instance.core;

import tech.metavm.entity.*;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceInput;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.List;

public abstract class BufferingInstanceContext extends BaseInstanceContext {

    protected final LoadingBuffer loadingBuffer;
    protected final EntityIdProvider idProvider;

    public BufferingInstanceContext(long appId,
                                    List<TreeSource> treeSources,
                                    VersionSource versionSource,
                                    EntityIdProvider idProvider,
                                    IndexSource indexSource,
                                    DefContext defContext,
                                    @Nullable IInstanceContext parent,
                                    boolean readonly) {
        super(appId, defContext, parent, readonly, indexSource);
        this.idProvider = idProvider;
        this.loadingBuffer = new LoadingBuffer(this, treeSources, versionSource);
    }


    @Override
    public void buffer(long id) {
        if (parent != null && parent.containsId(id))
            parent.buffer(id);
        else
            loadingBuffer.buffer(id);
    }

    @Override
    protected Instance allocateInstance(long id) {
        Type type = getType(getTypeId(id));
        if (type instanceof ArrayType arrayType) {
            return new ArrayInstance(id, arrayType, this::initializeInstance);
        } else {
            return new ClassInstance(id, (ClassType) type, this::initializeInstance);
        }
    }

    protected long getTypeId(long id) {
        return idProvider.getTypeId(id);
    }

    private void initializeInstance(Instance instance) {
        var tree = loadingBuffer.getTree(instance.getIdRequired());
        onTreeLoaded(tree);
        var input = new InstanceInput(new ByteArrayInputStream(tree.data()), this::internalGet);
        readInstance(input);
    }

    protected void onTreeLoaded(Tree tree) {
    }

    private Instance readInstance(InstanceInput input) {
        try (var entry = getProfiler().enter("readInstance")) {
            Instance instance = input.readMessage();
            entry.addMessage("id", instance.getIdRequired());
            onInstanceInitialized(instance);
            return instance;
        }
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean checkAliveInStore(long id) {
        return loadingBuffer.tryGetTree(id) != null;
    }

    @Override
    public void invalidateCache(Instance instance) {
        loadingBuffer.invalidateCache(List.of(instance.getIdRequired()));
    }
}
