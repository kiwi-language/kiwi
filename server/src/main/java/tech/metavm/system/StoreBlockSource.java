package tech.metavm.system;

import tech.metavm.object.instance.core.TypeId;
import tech.metavm.system.persistence.BlockMapper;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public class StoreBlockSource implements BlockSource{

    private final BlockMapper blockMapper;

    public StoreBlockSource(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Override
    public BlockRT getContainingBlock(long id) {
        return BlockUtils.createBlockRT(blockMapper.selectContaining(id));
    }

    @Override
    public List<BlockRT> getActive(List<Long> typeIds) {
        var blockPOs = blockMapper.selectActive(typeIds);
        return NncUtils.map(blockPOs, BlockUtils::createBlockRT);
    }

    @Override
    public void create(Collection<BlockRT> blocks) {
        blockMapper.batchInsert(NncUtils.map(blocks, BlockUtils::toPO));
    }

    @Override
    public void update(Collection<BlockRT> blocks) {
        blockMapper.batchUpdate(NncUtils.map(blocks, BlockUtils::toPO));
    }
}
