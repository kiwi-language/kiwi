package org.metavm.object.instance;

import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.log.InstanceLogService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MockInstanceLogService implements InstanceLogService {

    private final List<InstanceLog> logs = new ArrayList<>();

    @Override
    public void process(List<InstanceLog> logs, IInstanceStore instanceStore, @Nullable String clientId) {
        this.logs.addAll(logs);
    }

    public List<InstanceLog> getLogs() {
        return logs;
    }
}
