package tech.metavm.util;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.IndexDef;
import tech.metavm.entity.ValueType;

@ValueType("巴")
public record Bar(@EntityField(value = "编号", asTitle = true) String code) {

    public Bar(String code) {
        this.code = code;
    }

}
