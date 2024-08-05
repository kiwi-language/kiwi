package org.metavm.util;

import org.metavm.api.Configuration;
import org.metavm.task.DDLTask;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class Config {

    @Value("${metavm.ddl.disable-task-delay}")
    public void setDelay(boolean disableDDLTaskDelay) {
        DDLTask.DISABLE_DELAY = disableDDLTaskDelay;
    }

}
