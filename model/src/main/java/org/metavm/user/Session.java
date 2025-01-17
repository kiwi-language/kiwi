package org.metavm.user;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.AssertUtils;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@NativeEntity(17)
@Entity
public class Session extends org.metavm.entity.Entity {

    public static final IndexDef<Session> IDX_USER_STATE = IndexDef.create(Session.class,
            2, session -> List.of(session.userReference, Instances.intInstance(session.state.code())));
    public static final IndexDef<Session> IDX_TOKEN = IndexDef.createUnique(Session.class,
            1, session -> List.of(Instances.stringInstance(session.token)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private String token;
    private Reference userReference;
    private Date createdAt;
    @Nullable
    private Date closedAt;
    private Date autoCloseAt;
    private SessionState state = SessionState.ACTIVE;
    private List<SessionEntry> entries = new ArrayList<>();

    public Session(User user, Date autoCloseAt) {
        createdAt = new Date();
        this.token = UUID.randomUUID().toString();
        this.userReference = user.getReference();
        this.autoCloseAt = autoCloseAt;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
        visitor.visitLong();
        visitor.visitNullable(visitor::visitLong);
        visitor.visitLong();
        visitor.visitByte();
        visitor.visitList(visitor::visitEntity);
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return (User) userReference.get();
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
        entries.forEach(arg -> action.accept(arg.getReference()));
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("token", this.getToken());
        map.put("user", this.getUser().getStringId());
        map.put("createdAt", this.getCreatedAt().getTime());
        map.put("autoCloseAt", this.getAutoCloseAt().getTime());
        var closedAt = this.getClosedAt();
        if (closedAt != null) map.put("closedAt", closedAt.getTime());
        map.put("state", this.getState().name());
        map.put("active", this.isActive());
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
        entries.forEach(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Session;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.token = input.readUTF();
        this.userReference = (Reference) input.readValue();
        this.createdAt = input.readDate();
        this.closedAt = input.readNullable(input::readDate);
        this.autoCloseAt = input.readDate();
        this.state = SessionState.fromCode(input.read());
        this.entries = input.readList(() -> input.readEntity(SessionEntry.class, this));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(token);
        output.writeValue(userReference);
        output.writeDate(createdAt);
        output.writeNullable(closedAt, output::writeDate);
        output.writeDate(autoCloseAt);
        output.write(state.code());
        output.writeList(entries, output::writeEntity);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
