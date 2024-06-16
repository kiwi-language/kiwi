package org.metavm.user;

import org.metavm.api.EntityIndex;
import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.lang.UUIDUtils;

import javax.annotation.Nullable;
import java.util.Date;

@EntityType
public class LabSession {

    private final String token;
    private final LabUser user;
    private final Date createdAt;
    @Nullable
    private Date closedAt;
    private Date autoCloseAt;
    private LabSessionState state = LabSessionState.ACTIVE;

    public LabSession(LabUser user, Date autoCloseAt) {
        createdAt = new Date();
        this.token = UUIDUtils.randomUUID();
        this.user = user;
        this.autoCloseAt = autoCloseAt;
    }

    @EntityIndex
    public record UserStateIndex(LabUser user, LabSessionState state) implements Index<LabSession> {
        public UserStateIndex(LabSession session) {
            this(session.user, session.state);
        }
    }

    @EntityIndex
    public record TokenIndex(String token) implements Index<LabSession> {
        public TokenIndex(LabSession session) {
            this(session.token);
        }
    }

    public String getToken() {
        return token;
    }

    public LabUser getUser() {
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
