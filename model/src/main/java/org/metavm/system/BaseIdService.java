package org.metavm.system;

import org.metavm.entity.EntityIdProvider;
import org.metavm.object.instance.core.Id;
import org.metavm.util.ContextUtil;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

public abstract class BaseIdService implements EntityIdProvider {
    public static final int CACHE_CAPACITY = 100000;
    protected final BlockSource blockSource;
    protected final IdBlockCache cache;

    public BaseIdService(BlockSource blockSource) {
        this.blockSource = blockSource;
        cache = new IdBlockCache(
                CACHE_CAPACITY,
                this::loadBlock
        );
    }

    protected BlockRT loadBlock(long point) {
        try (var ignored = ContextUtil.getProfiler().enter("IdService.loadBlock")) {
            return NncUtils.requireNonNull(
                    blockSource.getContainingBlock(point),
                    () -> new InternalException("Can not find a block containing id " + point)
            );
        }
    }

    public BlockRT getBydId(Id id) {
        return cache.getById(id.getTreeId());
    }


}
