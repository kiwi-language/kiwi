package org.manul.task;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.event.rest.dto.FunctionChangeEvent;
import org.manul.event.rest.dto.TypeChangeEvent;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Hooks;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Entity
@Wire(68)
public class PublishMetadataEventTask extends Task {

    private final List<String> changedTypeDefIds = new ArrayList<>();
    private final List<String> removedTypeDefIds = new ArrayList<>();
    private final List<String> changedFunctionIds = new ArrayList<>();
    private final List<String> removedFunctionIds = new ArrayList<>();
    private final long version;
    private final @Nullable String clientId;

    public PublishMetadataEventTask(Id id,
                                    Collection<String> changedTypeDefIds,
                                    Collection<String> removedTypeDefIds,
                                    Collection<String> changedFunctionIds,
                                    Collection<String> removedFunctionIds,
                                    long version,
                                    @Nullable String clientId) {
        super(id, "PublishMetadataEventTask");
        this.changedTypeDefIds.addAll(changedTypeDefIds);
        this.removedTypeDefIds.addAll(removedTypeDefIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
        this.version = version;
        this.clientId = clientId;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(new TypeChangeEvent(context.getAppId(),
                    version,
                    Utils.merge(changedTypeDefIds, removedTypeDefIds), clientId));
        }
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(
                    new FunctionChangeEvent(context.getAppId(), version, Utils.merge(changedFunctionIds, removedFunctionIds), clientId)
            );
        }
        return true;
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
