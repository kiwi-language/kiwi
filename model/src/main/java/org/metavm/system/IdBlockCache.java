package org.metavm.system;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.metavm.util.NncUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public class IdBlockCache {

    private final Function<Long, BlockRT> getById;
    private final ConcurrentSkipListMap<Long, BlockRT> index = new ConcurrentSkipListMap<>();

    private final LoadingCache<Long, BlockRT> cache;

    private void onRemoveFromCache(BlockRT block) {
        index.remove(block.getStart());
    }

    public IdBlockCache(long maxSize, Function<Long, BlockRT> getById) {
        this.getById = getById;
        //noinspection DataFlowIssue
        cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .removalListener(notification -> onRemoveFromCache((BlockRT) notification.getValue()))
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull BlockRT load(@NotNull Long key) {
                        return getById.apply(key);
                    }
                });
    }

    private void addToCache(BlockRT block) {
        cache.put(block.getId(), block);
        index.put(block.getStart(), block);
    }

    public BlockRT getById(long id) {
        BlockRT block = NncUtils.get(index.floorEntry(id), Map.Entry::getValue);
        if (block == null || !block.contains(id)) {
            block = this.getById.apply(id);
            NncUtils.requireTrue(block != null && block.contains(id),
                    () -> "Can not find a block containing id: " + id
            );
            addToCache(block);
        }
        return block;
    }

}
