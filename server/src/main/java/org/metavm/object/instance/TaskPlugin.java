package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.task.ShadowTask;
import org.metavm.task.Task;

import java.util.ArrayList;

public class TaskPlugin implements ContextPlugin {

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        return false;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        var tasks = new ArrayList<Task>();
        for (VersionRT insert : change.inserts()) {
            var inst = context.get(insert.id());
            if(inst.getMappedEntity() instanceof Task task) {
                tasks.add(task);
            }
        }
        if(!tasks.isEmpty())
            ShadowTask.saveShadowTasksHook.accept(context.getAppId(), tasks);
    }
}
