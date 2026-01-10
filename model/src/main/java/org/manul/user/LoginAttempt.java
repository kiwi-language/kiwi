package org.manul.user;

import lombok.Getter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(57)
@Entity
public class LoginAttempt extends org.manul.entity.Entity {

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

    private final boolean successful;

    private final String loginName;

    private final String clientIP;

    private final Date time;

    public LoginAttempt(Id id, boolean successful, String loginName, String clientIP, Date time) {
        super(id);
        this.successful = successful;
        this.loginName = loginName;
        this.clientIP = clientIP;
        this.time = time;
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
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
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
