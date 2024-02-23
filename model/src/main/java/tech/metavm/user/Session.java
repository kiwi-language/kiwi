package tech.metavm.user;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.AssertUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

@EntityType("会话")
public class Session extends Entity {

    public static final IndexDef<Session> IDX_USER_STATE = IndexDef.create(Session.class, "user", "state");
    public static final IndexDef<Session> IDX_TOKEN = IndexDef.createUnique(Session.class, "token");

    @EntityField("令牌")
    private final String token;
    @EntityField("用户")
    private final User user;
    @EntityField("创建时间")
    private final Date createdAt;
    @EntityField("关闭时间")
    @Nullable
    private Date closedAt;
    @EntityField("自动关闭时间")
    private Date autoCloseAt;
    @EntityField("状态")
    private SessionState state = SessionState.ACTIVE;
    @ChildEntity("会话条目")
    private final ReadWriteArray<SessionEntry> entries = addChild(new ReadWriteArray<>(SessionEntry.class), "entries");

    public Session(User user, Date autoCloseAt) {
        createdAt = new Date();
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.autoCloseAt = autoCloseAt;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
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

    public void setEntry(String key, Instance value) {
        AssertUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        var existing = entries.stream().filter(e -> e.getKey().equals(key)).findFirst();
        if(existing.isPresent()) {
            existing.get().setValue(value);
        } else {
            entries.add(new SessionEntry(key, value));
        }
    }

    public @Nullable Instance getEntry(String key) {
        AssertUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        return entries.stream().filter(e -> e.getKey().equals(key)).findFirst().map(SessionEntry::getValue).orElse(null);
    }

    public void close() {
        AssertUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        state = SessionState.CLOSED;
        closedAt = new Date();
        entries.clear();
    }
}
