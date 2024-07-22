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
import java.util.*;

public abstract class BufferingInstanceContext extends BaseInstanceContext {

    protected final LoadingBuffer loadingBuffer;
    protected final IdInitializer idInitializer;
    private final Set<Long> loadedTreeIds = new HashSet<>();
    private final Map<Long, NavigableSet<ForwardingPointer>> forwardingPointers = new HashMap<>();

    public BufferingInstanceContext(long appId,
                                    List<TreeSource> treeSources,
                                    VersionSource versionSource,
                                    IndexSource indexSource,
                                    IdInitializer idInitializer,
                                    @Nullable IInstanceContext parent,
                                    TypeDefProvider typeDefProvider,
                                    MappingProvider mappingProvider,
                                    boolean readonly,
                                    long timeout) {
        super(appId, parent, readonly, indexSource, typeDefProvider, mappingProvider, timeout);
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
        var instances = initializeInstance0(id);
        for (DurableInstance instance : instances) {
            instance.accept(new DurableInstanceVisitor() {
                @Override
                public void visitDurableInstance(DurableInstance instance) {
                    instance.forEachReference((r, isChild) -> {
                        if (r.isEager())
                            r.resolve();
                        if(r.isResolved())
                            r.resolve().accept(this);
                    });
                }
            });
        }
    }

    private List<DurableInstance> initializeInstance0(Id id) {
        try {
            var result = loadingBuffer.getTree(id);
            var trees = result.trees();
            var instances = new ArrayList<DurableInstance>();
            for (Tree tree : trees) {
                if (onTreeLoaded(tree)) {
                    var input = createInstanceInput(new ByteArrayInputStream(tree.data()));
                    readInstance(input, instances);
                }
            }
            return instances;
        } catch (TreeNotFoundException e) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id);
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

    private void readInstance(InstanceInput input, List<DurableInstance> instances) {
        int numTrees = input.readInt();
        for (int i = 0; i < numTrees; i++) {
            var tree = input.readTree();
            if(tree instanceof DurableInstance instance) {
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

    @Override
    public void removeForwardingPointer(DurableInstance instance) {
        Objects.requireNonNull(forwardingPointers.get(instance.getOldId().getTreeId()))
                .remove(new ForwardingPointer(instance.getOldId(), instance.getCurrentId()));
        instance.clearOldId();
    }
}
