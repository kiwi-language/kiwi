package org.metavm.system;

import java.util.function.LongConsumer;

public record IdRange(long min, long max) {

    public void forEach(LongConsumer action) {
        for (long id = min; id < max; id++) {
            action.accept(id);
        }
    }

}
