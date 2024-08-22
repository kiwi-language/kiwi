package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.event.rest.dto.FunctionChangeEvent;
import org.metavm.event.rest.dto.TypeChangeEvent;
import org.metavm.util.Hooks;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collection;

public class PublishMetadataEventTask extends Task {

    @ChildEntity
    private final ReadWriteArray<String> changedTypeDefIds = addChild(new ReadWriteArray<>(String.class), "changedTypeDefIds");
    @ChildEntity
    private final ReadWriteArray<String> removedTypeDefIds = addChild(new ReadWriteArray<>(String.class), "removedTypeDefIds");
    @ChildEntity
    private final ReadWriteArray<String> changedFunctionIds = addChild(new ReadWriteArray<>(String.class), "changedFunctionIds");
    @ChildEntity
    private final ReadWriteArray<String> removedFunctionIds = addChild(new ReadWriteArray<>(String.class), "removedFunctionIds");
    private final long version;
    private final @Nullable String clientId;

    public PublishMetadataEventTask(Collection<String> changedTypeDefIds,
                                    Collection<String> removedTypeDefIds,
                                    Collection<String> changedFunctionIds,
                                    Collection<String> removedFunctionIds,
                                    long version,
                                    @Nullable String clientId) {
        super("PublishMetadataEventTask");
        this.changedTypeDefIds.addAll(changedTypeDefIds);
        this.removedTypeDefIds.addAll(removedTypeDefIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
        this.version = version;
        this.clientId = clientId;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(new TypeChangeEvent(context.getAppId(),
                    version,
                    NncUtils.merge(changedTypeDefIds.toList(), removedTypeDefIds.toList()), clientId));
        }
        if(!changedTypeDefIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Hooks.PUBLISH_APP_EVENT.accept(
                    new FunctionChangeEvent(context.getAppId(), version, NncUtils.merge(changedFunctionIds.toList(), removedFunctionIds.toList()), clientId)
            );
        }
        return true;
    }
}
