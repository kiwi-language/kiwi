package org.metavm.object.instance;

import org.metavm.entity.Tree;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class WalTreeSource implements TreeSource {

    private final WAL wal;

    public WalTreeSource(WAL wal) {
        this.wal = wal;
    }

    @Override
    public void save(List<Tree> trees) {

    }

    @Override
    public List<Tree> load(Collection<Long> ids, IInstanceContext context) {
        return NncUtils.mapAndFilter(
                ids,
                id -> NncUtils.get(wal.get(id), InstancePO::toTree),
                Objects::nonNull
        );
    }

    @Override
    public void remove(List<Long> ids) {

    }
}
