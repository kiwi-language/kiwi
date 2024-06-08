package tech.metavm.utils;

import tech.metavm.entity.EntityType;

@EntityType
public enum LabErrorCode {

    REENTERING_APP(608, "Please exit the current application before operating"),

    NOT_IN_APP(609, "Not in any APP currently"),

    ALREADY_AN_ADMIN(613, "User '{}' is already an administrator"),

    USER_NOT_ADMIN(614, "User '{}' is not an administrator"),

    ALREADY_JOINED_APP(611, "User '{}' has already joined the current application"),

    CAN_NOT_EVICT_APP_OWNER(1001, "The owner of the application cannot exit the application"),

    TOO_MANY_LOGIN_ATTEMPTS(613, "Too many login attempts, please try again later"),

    LOGIN_NAME_NOT_FOUND(602, "Account '{}' does not exist"),

    LOGIN_FAILED(603, "Login failed"),

    NOT_A_MEMBER_OF_THE_APP(607, "User not joined in the application cannot enter"),

    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "Verification code sent too frequently, please try again later"),

    INVALID_EMAIL_ADDRESS(615, "Incorrect email address"),

    INCORRECT_VERIFICATION_CODE(611, "Incorrect verification code"),

    USER_NOT_FOUND(604, "User (id:{}) does not exist"),

    INVITATION_ALREADY_ACCEPTED(612, "Invitation has already been accepted"),

    USER_NOT_LOGGED_IN(614, "User not logged in"),

    CURRENT_USER_NOT_APP_ADMIN(1002, "You are not an administrator and cannot perform this operation"),

    CURRENT_USER_NOT_APP_OWNER(1002, "You are not the owner of the application and cannot perform this operation"),

    ILLEGAL_ACCESS(413, "No access permission"),

    APPLICATION_NOT_SELECTED(414, "No application selected"),

    COMPONENT_MATERIAL_LACK_VIRTUAL_BOM(415, "Virtual material BOM component requires virtual BOM"),

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