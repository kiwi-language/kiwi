package org.metavm.system;

import org.metavm.system.persistence.IdSequenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultBlockRepository implements BlockRepository {

    public static final Logger logger = LoggerFactory.getLogger("block");

    public static final long INITIAL = 1000000000L;
    public static final long BLOCK_SIZE = 1000;

    private final IdSequenceMapper idSequenceMapper;

    public DefaultBlockRepository(IdSequenceMapper idSequenceMapper) {
        this.idSequenceMapper = idSequenceMapper;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Block allocate() {
        var next = idSequenceMapper.selectNextId();
        if(next == null) {
            next = INITIAL;
            idSequenceMapper.insert(INITIAL + BLOCK_SIZE);
        }
        else
            idSequenceMapper.incrementNextId(BLOCK_SIZE);
        return new Block(next, next + BLOCK_SIZE);
    }
}
