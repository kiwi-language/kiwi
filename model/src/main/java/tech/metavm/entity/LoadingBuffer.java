package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceVersion;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.StreamVisitor;

import java.io.ByteArrayInputStream;
import java.util.*;

public class LoadingBuffer {

    public static final Logger LOGGER = LoggerFactory.getLogger(LoadingBuffer.class);

    private final List<Long> bufferedIds = new ArrayList<>();
    private final Set<Long> visited = new HashSet<>();
    private final IInstanceContext context;
    private final Map<Long, Tree> invertedIndex = new HashMap<>();
    private final List<TreeSource> treeSources;
    private final VersionSource versionSource;

    public LoadingBuffer(IInstanceContext context, List<TreeSource> treeSources, VersionSource versionSource) {
        this.context = context;
        this.treeSources = treeSources;
        this.versionSource = versionSource;
    }

    public boolean buffer(long id) {
        if (visited.add(id)) {
            bufferedIds.add(id);
            return true;
        } else
            return false;
    }

    public Tree getTree(long id) {
        return NncUtils.requireNonNull(
                tryGetTree(id),
                () -> new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id)
        );
    }

    public void invalidateCache(List<Long> ids) {
        for (TreeSource treeSource : treeSources) {
            treeSource.remove(ids);
        }
    }

    public Tree tryGetTree(long id) {
        var tree = invertedIndex.get(id);
        if (tree != null)
            return tree;
        buffer(id);
        flush();
        tree = invertedIndex.get(id);
        return tree;
    }

    private void flush() {
        if (bufferedIds.isEmpty())
            return;
        loadForest(bufferedIds);
        bufferedIds.clear();
    }

    private void loadForest(List<Long> ids) {
        try(var ignored = context.getProfiler().enter("LoadingBuffer.loadForest")) {
            var rootVersions = versionSource.getRootVersions(ids, context);
            Map<Long, Long> versionMap = NncUtils.toMap(rootVersions, InstanceVersion::id, InstanceVersion::version);
            Set<Long> misses = new HashSet<>(versionMap.keySet());
            List<TreeSource> prevSources = new ArrayList<>();
            for (TreeSource treeSource : treeSources) {
                if (misses.isEmpty())
                    break;
                var trees = treeSource.load(misses, context);
                var hits = new ArrayList<Tree>();
                for (Tree tree : trees) {
                    if (tree.version() == versionMap.get(tree.id())) {
                        addTree(tree);
                        hits.add(tree);
                        misses.remove(tree.id());
                    }
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
        new StreamVisitor(new ByteArrayInputStream(tree.data())) {

            @Override
            public void visitRecordBody(long id) {
                invertedIndex.put(id, tree);
                super.visitRecordBody(id);
            }

        }.visitMessage();

    }

}
