package tech.metavm.system;

import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.system.persistence.BlockPO;
import tech.metavm.util.Constants;

import java.util.Objects;

public class BlockUtils {


    public static BlockPO toPO(BlockRT blockRT) {
        return new BlockPO(
                blockRT.getId(),
                blockRT.getAppId().getPhysicalId(),
                blockRT.getTypeId().tag().code(),
                blockRT.getTypeId().id(),
                blockRT.getStart(),
                blockRT.getEnd(),
                blockRT.getNext(),
                blockRT.isActive()
        );
    }

    public static BlockRT createBlockRT(BlockPO blockPO) {
        return new BlockRT(
                Objects.requireNonNull(blockPO.getId()),
                Constants.getAppId(blockPO.getAppId()),
                new TypeId(TypeTag.fromCode(blockPO.getTypeTag()), blockPO.getTypeId()),
                blockPO.getStartId(),
                blockPO.getEndId(),
                blockPO.getNextId()
        );
    }
}
