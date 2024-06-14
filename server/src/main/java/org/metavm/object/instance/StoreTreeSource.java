package org.metavm.object.instance;

import org.metavm.entity.Tree;
import org.metavm.object.instance.core.IInstanceContext;

import java.util.Collection;
import java.util.List;

public class StoreTreeSource implements TreeSource {

    private final IInstanceStore instanceStore;

    public StoreTreeSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public void save(List<Tree> trees) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        var instancePOs = instanceStore.loadForest(ids, context);
        return instancePOs.stream()
                .map(instancePO -> new Tree(instancePO.getId(), instancePO.getVersion(), instancePO.getNextNodeId(), instancePO.getData()))
                .toList();
    }

    @Override
    public void remove(List<Long> ids) {
    }

}
