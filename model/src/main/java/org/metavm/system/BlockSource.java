package org.metavm.system;

import java.util.Collection;
import java.util.List;

public interface BlockSource {

    BlockRT getContainingBlock(long id);

    List<BlockRT> getActive(List<Long> typeIds);

    void create(Collection<BlockRT> blocks);

    void update(Collection<BlockRT> blocks);


}
