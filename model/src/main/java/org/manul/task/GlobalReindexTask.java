package org.manul.task;

import org.manul.api.Entity;
import org.manul.application.Application;
import org.manul.object.instance.core.Id;
import org.manul.util.Hooks;
import org.manul.wire.Wire;

@Wire(203)
@Entity
public class GlobalReindexTask extends GlobalTask {
    public GlobalReindexTask(Id id, String title) {
        super(id, title);
    }

    @Override
    protected void processApplication(Application application) {
        Hooks.CREATE_REINDEX_TASK.accept(application.getTreeId());
    }
}
