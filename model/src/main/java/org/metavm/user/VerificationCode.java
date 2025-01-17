package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(51)
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
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static VerificationCode create(String receiver, String code, String clientIP) {
        return new VerificationCode(receiver, code, new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_IN_MILLIS), clientIP);
    }

    private String code;

    private String receiver;

    private Date expiredAt;

    private String clientIP;

    private Date createdAt = new Date();

    public VerificationCode(String receiver, String code, Date expiredAt, String clientIP) {
        this.code = code;
        this.receiver = receiver;
        this.expiredAt = expiredAt;
        this.clientIP = clientIP;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitUTF();
        visitor.visitLong();
        visitor.visitUTF();
        visitor.visitLong();
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

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("clientIP", this.getClientIP());
        map.put("createdAt", this.getCreatedAt().getTime());
        map.put("code", this.getCode());
        map.put("receiver", this.getReceiver());
        map.put("expiredAt", this.getExpiredAt().getTime());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_VerificationCode;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.code = input.readUTF();
        this.receiver = input.readUTF();
        this.expiredAt = input.readDate();
        this.clientIP = input.readUTF();
        this.createdAt = input.readDate();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(code);
        output.writeUTF(receiver);
        output.writeDate(expiredAt);
        output.writeUTF(clientIP);
        output.writeDate(createdAt);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
