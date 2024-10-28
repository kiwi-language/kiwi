package org.metavm.user;

import org.metavm.api.EntityIndex;
import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.lang.EmailUtils;
import org.metavm.api.lang.Indices;
import org.metavm.api.lang.Lang;
import org.metavm.api.lang.RegexUtils;
import org.metavm.utils.LabBusinessException;
import org.metavm.utils.LabErrorCode;

import java.util.Date;

@EntityType
public class LabVerificationCode {

    public static final long DEFAULT_EXPIRE_IN_MILLIS = 15 * 60 * 1000L;

    private static final int MAX_SENT_PER_FIFTEEN_MINUTES = 15;

    public static LabVerificationCode create(String receiver, String code, String clientIP) {
        return new LabVerificationCode(receiver, code, new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_IN_MILLIS), clientIP);
    }

    private final String code;

    private final String receiver;

    private final Date expiredAt;

    private final String clientIP;

    private final Date createdAt = new Date();

    public LabVerificationCode(String receiver, String code, Date expiredAt, String clientIP) {
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

    public static final String EMAIL_PTN = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    public static void sendVerificationCode(String receiver, String title, String clientIP) {
        if (!RegexUtils.match(EMAIL_PTN, receiver))
            throw new LabBusinessException(LabErrorCode.INVALID_EMAIL_ADDRESS);
        var code = Lang.formatNumber("000000", Lang.random(1000000));
        var count = Indices.count(
                new IndexClientIpCreatedAt(clientIP, new Date(System.currentTimeMillis() - 15 * 60 * 1000)),
                new IndexClientIpCreatedAt(clientIP, new Date(System.currentTimeMillis()))
        );
        if (count > MAX_SENT_PER_FIFTEEN_MINUTES)
            throw new LabBusinessException(LabErrorCode.VERIFICATION_CODE_SENT_TOO_OFTEN);
        LabVerificationCode.create(receiver, code, clientIP);
        EmailUtils.send(receiver, title, code);
    }

    public static void checkVerificationCode(String receiver, String code) {
        var valid = Indices.count(
                new IndexReceiverCodeExpiredAt(receiver, code, new Date()),
                new IndexReceiverCodeExpiredAt(receiver, code, new Date(System.currentTimeMillis() + 10 * DEFAULT_EXPIRE_IN_MILLIS))
        ) > 0L;
        if (!valid)
            throw new LabBusinessException(LabErrorCode.INCORRECT_VERIFICATION_CODE);
    }


    public record IndexReceiverCodeExpiredAt(String receiver, String code,
                                             Date expiredAt) implements Index<LabVerificationCode> {
        public IndexReceiverCodeExpiredAt(LabVerificationCode verificationCode) {
            this(verificationCode.receiver, verificationCode.code, verificationCode.expiredAt);
        }
    }

    @EntityIndex
    private IndexReceiverCodeExpiredAt indexReceiverCodeExpiredAt() {
        return new IndexReceiverCodeExpiredAt(receiver, code, expiredAt);
    }

    public record IndexClientIpCreatedAt(String clientIP, Date createdAt) implements Index<LabVerificationCode> {
        public IndexClientIpCreatedAt(LabVerificationCode verificationCode) {
            this(verificationCode.clientIP, verificationCode.createdAt);
        }
    }

    @EntityIndex
    private IndexClientIpCreatedAt indexClientIpCreatedAt() {
        return new IndexClientIpCreatedAt(clientIP, createdAt);
    }

}
