package tech.metavm.object.type.websocket;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MockMetaChangeQueue implements MetaChangeQueue {

    @Override
    public void notifyTypeChange(long tenantId, long version, List<Long> typeIds, @Nullable String triggerClientId) {

    }

}
