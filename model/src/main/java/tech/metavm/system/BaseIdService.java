package tech.metavm.system;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

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
        return cache.getById(id.getPhysicalId());
    }

    @Override
    public TypeId getTypeId(Id id) {
        return getBydId(id).getTypeId();
    }


}
