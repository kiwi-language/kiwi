package org.metavm.task;

import org.metavm.util.Utils;

public enum TaskState {

    RUNNABLE(1),
    RUNNING(2),
    COMPLETED(9),
    FAILED(10),
    ;

    private final int code;

    TaskState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static TaskState fromCode(int code) {
        return Utils.findRequired(values(), e -> e.code == code);
    }

}
