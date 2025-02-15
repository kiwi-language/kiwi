package org.metavm.object.instance.log;

import org.metavm.entity.DefContext;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface InstanceLogService {

    void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId, DefContext defContext);

    void createSearchSyncTask(long appId, Collection<Id> idsToIndex, Collection<Id> idsToRemove, DefContext defContext);
}
