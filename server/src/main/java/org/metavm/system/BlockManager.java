package org.metavm.system;

import org.springframework.stereotype.Component;
import org.metavm.system.persistence.BlockMapper;
import org.metavm.system.persistence.BlockPO;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.util.NncUtils;

import java.util.List;

@Component
public class BlockManager {

    private final BlockMapper blockMapper;

    public BlockManager(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    public BlockDTO getContaining(long id) {
        var blockPO =  blockMapper.selectContaining(id);
        return toDTO(blockPO);
    }

    public List<BlockDTO> getActive(List<Long> typeIds) {
        return NncUtils.map(blockMapper.selectActive(typeIds), this::toDTO);
    }

    private BlockDTO toDTO(BlockPO blockPO) {
        return new BlockDTO(
                blockPO.getId(), blockPO.getAppId(),
                blockPO.getTypeTag(), blockPO.getTypeId(), blockPO.getStartId(),
                blockPO.getEndId(), blockPO.getNextId(), blockPO.getActive()
        );
    }

}
