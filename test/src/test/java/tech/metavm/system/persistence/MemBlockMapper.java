package tech.metavm.system.persistence;

import tech.metavm.util.NncUtils;

import java.util.*;

public class MemBlockMapper implements BlockMapper {

    private final Map<Long, BlockPO> blocks = new HashMap<>();
    private final Map<Long, BlockPO> activeBlocks = new HashMap<>();
    private final TreeMap<Long, BlockPO> treeMap = new TreeMap<>();

    @Override
    public List<BlockPO> selectByIds(Collection<Long> ids) {
        return makeCopies(NncUtils.mapAndFilter(ids, blocks::get, Objects::nonNull));
    }

    @Override
    public int batchInsert(List<BlockPO> records) {
        records.forEach(block -> {
            block = block.copy();
            blocks.put(block.getId(), block);
            treeMap.put(block.getEndId(), block);
            if (block.getActive()) {
                if(block.getTypeId() == 50285L)
                    System.out.println("Caught");
                activeBlocks.put(block.getTypeId(), block);
            }
        });
        return blocks.size();
    }

    @Override
    public List<BlockPO> selectActive(Collection<Long> typeIds) {
        return makeCopies(NncUtils.mapAndFilter(typeIds, activeBlocks::get, Objects::nonNull));
    }

    @Override
    public void batchUpdate(Collection<BlockPO> records) {
        for (BlockPO record : records) {
            var block = blocks.get(record.getId());
//            if (block != null) {
                block.setNextId(record.getNextId());
                block.setActive(record.getActive());
                if (block.getActive()) {
                    if(block.getTypeId() == 50285L)
                        System.out.println("Caught");
                    activeBlocks.put(block.getTypeId(), block);
                }
                else if (activeBlocks.get(block.getTypeId()) == block)
                    activeBlocks.remove(block.getTypeId());
//            }
        }
    }

    @Override
    public BlockPO selectContaining(long point) {
        return NncUtils.get(treeMap.higherEntry(point), e -> e.getValue().copy());
    }

    @Override
    public void increaseNextId(long id, long inc) {
        var block = blocks.get(id);
//        if (block != null)
        block.setNextId(block.getNextId() + inc);
    }

    @Override
    public void inc(List<RangeInc> incs) {
        for (RangeInc inc : incs) {
            var block = blocks.get(inc.getId());
//            if (block != null) {
                block.setNextId(block.getNextId() + inc.getInc());
                if (inc.isDeactivating() && block.getActive()) {
                    activeBlocks.remove(block.getTypeId());
                    block.setActive(false);
                }
//            }
        }
    }

    @Override
    public List<BlockPO> selectActiveRanges(long appId, Collection<Long> typeIds) {
        return makeCopies(NncUtils.mapAndFilter(typeIds, activeBlocks::get, blk -> blk.getAppId() == appId));
    }

    private List<BlockPO> makeCopies(List<BlockPO> blocks) {
        return NncUtils.map(blocks, BlockPO::copy);
    }

    public MemBlockMapper copy() {
        var copy = new MemBlockMapper();
        copy.batchInsert(new ArrayList<>(blocks.values()));
        return copy;
    }

}
