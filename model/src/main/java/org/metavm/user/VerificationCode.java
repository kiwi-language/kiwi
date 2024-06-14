package org.metavm.user;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.IndexDef;

import java.util.Date;

@EntityType
public class VerificationCode extends Entity {

    public static final IndexDef<VerificationCode> IDX =
            IndexDef.create(VerificationCode.class, "receiver", "code", "expiredAt");

    public static final IndexDef<VerificationCode> IDX_CLIENT_IP_CREATED_AT =
            IndexDef.create(VerificationCode.class, "clientIP", "createdAt");

    public static final long DEFAULT_EXPIRE_IN_MILLIS = 15 * 60 * 1000L;

    public static VerificationCode create(String receiver, String code, String clientIP) {
        return new VerificationCode(receiver, code, new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_IN_MILLIS), clientIP);
    }

    private final String code;

    private final String receiver;

    private final Date expiredAt;

    private final String clientIP;

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
