package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.TreeSource;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.*;

@Slf4j
public abstract class BufferingInstanceContext extends BaseInstanceContext {

    protected final LoadingBuffer loadingBuffer;
    protected final IdInitializer idInitializer;
    private final Set<Long> loadedTreeIds = new HashSet<>();
    private final Map<Long, NavigableSet<ForwardingPointer>> forwardingPointers = new HashMap<>();

    public BufferingInstanceContext(long appId,
                                    List<TreeSource> treeSources,
                                    IndexSource indexSource,
                                    IdInitializer idInitializer,
                                    @Nullable IInstanceContext parent,
                                    boolean readonly,
                                    long timeout) {
        super(appId, parent, readonly, indexSource, timeout);
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
        if (DebugEnv.traceInstanceLoading)
            log.trace("Initializing instance {}", id);
        var instances = initializeInstance0(id);
        var visited = new IdentitySet<Instance>();
        for (Instance instance : instances) {
            instance.accept(new InstanceVisitor<Void>() {
                @Override
                public Void visitInstance(Instance instance) {
                    if (instance instanceof StringInstance || instance.getContext() != BufferingInstanceContext.this || !visited.add(instance))
                       return null;
                    if(instance instanceof ClassInstance clsInst)
                        updateMemoryIndex(clsInst);
                    instance.forEachReference(r -> {
                        if(r.isResolved())
                            r.get().accept(this);
                    });
                    return null;
                }
            });
        }
    }

    private List<Instance> initializeInstance0(Id id) {
        try {
            var result = loadingBuffer.getTree(id);
            var trees = result.trees();
            var instances = new ArrayList<Instance>();
            for (Tree tree : trees) {
                loadTree(tree, instances);
            }
            return instances;
        } catch (TreeNotFoundException e) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id);
        }
    }

    @Override
    public void loadTree(long id) {
        loadTree(loadingBuffer.loadTree(id), new ArrayList<>());
    }

    private void loadTree(Tree tree, List<Instance> instances) {
        if (onTreeLoaded(tree)) {
            var input = createInstanceInput(new ByteArrayInputStream(tree.data()));
            readInstance(input, instances);
        }
    }

    private void establishForwarding(List<ForwardingPointer> forwardingPointers) {
        for (ForwardingPointer fp : forwardingPointers) {
            addForwardingPointer(fp);
        }
    }

    protected void addForwardingPointer(ForwardingPointer forwardingPointer) {
        this.forwardingPointers.computeIfAbsent(forwardingPointer.sourceId().getTreeId(), k -> new TreeSet<>())
                .add(forwardingPointer);
//        mapManually(forwardingPointer.sourceId(), get(forwardingPointer.targetId()));
    }

    protected Collection<ForwardingPointer> getForwardingPointers(long groveId) {
        var fps = forwardingPointers.get(groveId);
        return fps != null ? Collections.unmodifiableCollection(fps) : List.of();
    }

    public Map<Long, NavigableSet<ForwardingPointer>> getForwardingPointers() {
        return forwardingPointers;
    }

    protected boolean onTreeLoaded(Tree tree) {
        return loadedTreeIds.add(tree.id());
    }

    private void readInstance(InstanceInput input, List<Instance> instances) {
        int numTrees = input.readInt();
        for (int i = 0; i < numTrees; i++) {
            var tree = input.readTree();
            if(tree instanceof Instance instance) {
                onInstanceInitialized(instance);
                instances.add(instance);
            }
            else if(tree instanceof ForwardingPointer fp)
                addForwardingPointer(fp);
            else
                throw new IllegalStateException("Unrecognized message: " + tree);
        }
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean checkAliveInStore(Id id) {
        return !loadingBuffer.tryGetTree(id).isEmpty();
    }

    @Override
    public List<Instance> batchGetRoots(List<Long> treeIds) {
        treeIds.forEach(loadingBuffer::buffer);
        return Utils.map(
                treeIds,
            treeId -> get(loadingBuffer.getRootId(treeId))
        );
    }

}
