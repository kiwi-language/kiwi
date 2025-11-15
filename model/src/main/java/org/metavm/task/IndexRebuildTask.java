package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.search.SearchSync;

import java.util.List;
import java.util.function.Consumer;

@Wire(41)
@Entity
@Slf4j
public class IndexRebuildTask extends ScanTask {

    public IndexRebuildTask(Id id) {
        super(id, "IndexRebuildTask");
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        var rootIds = batch.stream().filter(i -> i.isRoot() && !i.isValue())
                .map(Instance::getId).toList();
        SearchSync.sync(rootIds, List.of(), false, context);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
