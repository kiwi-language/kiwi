package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.object.type.rest.dto.ClassSourceCodes;
import tech.metavm.util.NncUtils;

@EntityType("Class来源")
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
