package tech.metavm.infra;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.infra.persistence.BlockMapper;
import tech.metavm.infra.persistence.BlockPO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.object.meta.IdConstants.DEFAULT_BLOCK_SIZE;

@Component
public class IdService implements EntityIdProvider {

    public static final int MAX_ALLOCATION_RECURSION_DEPTH = 3;

    public static final int CACHE_CAPACITY = 100000;

    private final BlockMapper blockMapper;

    private final RegionManager regionManager;

    private final IdBlockCache cache;

    public IdService(BlockMapper blockMapper, RegionManager regionManager) {
        this.blockMapper = blockMapper;
        this.regionManager = regionManager;
        cache = new IdBlockCache(
                CACHE_CAPACITY,
                this::loadBlock
        );
    }

    private BlockRT loadBlock(long point) {
        BlockPO blockPO = NncUtils.requireNonNull(
                blockMapper.selectByPoint(point),
                () -> new InternalException("Can not find a block containing id " + point)
        );
        return new BlockRT(blockPO);
    }

    public BlockRT getBydId(long id) {
        return cache.getById(id);
    }

    private Map<Long, BlockRT> getActiveBlockMap(long tenantId, Collection<Type> types) {
        List<BlockRT> blocks = NncUtils.map(
                blockMapper.selectActive(NncUtils.map(types, Entity::getId)),
                BlockRT::new
        );
        Map<Long, BlockRT> result = NncUtils.toMap(blocks, BlockRT::getTypeId);
        List<Type> residualTypes = NncUtils.exclude(types, t -> result.containsKey(t.getId()));
        if(!residualTypes.isEmpty()) {
            createBlocks(tenantId, residualTypes).forEach(block -> result.put(block.getTypeId(), block));
        }
        return result;
    }

    private List<BlockRT> createBlocks(long tenantId, List<Type> types) {
        Map<TypeCategory, List<Long>> category2types = NncUtils.toMultiMap(types, Type::getCategory, Entity::getId);
        List<BlockRT> blocks = new ArrayList<>();
        category2types.forEach(((typeCategory, typeIds) ->
            blocks.addAll(createBlocks(tenantId, typeCategory, typeIds))
        ));
        return blocks;
    }

    private List<BlockRT> createBlocks(long tenantId, TypeCategory typeCategory, Collection<Long> typeIds) {
        RegionRT region = regionManager.getRegion(typeCategory);
        if(region == null) {
            throw new InternalException("No id region defined for type category: " + typeCategory);
        }
        long id = region.getNext();
        List<BlockRT> blocks = new ArrayList<>();
        for (Long typeId : typeIds) {
            BlockRT block = newBlock(id++, tenantId, typeId, id++);
            id += block.getSize();
            blocks.add(block);
        }
        regionManager.inc(typeCategory, id - region.getNext());
        blockMapper.batchInsert(NncUtils.map(blocks, BlockRT::toPO));
        return blocks;
    }

    private BlockRT newBlock(long id, long tenantId, long typeId, long start) {
        return new BlockRT(
                id,
                tenantId,
                typeId,
                start,
                start + DEFAULT_BLOCK_SIZE
        );
    }

    private void updateBlocks(Collection<BlockRT> blocks) {
        blockMapper.batchUpdate(NncUtils.map(blocks, BlockRT::toPO));
    }

    public Long allocate(long tenantId, Type type) {
        Map<Type, List<Long>> result =
                allocate(tenantId, Map.of(type, 1));
        return result.values().iterator().next().get(0);
    }

    @Override
    public long getTypeId(long id) {
        return getBydId(id).getTypeId();
    }

    @Transactional
    public Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count) {
        return allocate0(tenantId, typeId2count, 0);
    }

    private Map<Type, List<Long>> allocate0(long tenantId, Map<Type, Integer> typeId2count, int depth) {
        if(depth > MAX_ALLOCATION_RECURSION_DEPTH) {
            throw new InternalException("Allocation recursion depth exceeds maximum: "
                    + MAX_ALLOCATION_RECURSION_DEPTH);
        }
        Map<Long, BlockRT> activeBlockMap = getActiveBlockMap(tenantId, typeId2count.keySet());
        Map<Type, List<Long>> result = new HashMap<>();
        Map<Type, Integer> residual = new HashMap<>();

        typeId2count.forEach((type, count) -> {
            BlockRT block = activeBlockMap.get(type.getId());
            NncUtils.requireNonNull(block, "Active block not found for type: " + type);
            Integer allocateCount = Math.min(count, block.available());
            result.put(type, block.allocate(count));
            if(allocateCount < count) {
                residual.put(type, count - allocateCount);
            }
        });
        updateBlocks(activeBlockMap.values());
        if(!residual.isEmpty()) {
            allocate0(tenantId, residual, depth + 1).forEach((typeId, ids) ->
                result.compute(typeId, (k, old) -> NncUtils.union(old, ids))
            );
        }
        return result;
    }

}
