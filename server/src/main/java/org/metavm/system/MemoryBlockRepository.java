package org.metavm.system;

import org.metavm.context.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component(module = "memory")
public class MemoryBlockRepository implements BlockRepository {

    private final AtomicLong next = new AtomicLong(DefaultBlockRepository.INITIAL);

    @Override
    public Block allocate() {
        var min = next.getAndAdd(DefaultBlockRepository.BLOCK_SIZE);
        return new Block(min, min + DefaultBlockRepository.BLOCK_SIZE);
    }

    public MemoryBlockRepository copy() {
        var copy = new MemoryBlockRepository();
        copy.next.set(this.next.get());
        return copy;
    }

}
