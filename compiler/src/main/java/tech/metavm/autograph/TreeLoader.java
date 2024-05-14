package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.Tree;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MetaVersionStore;
import tech.metavm.util.NncUtils;

public class TreeLoader {

    public static final Logger logger = LoggerFactory.getLogger(TreeLoader.class);

    private final MetaVersionStore metaVersionStore;
    private final DiskTreeStore diskTreeStore;
    private final LocalIndexSource indexSource;
    private final TypeClient typeClient;

    public TreeLoader(MetaVersionStore metaVersionStore, DiskTreeStore diskTreeStore, LocalIndexSource indexSource, TypeClient typeClient) {
        this.metaVersionStore = metaVersionStore;
        this.diskTreeStore = diskTreeStore;
        this.indexSource = indexSource;
        this.typeClient = typeClient;
    }

    public void load() {
        try(var ignored = ContextUtil.getProfiler().enter("TreeLoader.load")) {
            var version = metaVersionStore.getMetaVersion();
            var resp = typeClient.queryTrees(new TypeTreeQuery(version));
            var trees = NncUtils.map(resp.trees(), Tree::fromDTO);
            metaVersionStore.setMetaVersion(resp.version());
            diskTreeStore.remove(resp.removedIds());
            diskTreeStore.save(trees);
            diskTreeStore.persist();
            indexSource.populateIndex();
        }
    }

}
