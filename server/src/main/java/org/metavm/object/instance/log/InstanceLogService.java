package org.metavm.object.instance.log;

import org.metavm.entity.DefContext;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceLogService {

    void process(long appId, List<InstanceLog> logs, IInstanceStore instanceStore, List<Id> migrated, @Nullable String clientId, DefContext defContext);
}
