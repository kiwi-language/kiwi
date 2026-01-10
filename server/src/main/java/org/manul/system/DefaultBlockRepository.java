package org.manul.system;

import org.manul.context.Component;
import org.manul.context.sql.TransactionIsolation;
import org.manul.context.sql.TransactionPropagation;
import org.manul.context.sql.Transactional;
import org.manul.system.persistence.IdSequenceMapper;

@Component(module = "persistent")
public class DefaultBlockRepository implements BlockRepository {

    public static final long INITIAL = 1000000000L;
    public static final long BLOCK_SIZE = 1000;

    private final IdSequenceMapper idSequenceMapper;

    public DefaultBlockRepository(IdSequenceMapper idSequenceMapper) {
        this.idSequenceMapper = idSequenceMapper;
    }

    @Override
    @Transactional(isolation = TransactionIsolation.SERIALIZABLE, propagation = TransactionPropagation.REQUIRES_NEW)
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
