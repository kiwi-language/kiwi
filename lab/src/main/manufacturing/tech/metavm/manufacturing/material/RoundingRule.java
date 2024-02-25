package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("舍入规则")
public enum RoundingRule {
    @EnumConstant("四舍五入")
    ROUND_HALF_UP(),
    @EnumConstant("银行家舍入")
    ROUND_HALF_EVEN(),
    @EnumConstant("向上取整")
    ROUND_UP(),
    @EnumConstant("向下取整")
    ROUND_DOWN(),

}
