package org.metavm.autograph;

import org.metavm.entity.Tree;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.rest.GetTreesRequest;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerTreeSource implements TreeSource {

    public static final Logger logger = LoggerFactory.getLogger(ServerTreeSource.class);

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
        return Utils.map(trees, t -> new Tree(t.id(), t.version(), t.nextNodeId(), t.bytes()));
    }

    @Override
    public void remove(List<Long> ids) {
        throw new UnsupportedOperationException();
    }
}
