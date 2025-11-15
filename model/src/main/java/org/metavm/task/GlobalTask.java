package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.application.Application;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.List;
import java.util.function.Consumer;

@Wire(35)
@Entity
public abstract class GlobalTask extends EntityScanTask {

    protected GlobalTask(Id id, String title) {
        super(id, title, Application.class);
    }

    @Override
    protected void processModels(IInstanceContext context, List<Object> applications) {
        for (var entity : applications) {
            processApplication((Application) entity);
        }
    }

    protected abstract void processApplication(Application application);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
