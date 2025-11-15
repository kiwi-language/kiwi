package org.metavm.user;

import lombok.Getter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(51)
@Entity
public class VerificationCode extends org.metavm.entity.Entity {

    public static final IndexDef<VerificationCode> IDX =
            IndexDef.create(VerificationCode.class,
                    3, verificationCode -> List.of(
                            Instances.stringInstance(verificationCode.receiver),
                            Instances.stringInstance(verificationCode.code),
                            Instances.longInstance(verificationCode.createdAt.getTime())
                    ));
    public static final IndexDef<VerificationCode> IDX_CLIENT_IP_CREATED_AT =
            IndexDef.create(VerificationCode.class,
                    2, verificationCode -> List.of(
                       Instances.stringInstance(verificationCode.clientIP),
                       Instances.longInstance(verificationCode.createdAt.getTime())
                    ));

    public static final long DEFAULT_EXPIRE_IN_MILLIS = 15 * 60 * 1000L;

    public static VerificationCode create(Id id, String receiver, String code, String clientIP) {
        return new VerificationCode(id, receiver, code, new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_IN_MILLIS), clientIP);
    }

    private final String code;

    private final String receiver;

    private final Date expiredAt;

    private final String clientIP;

    private final Date createdAt = new Date();

    public VerificationCode(Id id, String receiver, String code, Date expiredAt, String clientIP) {
        super(id);
        this.code = code;
        this.receiver = receiver;
        this.expiredAt = expiredAt;
        this.clientIP = clientIP;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
