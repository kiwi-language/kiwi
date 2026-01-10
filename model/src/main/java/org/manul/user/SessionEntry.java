package org.manul.user;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.wire.Parent;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(22)
@Entity
public class SessionEntry extends org.manul.entity.Entity {

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
    public org.manul.entity.Entity getParentEntity() {
        return session;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.manul.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
