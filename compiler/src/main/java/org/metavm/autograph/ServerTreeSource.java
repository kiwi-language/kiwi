package org.metavm.autograph;

import org.metavm.entity.Tree;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerTreeSource implements TreeSource {

    private final TypeClient typeClient;

    public ServerTreeSource(TypeClient typeClient) {
        this.typeClient = typeClient;
    }

    @Override
    public void save(List<Tree> trees) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        var trees = typeClient.getTrees(new GetTreesRequest(new ArrayList<>(ids)));
        return NncUtils.map(trees, t -> new Tree(t.id(), t.version(), t.nextNodeId(), t.bytes()));
    }

    @Override
    public void remove(List<Long> ids) {
        throw new UnsupportedOperationException();
    }
}
