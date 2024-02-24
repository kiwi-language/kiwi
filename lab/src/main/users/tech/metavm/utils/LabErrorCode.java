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

    NOT_A_MEMBER_OF_THE_APP(607, "用户未加入应用无法进入"),

    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "验证码发送太频繁，请稍后再试"),

    INVALID_EMAIL_ADDRESS(615, "邮箱地址错误"),

    INCORRECT_VERIFICATION_CODE(611, "验证码错误"),

    USER_NOT_FOUND(604, "用户(id:{})不存在"),

    INVITATION_ALREADY_ACCEPTED(612, "邀请已经接受"),

    USER_NOT_LOGGED_IN(614, "用户未登录"),

    CURRENT_USER_NOT_APP_ADMIN(1002, "您不是管理员，无法执行该操作"),

    CURRENT_USER_NOT_APP_OWNER(1002, "您不是应用所有者，无法执行该操作"),

    ILLEGAL_ACCESS(413, "无权限访问"),

    APPLICATION_NOT_SELECTED(414, "未选择应用"),

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
