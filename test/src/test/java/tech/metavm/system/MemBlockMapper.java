package tech.metavm.system;

import tech.metavm.system.persistence.BlockMapper;
import tech.metavm.system.persistence.BlockPO;
import tech.metavm.system.persistence.RangeInc;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemBlockMapper implements BlockMapper {

    private final Map<Long, BlockPO> map = new HashMap<>();
    private final Map<Long, Set<BlockPO>> typeId2blocks = new HashMap<>();

    @Override
    public List<BlockPO> selectByIds(Collection<Long> ids) {
        return NncUtils.mapAndFilter(
                ids,
                map::get,
                Objects::nonNull
        );
    }

    @Override
    public int batchInsert(List<BlockPO> records) {
        for (BlockPO blockPO : records) {
            NncUtils.requireNonNull(blockPO.getId());
            NncUtils.requireNonNull(blockPO.getTypeId());
            NncUtils.requireNonNull(blockPO.getStartId());
            NncUtils.requireNonNull(blockPO.getEndId());
            NncUtils.requireNonNull(blockPO.getActive());
            map.put(blockPO.getId(), blockPO);
            typeId2blocks.computeIfAbsent(blockPO.getTypeId(), k -> new IdentitySet<>())
                    .add(blockPO);
        }
        return records.size();
    }

    @Override
    public List<BlockPO> selectActive(Collection<Long> typeIds) {
        return NncUtils.flatMapAndFilter(
                typeIds,
                typeId2blocks::get,
                block -> Boolean.TRUE.equals(block.getActive())
        );
    }

    @Override
    public void batchUpdate(Collection<BlockPO> records) {
        for (BlockPO record : records) {
            BlockPO existing = map.get(record.getId());
            NncUtils.requireNonNull(existing);
            typeId2blocks.get(existing.getTypeId()).remove(existing);
        }
    }

    @Override
    public BlockPO selectContaining(long point) {
        for (BlockPO blockPO : map.values()) {
            if(blockPO.getStartId() <= point && blockPO.getEndId() > point) {
                return blockPO;
            }
        }
        return null;
    }

    @Override
    public void increaseNextId(long id, long inc) {
        BlockPO blockPO = map.get(id);
        blockPO.setNextId(blockPO.getNextId() + inc);
        NncUtils.requireTrue(blockPO.getNextId() <= blockPO.getEndId(), "超出边界");
    }

    @Override
    public void inc(List<RangeInc> incs) {
        for (RangeInc inc : incs) {
            increaseNextId(inc.getId(), inc.getInc());
        }
    }

    @Override
    public List<BlockPO> selectActiveRanges(long appId, Collection<Long> typeIds) {
        return NncUtils.flatMapAndFilter(
                typeIds,
                typeId2blocks::get,
                block -> block.getAppId() == appId && Boolean.TRUE.equals(block.getActive())
        );
    }
}
