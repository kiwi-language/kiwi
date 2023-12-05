package tech.metavm.system;

import tech.metavm.system.persistence.BlockPO;

import java.util.Objects;

public class BlockUtils {


    public static BlockPO toPO(BlockRT blockRT) {
        return new BlockPO(
                blockRT.getId(),
                blockRT.getAppId(),
                blockRT.getTypeId(),
                blockRT.getStart(),
                blockRT.getEnd(),
                blockRT.getNext(),
                blockRT.isActive()
        );
    }

    public static BlockRT createBlockRT(BlockPO blockPO) {
        return new BlockRT(
                Objects.requireNonNull(blockPO.getId()),
                blockPO.getAppId(),
                blockPO.getTypeId(),
                blockPO.getStartId(),
                blockPO.getEndId(),
                blockPO.getNextId()
        );
    }
}
