package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.util.NncUtils;

@Entity
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

    public static ClassSource fromCode(int code) {
        return NncUtils.findRequired(values(), source -> source.code == code);
    }

}
