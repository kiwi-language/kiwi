package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.util.NncUtils;

@EntityType("任务状态")
public enum JobState {

    RUNNABLE(1),
    RUNNING(2),
    FINISHED(9);

    private final int code;

    JobState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static JobState getByCode(int code) {
        return NncUtils.findRequired(values(), e -> e.code == code);
    }

}
