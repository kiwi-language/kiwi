package org.metavm.user;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.util.AssertUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.UUID;

@EntityType
public class Session extends Entity {

    public static final IndexDef<Session> IDX_USER_STATE = IndexDef.create(Session.class, "user", "state");
    public static final IndexDef<Session> IDX_TOKEN = IndexDef.createUnique(Session.class, "token");

    private final String token;
    private final User user;
    private final Date createdAt;
    @Nullable
    private Date closedAt;
    private Date autoCloseAt;
    private SessionState state = SessionState.ACTIVE;
    @ChildEntity
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

    public boolean removeEntry(String key) {
        return entries.removeIf(e -> e.getKey().equals(key));
    }

    public void close() {
        AssertUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        state = SessionState.CLOSED;
        closedAt = new Date();
        entries.clear();
    }
}
