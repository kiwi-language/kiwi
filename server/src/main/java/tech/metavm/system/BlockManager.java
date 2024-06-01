package tech.metavm.system;

import org.springframework.stereotype.Component;
import tech.metavm.system.persistence.BlockMapper;
import tech.metavm.system.persistence.BlockPO;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.util.NncUtils;

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
