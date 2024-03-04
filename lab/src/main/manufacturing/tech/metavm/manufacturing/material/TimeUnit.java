package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("时间单位")
public enum TimeUnit {
    @EnumConstant("年")
    YEAR,
    @EnumConstant("月")
    MONTH,
    @EnumConstant("日")
    DAY,
    @EnumConstant("小时")
    HOUR,
    @EnumConstant("分钟")
    MINUTE,
}
