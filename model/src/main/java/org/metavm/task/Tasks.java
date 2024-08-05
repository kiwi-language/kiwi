package org.metavm.task;

import org.metavm.util.Constants;

public class Tasks {

    public static Task delay(Task task) {
        if(Constants.SESSION_TIMEOUT != -1)
            task.setStartAt(System.currentTimeMillis() + Constants.SESSION_TIMEOUT + 100);
        return task;
    }

}
