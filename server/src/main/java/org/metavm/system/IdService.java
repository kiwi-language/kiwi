package org.metavm.system;

import org.metavm.entity.EntityIdProvider;
import org.metavm.object.instance.core.TypeId;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeCategory;
import org.metavm.system.persistence.BlockMapper;
import org.metavm.util.ContextUtil;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.metavm.object.type.IdConstants.DEFAULT_BLOCK_SIZE;

@Component
public class IdService extends BaseIdService implements EntityIdProvider {

    public static final int MAX_ALLOCATION_RECURSION_DEPTH = 3;

    private final RegionManager regionManager;

    public IdService(BlockMapper blockMapper, RegionManager regionManager) {
        super(new StoreBlockSource(blockMapper));
        this.regionManager = regionManager;
    }

    private Map<TypeId, BlockRT> getActiveBlockMap(long appId, Collection<? extends Type> types) {
        try (var ignored = ContextUtil.getProfiler().enter("IdService.getActiveBlockMap")) {
            List<BlockRT> blocks = blockSource.getActive(NncUtils.map(types, t -> ((ClassType) t).resolve().getTreeId()));
            Map<TypeId, BlockRT> result = NncUtils.toMap(blocks, BlockRT::getTypeId);
            List<Type> residualTypes = NncUtils.exclude(types, t -> result.containsKey(t.getTypeId()));
            if (!residualTypes.isEmpty()) {
                createBlocks(appId, residualTypes).forEach(block -> result.put(block.getTypeId(), block));
            }
            return result;
        }
    }

    private List<BlockRT> createBlocks(long appId, List<Type> types) {
        Map<TypeCategory, List<TypeId>> category2types = NncUtils.toMultiMap(types, Type::getCategory, Type::getTypeId);
        List<BlockRT> blocks = new ArrayList<>();
        category2types.forEach(((typeCategory, typeIds) ->
                blocks.addAll(createBlocks(appId, typeCategory, typeIds))
        ));
        return blocks;
    }

    private List<BlockRT> createBlocks(long appId, TypeCategory typeCategory, Collection<TypeId> typeIds) {
        try (var ignored = ContextUtil.getProfiler().enter("IdService.createBlocks")) {
            RegionRT region = regionManager.get(typeCategory);
            if (region == null) {
                throw new InternalException("No id region defined for type category: " + typeCategory);
            }
            long id = region.getNext();
            List<BlockRT> blocks = new ArrayList<>();
            for (var typeId : typeIds) {
                BlockRT block = newBlock(id++, appId, typeId, id++);
                id += block.getSize();
                blocks.add(block);
            }
            regionManager.inc(typeCategory, id - region.getNext());
            blockSource.create(blocks);
            return blocks;
        }
    }

    private BlockRT newBlock(long id, long appId, TypeId typeId, long start) {
        return new BlockRT(
                id,
                appId,
                typeId,
                start,
                start + DEFAULT_BLOCK_SIZE,
                start
        );
    }

    private void updateBlocks(Collection<BlockRT> blocks) {
        try (var ignored = ContextUtil.getProfiler().enter("IdService.updateBlocks")) {
            blockSource.update(blocks);
        }
    }

    @Transactional
    public Long allocate(long appId, Type type) {
        var result = allocate(appId, Map.of(type, 1));
        return result.values().iterator().next().get(0);
    }

    @Transactional
    public Map<Type, List<Long>> allocate(long appId, Map<? extends Type, Integer> typeId2count) {
        return allocate0(appId, typeId2count, 0);
    }

    private Map<Type, List<Long>> allocate0(long appId, Map<? extends Type, Integer> typeId2count, int depth) {
        if (depth > MAX_ALLOCATION_RECURSION_DEPTH) {
            throw new InternalException("Allocation recursion depth exceeds maximum: "
                    + MAX_ALLOCATION_RECURSION_DEPTH);
        }
        Map<TypeId, BlockRT> activeBlockMap = getActiveBlockMap(appId, typeId2count.keySet());
        Map<Type, List<Long>> result = new HashMap<>();
        Map<Type, Integer> residual = new HashMap<>();

        typeId2count.forEach((type, count) -> {
            BlockRT block = activeBlockMap.get(type.getTypeId());
            NncUtils.requireNonNull(block, "Active block not found for type: " + type);
            int allocateCount = Math.min(count, block.available());
            result.put(type, block.allocate(allocateCount));
            if (allocateCount < count) {
                residual.put(type, count - allocateCount);
            }
        });
        updateBlocks(activeBlockMap.values());
        if (!residual.isEmpty()) {
            allocate0(appId, residual, depth + 1).forEach((typeId, ids) ->
                    result.compute(typeId, (k, old) -> NncUtils.union(old, ids))
            );
        }
        return result;
    }

}
