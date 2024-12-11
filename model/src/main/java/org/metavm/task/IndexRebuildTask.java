package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Instance;

import java.util.List;

@Entity
public class IndexRebuildTask extends ScanTask {

    protected IndexRebuildTask() {
        super("Index rebuild");
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        batch.forEach(Instance::incVersion);
    }

}
