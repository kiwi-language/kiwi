package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.util.NncUtils;

@EntityType("Class来源")
public enum ClassSource {
    BUILTIN(1),
    COMPILATION(2),
    RUNTIME(3);

    private final int code;

    ClassSource(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ClassSource getByCode(int code) {
        return NncUtils.findRequired(values(), source -> source.code == code);
    }

}
