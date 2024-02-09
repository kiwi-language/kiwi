package tech.metavm.user;

import tech.metavm.builtin.IndexDef;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.util.UUIDUtils;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType("会话")
public class LabSession {

    public static final IndexDef<LabSession> IDX_USER_STATE = IndexDef.create(LabSession.class, "user", "state");
    public static final IndexDef<LabSession> IDX_TOKEN = IndexDef.createUnique(LabSession.class, "token");

    @EntityField("令牌")
    private final String token;
    @EntityField("用户")
    private final LabUser user;
    @EntityField("创建时间")
    private final Date createdAt;
    @EntityField("关闭时间")
    @Nullable
    private Date closedAt;
    @EntityField("自动关闭时间")
    private Date autoCloseAt;
    @EntityField("状态")
    private LabSessionState state = LabSessionState.ACTIVE;

    public LabSession(LabUser user, Date autoCloseAt) {
        createdAt = new Date();
        this.token = UUIDUtils.randomUUID();
        this.user = user;
        this.autoCloseAt = autoCloseAt;
    }

    public String getToken() {
        return token;
    }

    public LabUser getLabUser() {
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

    public LabSessionState getState() {
        return state;
    }

    public void setAutoCloseAt(Date autoCloseAt) {
        this.autoCloseAt = autoCloseAt;
    }

    public boolean isActive() {
        return state == LabSessionState.ACTIVE && autoCloseAt.after(new Date());
    }

    public void close() {
//        AssertUtils.assertTrue(state == LabSessionState.ACTIVE,
//                ErrorCode.ILLEGAL_SESSION_STATE);
        state = LabSessionState.CLOSED;
        closedAt = new Date();
    }
}
