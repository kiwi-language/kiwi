package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;
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

@NativeEntity(57)
@Entity
public class LoginAttempt extends org.metavm.entity.Entity {

    public static final IndexDef<LoginAttempt> IDX_LOGIN_NAME_SUCC_TIME =
            IndexDef.create(LoginAttempt.class,
                    3, loginAttempt -> List.of(
                            Instances.stringInstance(loginAttempt.loginName),
                            Instances.booleanInstance(loginAttempt.successful),
                            Instances.longInstance(loginAttempt.time.getTime())
                    ));

    public static final IndexDef<LoginAttempt> IDX_CLIENT_IP_SUCC_TIME  =
            IndexDef.create(LoginAttempt.class,
                    3, loginAttempt -> List.of(
                            Instances.stringInstance(loginAttempt.clientIP),
                            Instances.booleanInstance(loginAttempt.successful),
                            Instances.longInstance(loginAttempt.time.getTime())
                    ));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private boolean successful;

    private String loginName;

    private String clientIP;

    private Date time;

    public LoginAttempt(boolean successful, String loginName, String clientIP, Date time) {
        this.successful = successful;
        this.loginName = loginName;
        this.clientIP = clientIP;
        this.time = time;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
        visitor.visitUTF();
        visitor.visitUTF();
        visitor.visitLong();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getClientIP() {
        return clientIP;
    }

    public Date getTime() {
        return time;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("successful", this.isSuccessful());
        map.put("loginName", this.getLoginName());
        map.put("clientIP", this.getClientIP());
        map.put("time", this.getTime().getTime());
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
        return EntityRegistry.TAG_LoginAttempt;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.successful = input.readBoolean();
        this.loginName = input.readUTF();
        this.clientIP = input.readUTF();
        this.time = input.readDate();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(successful);
        output.writeUTF(loginName);
        output.writeUTF(clientIP);
        output.writeDate(time);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
