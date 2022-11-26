package tech.metavm.infra;

import tech.metavm.util.NncUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class IdBlockCache {

    private final long maxSize;
    private final Function<Long, BlockRT> getById;
    private final TreeMap<Long, BlockRT> index = new TreeMap<>();

    private final LinkedHashMap<Long, BlockRT> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, BlockRT> eldest) {
            boolean shouldRemove = size() > maxSize;
            if(shouldRemove) {
                onRemoveFromCache(eldest.getValue());
                return true;
            }
            return false;
        }
    };

    private void onRemoveFromCache(BlockRT block) {
        index.remove(block.getStart());
    }

    public IdBlockCache(long maxSize, Function<Long, BlockRT> getById) {
        this.maxSize = maxSize;
        this.getById = getById;
    }

    private void addToCache(BlockRT block) {
        cache.put(block.getId(), block);
        index.put(block.getStart(), block);
    }

    public BlockRT getById(long id) {
        BlockRT block = NncUtils.get(index.floorEntry(id), Map.Entry::getValue);
        if(block == null || !block.contains(id)) {
            block = this.getById.apply(id);
            NncUtils.requireTrue(block != null && block.contains(id),
                    "Can not find a block that contains id: " + id);
            addToCache(block);
        }
        return block;
    }

}
