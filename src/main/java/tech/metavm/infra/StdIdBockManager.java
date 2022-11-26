package tech.metavm.infra;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.infra.persistence.BlockMapper;
import tech.metavm.infra.persistence.BlockPO;
import tech.metavm.object.meta.IdConstants;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static tech.metavm.object.meta.IdConstants.*;

@Component
public class StdIdBockManager {

    private final BlockMapper blockMapper;

    public StdIdBockManager(BlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    @Transactional
    public void initialize() {
        Set<Long> existingIds =
                NncUtils.mapToIds(blockMapper.selectByIds(NncUtils.mapToIds(VALUES)));
        List<BlockPO> toInserts =
                NncUtils.filter(VALUES, block -> !existingIds.contains(block.getId()));
        if(NncUtils.isNotEmpty(toInserts)) {
            blockMapper.batchInsert(toInserts);
        }
    }

    private final static List<BlockPO> VALUES = new ArrayList<>();

    static {
        create(1L, IdConstants.TYPE.ID, TYPE_BASE);
        create(2L, IdConstants.TYPE.ID, NULLABLE_TYPE_BASE);
        create(3L, IdConstants.TYPE.ID, ARRAY_TYPE_BASE);
        create(11L, IdConstants.FIELD.ID, FIELD_BASE, 10);
        create(12L, IdConstants.UNIQUE_CONSTRAINT.ID, UNIQUE_CONSTRAINT_BASE);
        create(13L, IdConstants.CHECK_CONSTRAINT.ID, CHECK_CONSTRAINT_BASE);
    }

    public static void create(long id, long typeId, long start) {
        create(id, typeId, start, 1);
    }

    public static void create(long id, long typeId, long start, int sizeMultiplier) {
        long size = DEFAULT_BLOCK_SIZE * sizeMultiplier;
        BlockPO stdBlock = new BlockPO(
                id,
                -1L,
                typeId,
                start,
                start + size,
                start + size,
                false
        );
        VALUES.add(stdBlock);
    }

}
