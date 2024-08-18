package org.metavm.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IdGenerator.class);
    private final BlockRepository blockRepository;
    private volatile Block block = new Block(0, 0);

    public IdGenerator(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public List<Long> generate(int count) {
        var ids = new ArrayList<Long>();
        do {
            block.allocate(count - ids.size()).forEach(ids::add);
            if (ids.size() < count)
                nextBlock();
            else
                return ids;
        }
        while (true);
    }

    private void nextBlock() {
        if(block.hasNext())
            return;
        synchronized (this) {
            if(block.hasNext())
                return;
            try {
                block = blockRepository.allocate();
            }
            catch (PessimisticLockingFailureException e) {
                logger.info("Failed to allocate a block because of a serializable failure", e);
            }
        }
    }

}
