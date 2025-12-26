package org.metavm.user;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.wire.Parent;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(22)
@Entity
public class SessionEntry extends org.metavm.entity.Entity {

    @Parent
    private final Session session;
    @Getter
    private final String key;
    @Setter
    @Getter
    private Value value;

    public SessionEntry(Session session, String key, Value value) {
        super(session.nextChildId());
        this.session = session;
        this.key = key;
        this.value = value;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return session;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
