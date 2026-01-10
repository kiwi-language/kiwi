package org.manul.message;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.api.EntityField;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.entity.SearchField;
import org.manul.message.rest.dto.MessageDTO;
import org.manul.object.instance.core.*;
import org.manul.user.User;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Wire(56)
@Entity
public class Message extends org.manul.entity.Entity {

    public static final IndexDef<Message> IDX_TARGET = IndexDef.create(Message.class,
            1, message -> List.of(message.target));

    public static final SearchField<Message> esTitle =
            SearchField.createTitle(0, "s0", msg -> Instances.stringInstance(msg.title));

    public static final SearchField<Message> esReceiver =
            SearchField.create(0, "r0", msg -> msg.receiver);

    public static final SearchField<Message> esRead =
            SearchField.create(0, "b0", msg -> Instances.booleanInstance(msg.read));

    private final EntityReference receiver;
    @EntityField(asTitle = true)
    private final String title;
    @Getter
    private final MessageKind kind;
    @Setter
    @Getter
    private boolean read;
    private Value target;

    public Message(Id id, User receiver, String title, MessageKind kind, Value target) {
        super(id);
        this.receiver = receiver.getReference();
        this.title = title;
        this.kind = kind;
        this.target = target;
    }

    public User getReceiver() {
        return (User) receiver.get();
    }

    @Nullable
    public Object getTarget() {
        return target;
    }

    public void clearTarget() {
        this.target = Instances.nullInstance();
    }

    public MessageDTO toDTO() {
        return new MessageDTO(
                getStringId(),
                receiver.getStringId(), title, kind.code,
                ((EntityReference) target).getStringId(),
                read
        );
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(receiver);
        if (target instanceof Reference r) action.accept(r);
        else if (target instanceof NativeValue t) t.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        source.put("l0." + esTitle.getColumn(), esTitle.getValue(this));
        source.put("l0." + esReceiver.getColumn(), esReceiver.getValue(this));
        source.put("l0." + esRead.getColumn(), esRead.getValue(this));
    }
}
