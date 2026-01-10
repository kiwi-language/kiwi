package org.manul.task;

import lombok.extern.slf4j.Slf4j;
import org.manul.api.Entity;
import org.manul.application.Application;
import org.manul.wire.Wire;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Hooks;

import java.util.List;
import java.util.function.Consumer;

@Wire(7)
@Slf4j
@Entity
public class RemoveAppTaskGroup extends TaskGroup {

    private Id appId;

    public RemoveAppTaskGroup(Id id, Id appId) {
        super(id);
        this.appId = appId;
    }

    @Override
    public List<Task> createTasks(IInstanceContext context) {
        var app = context.getEntity(Application.class, appId);
        return List.of(
                new ClearUsersTask(nextChildId(), String.format("ClearUsersTask(%s)", app.getName()), appId),
                new ClearInvitationTask(nextChildId(), String.format("ClearInvitationTask(%s)", app.getName()), appId)
        );
    }

    @Override
    protected void onCompletion(IInstanceContext context, IInstanceContext taskContext) {
        context.remove(context.get(appId));
        Hooks.DROP_TABLES.accept(appId.getTreeId());
        Hooks.DROP_INDICES.accept(appId.getTreeId());
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
