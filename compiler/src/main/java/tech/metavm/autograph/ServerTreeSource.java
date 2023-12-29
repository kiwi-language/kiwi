package tech.metavm.autograph;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.rest.GetTreesRequest;
import tech.metavm.object.instance.rest.TreeDTO;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerTreeSource implements TreeSource {

    @Override
    public void save(List<Tree> trees) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        var trees = HttpUtils.post("/instance/trees",
                new GetTreesRequest(new ArrayList<>(ids)),
                new TypeReference<List<TreeDTO>>() {
                });
        return NncUtils.map(trees, t -> new Tree(t.id(), t.version(), t.bytes()));
    }

    @Override
    public void remove(List<Long> ids) {
        throw new UnsupportedOperationException();
    }
}
