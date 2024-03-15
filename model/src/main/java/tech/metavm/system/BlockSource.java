package tech.metavm.system;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;

import java.util.Collection;
import java.util.List;

public interface BlockSource {

    BlockRT getContainingBlock(long id);

    List<BlockRT> getActive(List<Long> typeIds);

    void create(Collection<BlockRT> blocks);

    void update(Collection<BlockRT> blocks);


}
