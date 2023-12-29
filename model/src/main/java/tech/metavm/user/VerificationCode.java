package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

import java.util.Date;

@EntityType("验证码")
public class VerificationCode extends Entity {

    public static final IndexDef<VerificationCode> IDX =
            IndexDef.create(VerificationCode.class, "receiver", "code", "expiredAt");

    public static final IndexDef<VerificationCode> IDX_CLIENT_IP_CREATED_AT =
            IndexDef.create(VerificationCode.class, "clientIP", "createdAt");

    public static final long DEFAULT_EXPIRE_IN_MILLIS = 15 * 60 * 1000L;

    public static VerificationCode create(String receiver, String code, String clientIP) {
        return new VerificationCode(receiver, code, new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_IN_MILLIS), clientIP);
    }

    @EntityField("验证码")
    private final String code;

    @EntityField("接收地址")
    private final String receiver;

    @EntityField("失效时间")
    private final Date expiredAt;

    @EntityField("客户端IP")
    private final String clientIP;

    @EntityField("创建时间")
    private final Date createdAt = new Date();

    public VerificationCode(String receiver, String code, Date expiredAt, String clientIP) {
        this.code = code;
        this.receiver = receiver;
        this.expiredAt = expiredAt;
        this.clientIP = clientIP;
    }

    public String getClientIP() {
        return clientIP;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCode() {
        return code;
    }

    public String getReceiver() {
        return receiver;
    }

    public Date getExpiredAt() {
        return expiredAt;
    }
}
