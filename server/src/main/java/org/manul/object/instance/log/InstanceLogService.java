package org.manul.object.instance.log;

import org.manul.entity.DefContext;
import org.manul.object.instance.IInstanceStore;
import org.manul.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface InstanceLogService {

    void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId, DefContext defContext);

    void createSearchSyncTask(long appId, Collection<Id> idsToIndex, Collection<Id> idsToRemove, DefContext defContext, boolean migrating);
}
