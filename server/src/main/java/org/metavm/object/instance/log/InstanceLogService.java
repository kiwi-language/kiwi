package org.metavm.object.instance.log;

import org.metavm.object.instance.IInstanceStore;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceLogService {

    void process(List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId);
}
