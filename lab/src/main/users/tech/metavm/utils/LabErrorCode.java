package tech.metavm.utils;

import tech.metavm.entity.EntityType;

@EntityType("错误代码")
public enum LabErrorCode {

    REENTERING_APP(608, "请先退出当前应用再进行操作"),

    NOT_IN_APP(609, "当前未进入任何APP"),

    ALREADY_AN_ADMIN(613, "用户'{}'已经是管理员"),

    USER_NOT_ADMIN(614, "用户'{}'不是管理员"),

    ALREADY_JOINED_APP(611, "用户'{}'已加入当前应用"),

    CAN_NOT_EVICT_APP_OWNER(1001, "应用所有人无法退出应用"),

    TOO_MANY_LOGIN_ATTEMPTS(613, "登录尝试次数过多，请稍后再试"),

    LOGIN_NAME_NOT_FOUND(602, "账号'{}'不存在"),

    LOGIN_FAILED(603, "登录失败"),

    ;

    private final int code;
    private final String message;


    LabErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

}
