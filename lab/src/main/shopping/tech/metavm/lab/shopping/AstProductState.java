package tech.metavm.lab.shopping;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("商品状态")
public enum AstProductState {

    @EnumConstant("正常")
    NORMAL(0),

    @EnumConstant("下架")
    OFF_THE_SHELF(1)
    ;

    @EntityField("编号")
    private final int code;

    AstProductState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
