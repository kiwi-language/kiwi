package org.metavm.object.type;

import org.metavm.entity.EntityType;
import org.metavm.object.type.rest.dto.ClassSourceCodes;
import org.metavm.util.NncUtils;

@EntityType
public enum ClassSource {
    BUILTIN(ClassSourceCodes.BUILTIN),
    COMPILATION(ClassSourceCodes.COMPILATION),
    RUNTIME(ClassSourceCodes.RUNTIME);

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
