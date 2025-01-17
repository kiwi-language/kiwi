package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.util.Utils;

@Entity
public enum FieldChangeKind {
    CREATION(1),
    TYPE_CHANGE(2),
    SUPER_CLASS_ADDED(3),

    ;

    private final int code;

    FieldChangeKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static FieldChangeKind fromCode(int code) {
        return Utils.findRequired(values(), fc -> fc.code == code);
    }

}
