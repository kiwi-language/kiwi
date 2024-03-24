package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.Tree;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.KeyValue;
import tech.metavm.util.NncUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CacheTreeSource implements TreeSource {

    private final Cache cache;

    public CacheTreeSource(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void save(List<Tree> trees) {
        List<KeyValue<Long, byte[]>> entries = new ArrayList<>();
        for (var tree : trees) {
            entries.add(new KeyValue<>(tree.id(), tree.data()));
        }
        cache.batchAdd(entries);
    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        var bytes = cache.batchGet(ids);
        var trees = new ArrayList<Tree>();
        NncUtils.biForEach(ids, bytes, (id, bs) -> {
            if (bs != null) {
                var input = new InstanceInput(new ByteArrayInputStream(bs));
                trees.add(new Tree(id, input.readLong(), input.readInt(), bs));
            }
        });
        return trees;
    }

    @Override
    public void remove(List<Long> ids) {
        cache.batchRemove(ids);
    }
}
