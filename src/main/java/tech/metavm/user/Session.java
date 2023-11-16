package tech.metavm.user;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

@EntityType("会话")
public class Session extends Entity {

    public static final IndexDef<Session> IDX_TOKEN = IndexDef.uniqueKey(Session.class, "token");

    @EntityField("令牌")
    private final String token;
    @EntityField("用户")
    private final UserRT user;
    @EntityField("创建时间")
    private final Date createdAt;
    @EntityField("关闭时间")
    @Nullable
    private Date closedAt;
    @EntityField("自动关闭时间")
    private Date autoCloseAt;
    @EntityField("状态")
    private SessionState state = SessionState.ACTIVE;

    public Session(UserRT user, Date autoCloseAt) {
        createdAt = new Date();
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.autoCloseAt = autoCloseAt;
    }

    public String getToken() {
        return token;
    }

    public UserRT getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getAutoCloseAt() {
        return autoCloseAt;
    }

    @Nullable
    public Date getClosedAt() {
        return closedAt;
    }

    public SessionState getState() {
        return state;
    }

    public void setAutoCloseAt(Date autoCloseAt) {
        this.autoCloseAt = autoCloseAt;
    }

    public boolean isActive() {
        return state == SessionState.ACTIVE && autoCloseAt.after(new Date());
    }

    public void close() {
        NncUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        state = SessionState.CLOSED;
        closedAt = new Date();
    }
}
