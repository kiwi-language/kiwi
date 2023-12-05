package tech.metavm.object.instance.log;

import org.springframework.stereotype.Component;
import tech.metavm.entity.IEntityContext;
import tech.metavm.event.rest.dto.TypeChangeEvent;
import tech.metavm.event.EventQueue;
import tech.metavm.object.version.Version;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class VersionHandler implements LogHandler<Version> {

    private final EventQueue eventQueue;

    public VersionHandler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public Class<Version> getEntityClass() {
        return Version.class;
    }

    @Override
    public void process(List<Version> created, @Nullable String clientId, IEntityContext context) {
        if(!created.isEmpty()) {
            long maxVersion = 0L;
            Set<Long> typeIds = new HashSet<>();
            for (Version version : created) {
                maxVersion = Math.max(maxVersion, version.getVersion());
                typeIds.addAll(version.getRemovedTypeIds());
                typeIds.addAll(version.getChangeTypeIds());
            }
            eventQueue.publishAppEvent(
                    new TypeChangeEvent(context.getAppId(), maxVersion, new ArrayList<>(typeIds), clientId)
            );
        }
    }
}
