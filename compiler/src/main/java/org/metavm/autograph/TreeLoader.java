package org.metavm.autograph;

import org.metavm.entity.Tree;
import org.metavm.object.type.rest.dto.TypeTreeQuery;
import org.metavm.util.ContextUtil;
import org.metavm.util.MetaVersionStore;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            var trees = Utils.map(resp.trees(), Tree::fromDTO);
            metaVersionStore.setMetaVersion(resp.version());
            diskTreeStore.load(trees, resp.removedIds());
            diskTreeStore.persist();
            indexSource.populateIndex();
        }
    }

}
