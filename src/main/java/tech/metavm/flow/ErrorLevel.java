package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("错误级别")
public enum ErrorLevel {
    @EnumConstant("警告")
    WARNING,
    @EnumConstant("错误")
    ERROR
}
