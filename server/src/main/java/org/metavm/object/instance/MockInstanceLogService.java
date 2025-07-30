package org.metavm.object.instance;

import org.metavm.entity.DefContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.log.InstanceLogService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockInstanceLogService implements InstanceLogService {

    private final List<InstanceLog> logs = new ArrayList<>();

    @Override
    public void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId, DefContext defContext) {
        this.logs.addAll(logs);
    }

    @Override
    public void createSearchSyncTask(long appId, Collection<Id> idsToIndex, Collection<Id> idsToRemove, DefContext defContext, boolean migrating) {

    }

    public List<InstanceLog> getLogs() {
        return logs;
    }
}
