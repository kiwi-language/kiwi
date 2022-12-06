package tech.metavm.object.instance.log;

import java.util.List;

public interface InstanceLogService {

    void asyncProcess(List<InstanceLog> logs);

    void process(List<InstanceLog> logs);
}
