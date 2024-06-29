package org.metavm.entity;

import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.NncUtils;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class LoadingBuffer {

    public static final Logger logger = LoggerFactory.getLogger(LoadingBuffer.class);

    private final List<Long> bufferedIds = new ArrayList<>();
    private final Set<Long> visited = new HashSet<>();
    private final IInstanceContext context;
    private final Map<Long, List<Id>> index = new HashMap<>();
    private final Map<Id, Tree> invertedIndex = new HashMap<>();
    private final List<TreeSource> treeSources;

    public LoadingBuffer(IInstanceContext context, List<TreeSource> treeSources) {
        this.context = context;
        this.treeSources = new ArrayList<>(treeSources);
    }

    public boolean buffer(Long id) {
        if (visited.add(id)) {
            bufferedIds.add(id);
            return true;
        } else
            return false;
    }

    public Id getRootId(long id) {
        buffer(id);
        flush();
        return requireNonNull(index.get(id)).get(0);
    }

    public Tree getTree(Id id) {
        return NncUtils.requireNonNull(
                tryGetTree(id),
                TreeNotFoundException::new
        );
    }

    public List<Id> getIdsInTree(long treeId) {
        return index.get(treeId);
    }

    public void invalidateCache(List<Long> ids) {
        for (TreeSource treeSource : treeSources) {
            treeSource.remove(ids);
        }
    }

    public Tree tryGetTree(Id id) {
        var tree = invertedIndex.get(id);
        if (tree != null)
            return tree;
        buffer(id.getTreeId());
        flush();
        tree = invertedIndex.get(id);
        return tree;
    }

    public void flush() {
        if (bufferedIds.isEmpty())
            return;
        loadForest(bufferedIds);
        bufferedIds.clear();
    }

    private void loadForest(List<Long> treeIds) {
        try(var ignored = context.getProfiler().enter("LoadingBuffer.loadForest")) {
            Set<Long> misses = new HashSet<>(treeIds);
            List<TreeSource> prevSources = new ArrayList<>();
            for (TreeSource treeSource : treeSources) {
                if (misses.isEmpty())
                    break;
                var trees = treeSource.load(misses, context);
                var hits = new ArrayList<Tree>();
                for (Tree tree : trees) {
//                    if (tree.version() == versionMap.get(tree.id())) {
                        addTree(tree);
                        hits.add(tree);
                        misses.remove(tree.id());
//                    }
                }
                if (!hits.isEmpty()) {
                    for (TreeSource prevSource : prevSources) {
                        prevSource.save(hits);
                    }
                }
                prevSources.add(treeSource);
            }
        }
    }

    private void addTree(Tree tree) {
        var ids = new ArrayList<Id>();
        index.put(tree.id(), ids);
        new StreamVisitor(new ByteArrayInputStream(tree.data())) {

            @Override
            public void visitRecordBody(long nodeId, TypeOrTypeKey typeOrTypeKey) {
                var id = PhysicalId.of(getTreeId(), nodeId, typeOrTypeKey);
                invertedIndex.put(id, tree);
                ids.add(id);
                super.visitRecordBody(nodeId, typeOrTypeKey);
            }

        }.visitMessage();

    }

}
