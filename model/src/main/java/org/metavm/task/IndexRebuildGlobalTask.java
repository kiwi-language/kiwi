package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.application.Application;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Hooks;

import java.util.function.Consumer;

@Wire(31)
@Entity
@Slf4j
public class IndexRebuildGlobalTask extends GlobalTask {

    public IndexRebuildGlobalTask(Id id) {
        super(id, "IndexRebuildGlobalTask");
    }

    @Override
    protected void processApplication(Application application) {
        Hooks.CREATE_INDEX_IF_NOT_EXISTS.accept(application.getTreeId());
        Hooks.CREATE_INDEX_REBUILD_TASK.accept(application.getTreeId());
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
