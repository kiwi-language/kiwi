package tech.metavm.autograph;

import tech.metavm.entity.Tree;
import tech.metavm.object.type.rest.dto.TypeTreeQuery;
import tech.metavm.object.type.rest.dto.TypeTreeResponse;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.MetaVersionStore;
import tech.metavm.util.TypeReference;

public class TreeLoader {

    private final MetaVersionStore metaVersionStore;
    private final DiskTreeStore diskTreeStore;
    private final LocalIndexSource indexSource;

    public TreeLoader(MetaVersionStore metaVersionStore, DiskTreeStore diskTreeStore, LocalIndexSource indexSource) {
        this.metaVersionStore = metaVersionStore;
        this.diskTreeStore = diskTreeStore;
        this.indexSource = indexSource;
    }

    public void load() {
        var version = metaVersionStore.getMetaVersion();
        var resp = HttpUtils.post(
                "/type/query-trees",
                new TypeTreeQuery(version),
                new TypeReference<TypeTreeResponse>() {
                }
        );
        var trees = NncUtils.map(resp.trees(), Tree::fromDTO);
        metaVersionStore.setMetaVersion(resp.version());
        diskTreeStore.remove(resp.removeTypeIds());
        diskTreeStore.save(trees);
        diskTreeStore.persist();
        indexSource.populateIndex();
    }

}
