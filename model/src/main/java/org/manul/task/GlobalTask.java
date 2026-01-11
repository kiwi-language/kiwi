package org.manul.task;

import org.manul.api.Entity;
import org.manul.application.Application;
import org.manul.wire.Wire;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

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
