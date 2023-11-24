package tech.metavm.object.type.websocket;

import javax.annotation.Nullable;
import java.util.List;

public interface MetaChangeQueue {

    void notifyTypeChange(long tenantId, long version, List<Long> typeIds, @Nullable String triggerClientId);

}
