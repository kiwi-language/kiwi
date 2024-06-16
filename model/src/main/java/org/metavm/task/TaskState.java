package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.util.NncUtils;

@EntityType
public enum TaskState {

    RUNNABLE(1),
    RUNNING(2),
    FINISHED(9);

    private final int code;

    TaskState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static TaskState getByCode(int code) {
        return NncUtils.findRequired(values(), e -> e.code == code);
    }

}
