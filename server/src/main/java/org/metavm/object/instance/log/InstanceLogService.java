package org.metavm.object.instance.log;

import org.metavm.object.instance.log.InstanceLog;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceLogService {

    void process(List<InstanceLog> logs, @Nullable String clientId);
}
