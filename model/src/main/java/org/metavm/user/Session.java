package org.metavm.user;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.common.ErrorCode;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.AssertUtils;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Wire(17)
@Entity
public class Session extends org.metavm.entity.Entity {

    public static final IndexDef<Session> IDX_USER_STATE = IndexDef.create(Session.class,
            2, session -> List.of(session.userReference, Instances.intInstance(session.state.code())));
    public static final IndexDef<Session> IDX_TOKEN = IndexDef.createUnique(Session.class,
            1, session -> List.of(Instances.stringInstance(session.token)));

    @Getter
    private final String token;
    private final Reference userReference;
    @Getter
    private final Date createdAt;
    @Nullable
    private Date closedAt;
    @Setter
    @Getter
    private Date autoCloseAt;
    @Getter
    private SessionState state = SessionState.ACTIVE;
    private final List<SessionEntry> entries = new ArrayList<>();

    public Session(Id id, User user, Date autoCloseAt) {
        super(id);
        createdAt = new Date();
        this.token = UUID.randomUUID().toString();
        this.userReference = user.getReference();
        this.autoCloseAt = autoCloseAt;
    }

    public User getUser() {
        return (User) userReference.get();
    }

    @Nullable
    public Date getClosedAt() {
        return closedAt;
    }

    public boolean isActive() {
        return state == SessionState.ACTIVE && autoCloseAt.after(new Date());
    }

    public void setEntry(String key, Value value) {
        AssertUtils.assertTrue(state == SessionState.ACTIVE,
                ErrorCode.ILLEGAL_SESSION_STATE);
        var existing = entries.stream().filter(e -> e.getKey().equals(key)).findFirst();
        if(existing.isPresent()) {
            existing.get().setValue(value);
        } else {
            entries.add(new SessionEntry(this, key, value));
        }
    }

    public @Nullable Value getEntry(String key) {
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

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(userReference);
        for (var entries_ : entries) action.accept(entries_.getReference());
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (var entries_ : entries) action.accept(entries_);
    }

}
