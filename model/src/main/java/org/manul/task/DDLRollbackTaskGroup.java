package org.manul.task;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import java.util.List;
import java.util.function.Consumer;

@Wire(78)
@Entity
public class DDLRollbackTaskGroup extends TaskGroup {

    public DDLRollbackTaskGroup(Id id) {
        super(id);
    }

    @Override
    public List<Task> createTasks(IInstanceContext context) {
        return List.of(new DDLRollbackTask(nextChildId()));
    }

    @Override
    protected void onCompletion(IInstanceContext context, IInstanceContext taskContext) {

    }

    @Override
    public String getTitle() {
        return null;
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